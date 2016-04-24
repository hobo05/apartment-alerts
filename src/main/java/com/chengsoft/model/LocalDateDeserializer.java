package com.chengsoft.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Created by tcheng on 4/23/16.
 */
public class LocalDateDeserializer
        extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser jsonParser,
                                 DeserializationContext deserializationContext) throws
            IOException, JsonProcessingException {

        String text = jsonParser.getText();
        Long epochMillis = Long.parseLong(text.replaceFirst("/Date\\((\\d+)\\)/", "$1"));
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.of("UTC")).toLocalDate();
    }
}
