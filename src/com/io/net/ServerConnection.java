package com.io.net;

import com.io.gui.Client;

public class ServerConnection {

    private int userId;
    private String username;
    private Connector connector;

    public ServerConnection(int userId, Connector connector) {
        this.userId = userId;
        this.connector = connector;
        this.username = Client.INITIAL_USER_NAME;
    }

    public int getUserId() {
        return userId;
    }

    public Connector getConnector() {
        return connector;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
