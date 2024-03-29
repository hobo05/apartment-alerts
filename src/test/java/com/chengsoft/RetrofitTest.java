package com.chengsoft;

import com.chengsoft.model.Apartment;
import com.chengsoft.model.Community;
import com.chengsoft.service.ApartmentSearchService;
import com.chengsoft.service.AvalonService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Created by tcheng on 4/23/16.
 */
@Ignore
public class RetrofitTest {

    final static Logger logger = LoggerFactory.getLogger(RetrofitTest.class);

    @Rule public MockitoRule mockito = MockitoJUnit.rule();

    @InjectMocks ApartmentSearchService apartmentSearchService;
    @Mock AvalonService avalonService;
    @Mock Call<JsonNode> retrofitCall;

    @Test
    public void  testDates() {
        ArrayList<Apartment> apartments = Lists.newArrayList(Apartment.builder()
                        .apartmentCode("a")
                        .dateFound(LocalDate.of(2016, 1, 1))
                        .build(),
                Apartment.builder()
                        .apartmentCode("b")
                        .dateFound(LocalDate.of(2018, 1, 1))
                        .build(),
                Apartment.builder()
                        .apartmentCode("a")
                        .dateFound(LocalDate.of(2012, 1, 1))
                        .build());
        System.out.println(apartments.stream()
        .max(Comparator.comparing(Apartment::getDateFound)));
    }

    @Test
    public void testRetrofit() {

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl("https://api.avalonbay.com/json/reply/")
                .build();

        AvalonService service = retrofit.create(AvalonService.class);
        Call<JsonNode> repos = service.search("MA039",
                "2016-06-01T04:00:00.000Z",
                1900,
                3300);
        try {
            Response<JsonNode> response = repos.execute();
            if (response.isSuccessful()) {
                logger.info(response.body().toString());
            } else {
                logger.error(response.errorBody().string());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testParseJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });

        File jsonFile = new File("src/test/resources/available.json");
//        File jsonFile = new File("src/test/resources/unavailable.json");

        JsonNode jsonNode = mapper.readValue(jsonFile, JsonNode.class);
        System.out.println(jsonNode);

        Response<JsonNode> retrofitResponse = Response.success(jsonNode);
        when(retrofitCall.execute()).thenReturn(retrofitResponse);
        when(avalonService.search(anyString(), anyString(), anyInt(), anyInt())).thenReturn(retrofitCall);

        Set<Apartment> apartments = apartmentSearchService.lookForNewApartments(LocalDate.of(2016, 06, 1), Community.SOMERVILLE);
        logger.info("Apartments={}", apartments);

    }

    @Test
    public void testDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.of(2016, 5, 28, 0, 0), ZoneId.of("America/New_York"));
        ZonedDateTime utc = zdt.withZoneSameInstant(ZoneId.of("UTC"));
        System.out.println(formatter.format(utc));
        System.out.println(DateTimeFormatter.ISO_LOCAL_DATE.format(utc));

    }
}
