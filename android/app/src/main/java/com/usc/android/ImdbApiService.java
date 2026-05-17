package com.usc.android;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ImdbApiService {
    @GET("/")
    Call<ImdbSearchResponse> searchMovies(
            @Query("apikey") String apiKey,
            @Query("s") String query,
            @Query("type") String type
    );
}
