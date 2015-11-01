package com.io.domain;

import java.util.Hashtable;

public class ConnectionUpdate extends Packet {
    private Hashtable<Integer, String> userTable;

    public ConnectionUpdate(int userId, Hashtable<Integer, String> userTable){
        super(userId, PacketType.CONNECTION_UPDATE);
        this.userTable = userTable;
    }

    public Hashtable<Integer, String> getUserTable() {
        return userTable;
    }
}
