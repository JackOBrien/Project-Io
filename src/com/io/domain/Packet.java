package com.io.domain;

import java.io.Serializable;

public abstract class Packet implements Serializable {

    private int packetType;

    public Packet(int packetType) {
        this.packetType = packetType;
    }

    public int getPacketType() {
        return packetType;
    }
}
