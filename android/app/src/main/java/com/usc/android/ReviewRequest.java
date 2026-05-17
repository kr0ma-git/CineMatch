package com.usc.android;

public class ReviewRequest {
    private String userId;
    private String movieId;
    private String movieTitle;
    private String movieReview;
    private int movieReviewStars;

    public ReviewRequest(String userId, String movieId, String movieTitle, String movieReview, int movieReviewStars) {
        this.userId = userId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.movieReview = movieReview;
        this.movieReviewStars = movieReviewStars;
    }
}
