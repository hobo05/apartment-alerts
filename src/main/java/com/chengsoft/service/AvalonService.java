package com.chengsoft.service;

import com.fasterxml.jackson.databind.JsonNode;
import retrofit2.Call;
import retrofit2.http.Query;
import retrofit2.http.GET;

/**
 * Created by tcheng on 4/23/16.
 */
public interface AvalonService {

    @GET("ApartmentSearch")
    Call<JsonNode> search(@Query("communityCode") String communityCode,
                          @Query("desiredMoveInDate") String desiredMoveInDate,
                          @Query("min") Integer min,
                          @Query("max") Integer max);
}
