package com.app.soundmeter.api;

import com.app.soundmeter.dto.ResponseMessage;
import com.app.soundmeter.dto.UserCredentials;
import com.app.soundmeter.dto.UserProfile;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserAPI {

    @POST("users/login")
    Call<UserProfile> loginUser(@Body UserCredentials userCredentials);

    @POST("users/register")
    Call<ResponseMessage> registerUser(@Body UserCredentials userCredentials);

    @GET("users/profile")
    Call<UserProfile> getUserProfile(@Query("login") String login);

    @POST("users/profile")
    Call<ResponseMessage> saveProfile(@Body UserProfile userProfile);
}
