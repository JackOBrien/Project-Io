package com.io.domain;

import java.io.Serializable;

public abstract class Packet implements Serializable {

    private int userId;

    private int packetType;

    public Packet(int userId, int packetType) {
        this.userId = userId;
        this.packetType = packetType;
    }

    public int getUserId() {
        return userId;
    }

    public int getPacketType() {
        return packetType;
    }
}
