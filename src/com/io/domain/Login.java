package com.io.domain;

public class Login extends Packet {
    private String username;

    public Login(int packetType, String username){
        super(packetType);
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }
}