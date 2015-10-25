package com.io.domain;

public enum PacketType {
    DOCUMENT_EDIT(0),
    CURSOR_MOVE(1),
    LOGIN(2),
    FILE_REQUEST(3);

    private int packetID;

    PacketType(int packetID){
        this.packetID = packetID;
    }

    public int id() {
        return packetID;
    }
}
