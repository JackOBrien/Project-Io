package com.io.domain;

public class Login extends Packet {
    private String username;
    private int userId;

    public Login(String username){
        super(PacketType.LOGIN.id());
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
}