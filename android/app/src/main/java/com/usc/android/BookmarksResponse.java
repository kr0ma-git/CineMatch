package com.usc.android;

import java.util.List;

public class BookmarksResponse {
    private String message;
    private List<Bookmark> bookmarks;

    public String getMessage() { return message; }
    public List<Bookmark> getBookmarks() { return bookmarks; }
}
