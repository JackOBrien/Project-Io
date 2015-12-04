package com.io.domain;

public class Logout extends Packet {

    public Logout(int userId) {
        super(userId, PacketType.LOGOUT);
    }

}
