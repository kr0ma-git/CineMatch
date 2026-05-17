package com.usc.android;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ImdbSearchResponse {
    @SerializedName("Search")
    private List<SearchResult> search;
    
    @SerializedName("Response")
    private String response;

    public List<SearchResult> getSearch() { return search; }
    public String getResponse() { return response; }

    public static class SearchResult {
        @SerializedName("imdbID")
        private String id;
        
        @SerializedName("Title")
        private String title;
        
        @SerializedName("Poster")
        private String image;
        
        @SerializedName("Year")
        private String description;

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getImage() { return image; }
        public String getDescription() { return description; }
    }
}
