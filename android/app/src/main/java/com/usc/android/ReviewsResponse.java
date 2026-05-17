package com.usc.android;

import java.util.List;

public class ReviewsResponse {
    private String message;
    private List<Review> reviews;

    public String getMessage() { return message; }
    public List<Review> getReviews() { return reviews; }
}
