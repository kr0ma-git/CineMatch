package com.usc.android;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    private String message;
    private UserData user;

    public String getMessage() { return message; }
    public UserData getUser() { return user; }

    public static class UserData {
        @SerializedName("id")
        private String id;
        private String email;
        private String username;

        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getUsername() { return username; }
    }
}
