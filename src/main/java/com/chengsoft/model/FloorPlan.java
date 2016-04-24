package com.chengsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Created by tcheng on 4/23/16.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FloorPlan {
    private Integer floorPlanId;
    private String floorPlanName;
    private Integer estimatedSize;
    private String floorPlanImage;
}
