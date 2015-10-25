package com.io.domain;

public abstract class Packet {

    private int packetType;

    public Packet(int packetType) {
        this.packetType = packetType;
    }

    public int getPacketType() {
        return packetType;
    }
}
