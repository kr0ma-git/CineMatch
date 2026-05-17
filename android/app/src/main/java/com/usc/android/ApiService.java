package com.usc.android;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/v1/users/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);

    @POST("api/v1/users/login")
    Call<RegisterResponse> loginUser(@Body LoginRequest request);

    @GET("api/v1/users/{id}")
    Call<RegisterResponse> getUserProfile(@Path("id") String userId);

    @GET("api/v1/reviews")
    Call<ReviewsResponse> getAllReviews();

    @POST("api/v1/reviews/create")
    Call<Void> createReview(@Body ReviewRequest request);

    @DELETE("api/v1/reviews/delete/{reviewId}")
    Call<Void> deleteReview(@Path("reviewId") String reviewId);

    @GET("api/v1/bookmarks")
    Call<BookmarksResponse> getAllBookmarks();

    @POST("api/v1/bookmarks/create")
    Call<BookmarksResponse> createBookmark(@Body BookmarkRequest request);

    @GET("api/v1/bookmarks/{userId}")
    Call<BookmarksResponse> getUserBookmarks(@Path("userId") String userId);

    @DELETE("api/v1/bookmarks/delete/{userId}/{movieId}")
    Call<Void> deleteBookmark(@Path("userId") String userId, @Path("movieId") String movieId);

    @GET("api/v1/reviews/user/{userId}")
    Call<ReviewsResponse> getUserReviews(@Path("userId") String userId);
}
