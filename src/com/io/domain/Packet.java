package com.io.domain;

import java.io.Serializable;

public abstract class Packet implements Serializable {

    private int userId;

    private PacketType packetType;

    public Packet(int userId, PacketType packetType) {
        this.userId = userId;
        this.packetType = packetType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public PacketType getPacketType() {
        return packetType;
    }
}
