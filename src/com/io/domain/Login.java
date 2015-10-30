package com.io.domain;

public class Login extends Packet {
    private String username;

    public Login(int userId, String username){
        super(userId, PacketType.LOGIN);
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }
}