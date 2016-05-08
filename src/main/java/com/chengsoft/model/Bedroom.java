package com.chengsoft.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

/**
 * Created by tcheng on 5/7/16.
 */
public enum Bedroom {
    ONE("1 bedroom"),
    TWO("2 bedrooms");

    private String value;

    Bedroom(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Bedroom forValue(String value) {
        return Stream.of(values())
                .filter(b -> b.value.equals(value))
                .findFirst()
                .orElse(null);
    }

    public String getValue() {
        return value;
    }
}
