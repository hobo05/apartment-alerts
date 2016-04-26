package com.chengsoft.service;

import com.chengsoft.model.Apartment;
import com.chengsoft.model.Community;
import com.chengsoft.model.FloorPlan;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.*;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
    private static Multimap<String, Apartment> cachedApartmentsMap = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

    public Set<Apartment> lookForNewApartments(LocalDate moveInDate, Community community) {

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl("https://api.avalonbay.com/json/reply/")
                .build();

        LocalDateTime localDateTime = LocalDateTime.of(moveInDate, LocalTime.MIN);
        ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.of("America/New_York"));
        ZonedDateTime utc = zdt.withZoneSameInstant(ZoneId.of("UTC"));

        AvalonService service = retrofit.create(AvalonService.class);

        Call<JsonNode> jsonResponse = service.search(community.getCode(),
                FORMATTER.format(utc),
                2000,
                3500);

        Response<JsonNode> response = null;
        try {
            response = jsonResponse.execute();
            if (!response.isSuccessful()) {
                logger.error("Call to Avalon {} Failed! Error body=[{}]", community, response.errorBody().string());
                return ImmutableSet.of();
            } else if (Objects.isNull(response.body())) {
                logger.error("Call to Avalon {} Failed! Null response!", community, response.errorBody().string());
                return ImmutableSet.of();
            }
        } catch (IOException e) {
            logger.error("Exception when invoking service call to AvalonService: ", e);
        }

        String json = response.body().toString();
        List<FloorPlan> oneBedroomFloorPlans = JsonPath
                .parse(json)
                .read(ONE_BEDROOM_PATH + ALL,
                        new TypeRef<List<FloorPlan>>() {
                        });

        String formattedDate = DateTimeFormatter.ISO_DATE.format(moveInDate);

        if (oneBedroomFloorPlans.isEmpty()) {
            logger.error("{} - No floor plans available for {}", community, formattedDate);
            return ImmutableSet.of();
        }

        logger.info("{} - {} One bedroom floor plans available for {}", community, oneBedroomFloorPlans.size(), formattedDate);

        Set<Apartment> foundApartments = JsonPath
                .parse(json)
                .read(ONE_BEDROOM_PATH + APARTMENTS_FORMAT + ALL,
                        new TypeRef<List<Apartment>>() {
                        })
                .stream()
                .map(a -> a.toBuilder()
                        .community(community)
                        .build())
                .collect(toSet());

        String mapKey = String.format("%s_%s", community.toString(), DateTimeFormatter.ISO_DATE.format(localDateTime));
        logger.info("API Call Results [key={}, apartments={}]", mapKey, foundApartments.stream()
                .map(Apartment::getApartmentNumber)
                .collect(toList()));

        // Get the new apartments and save them to the cached apartments
        Set<Apartment> cachedApartments = ImmutableSet.copyOf(cachedApartmentsMap.get(mapKey));

        logger.info("Cached Apartments [key={}, apartments={}]", mapKey, cachedApartments.stream()
                .map(Apartment::getApartmentNumber)
                .collect(toList()));

        Set<Apartment> difference = Sets.difference(foundApartments, cachedApartments);

        logger.info("New Apartments not in cache [key={}, apartments={}]", mapKey, difference.stream()
                .map(Apartment::getApartmentNumber)
                .collect(toList()));

        cachedApartmentsMap.putAll(mapKey, difference);

        ImmutableSet<Apartment> returnedApartments = ImmutableSet.copyOf(cachedApartmentsMap.get(mapKey));
        logger.info("Returned Apartments [key={}, apartments={}]", mapKey, difference.stream()
                .map(Apartment::getApartmentNumber)
                .collect(toList()));

        return returnedApartments;
    }

    public Optional<Apartment> getOneMostRecent() {
        return cachedApartmentsMap.values().stream()
                .max(comparing(Apartment::getDateFound));
    }
}
