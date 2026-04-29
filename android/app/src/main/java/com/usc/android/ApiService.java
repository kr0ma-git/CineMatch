package com.usc.android;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/v1/users/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);

    @POST("api/v1/users/login")
    Call<RegisterResponse> loginUser(@Body LoginRequest request);
}
