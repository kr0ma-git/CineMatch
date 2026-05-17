package com.usc.android;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("_id")
    private String id;
    private String user;
    private String imdbMovieId;
    private String imdbMovieTitle;
    private String movieReviewString;
    private float movieRatingStars;

    public String getId() { return id; }
    public String getUser() { return user; }
    public String getImdbMovieId() { return imdbMovieId; }
    public String getImdbMovieTitle() { return imdbMovieTitle; }
    public String getMovieReviewString() { return movieReviewString; }
    public float getMovieRatingStars() { return movieRatingStars; }
}
