package com.usc.android;

import java.util.List;

public class UsersResponse {
    private String message;
    private List<RegisterResponse.UserData> userData;

    public String getMessage() { return message; }
    public List<RegisterResponse.UserData> getUserData() { return userData; }
}
