package com.app.soundmeter.api;

import com.app.soundmeter.dto.Measurement;
import com.app.soundmeter.dto.ResponseMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

// API do komunikacji z backendem (pomiary)
public interface MeasurementAPI {

    @POST("measurements/save")
    Call<ResponseMessage> saveMeasurement(@Body Measurement measurement);

    @GET("measurements/all")
    Call<List<Measurement>> getMeasurements(@Query("login") String login);
}
