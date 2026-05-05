package com.usc.android;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/v1/users/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);

    @POST("api/v1/users/login")
    Call<RegisterResponse> loginUser(@Body LoginRequest request);

    @GET("api/v1/users/{id}")
    Call<RegisterResponse.UserData> getUserProfile(@Path("id") String userId);
}
