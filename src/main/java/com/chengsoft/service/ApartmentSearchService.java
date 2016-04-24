package com.chengsoft.service;

import com.chengsoft.model.Apartment;
import com.chengsoft.model.FloorPlan;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * Created by tcheng on 4/23/16.
 */
@Service
public class ApartmentSearchService {

    final static Logger logger = LoggerFactory.getLogger(ApartmentSearchService.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private static final String ONE_BEDROOM_PATH = "$.results.availableFloorPlanTypes[?(@.floorPlanTypeCode == '1BD')].availableFloorPlans";
    private static final String ALL = "[*]";
    private static final String APARTMENTS_FORMAT = "..apartments";


    @Cacheable("apartments")
    public Set<Apartment> lookForNewApartments(LocalDate moveInDate) {

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl("https://api.avalonbay.com/json/reply/")
                .build();

        LocalDateTime localDateTime = LocalDateTime.of(moveInDate, LocalTime.MIN);
        ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.of("America/New_York"));
        ZonedDateTime utc = zdt.withZoneSameInstant(ZoneId.of("UTC"));

        AvalonSomervilleService service = retrofit.create(AvalonSomervilleService.class);
        Call<JsonNode> jsonResponse = service.search("MA039",
                FORMATTER.format(utc),
                1900,
                3300);

        Response<JsonNode> response = null;
        try {
            response = jsonResponse.execute();
            if (!response.isSuccessful()) {
                logger.error("Call to Avalon Somerville Failed! " + response.errorBody().string());
                return ImmutableSet.of();
            }
        } catch (IOException e) {
            logger.error("Failed to invoke service call to AvalonSomervilleService: ", e);
        }

        String json = response.body().toString();
        List<FloorPlan> oneBedroomFloorPlans = JsonPath
                .parse(json)
                .read(ONE_BEDROOM_PATH + ALL,
                        new TypeRef<List<FloorPlan>>() {});

        String formattedDate = DateTimeFormatter.ISO_DATE.format(moveInDate);

        if (oneBedroomFloorPlans.isEmpty()) {
            logger.error("No floor plans available for {}", formattedDate);
            return ImmutableSet.of();
        }

        logger.info("{} One bedroom floor plans availablefor {}", oneBedroomFloorPlans.size(), formattedDate);

        List<Apartment> apartments = JsonPath
                .parse(json)
                .read(ONE_BEDROOM_PATH+APARTMENTS_FORMAT+ALL,
                        new TypeRef<List<Apartment>>() {});

        return ImmutableSet.copyOf(apartments);
    }
}
