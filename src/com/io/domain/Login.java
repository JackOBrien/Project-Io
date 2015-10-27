package com.io.domain;

public class Login extends Packet {
    private String username;

    public Login(String username){
        super(PacketType.LOGIN.id());
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }
}