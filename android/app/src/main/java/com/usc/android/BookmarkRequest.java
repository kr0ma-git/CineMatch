package com.usc.android;

public class BookmarkRequest {
    private String userId;
    private String movieId;
    private String movieTitle;
    private String type;

    public BookmarkRequest(String userId, String movieId, String movieTitle, String type) {
        this.userId = userId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.type = type;
    }
}
