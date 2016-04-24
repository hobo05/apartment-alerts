package com.chengsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.LocalDate;

/**
 * Created by tcheng on 4/23/16.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pricing {
    private Integer amenitizedRent;
    private Integer effectiveRent;
    private Boolean showAsAvailable;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate availableDate;
}
