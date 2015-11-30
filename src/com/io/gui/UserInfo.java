package com.io.gui;

import java.io.Serializable;

public class UserInfo implements Serializable {

    private int userId;
    private String username;

    public UserInfo(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String toString() {
        return this.username + " (" + this.userId + ")";
    }

    public int getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }
    
}
