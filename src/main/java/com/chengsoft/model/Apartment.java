package com.chengsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Created by tcheng on 4/23/16.
 */
@Data @Builder(toBuilder = true)
@EqualsAndHashCode(exclude = "dateFound") // don't take into dateFound into account
@NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Apartment {
    private String apartmentCode;
    private Integer apartmentNumber;
    private Integer floor;
    private Pricing pricing;
    private LocalDate dateFound = LocalDate.now(ZoneId.of("America/New_York"));
    private Community community;
}
