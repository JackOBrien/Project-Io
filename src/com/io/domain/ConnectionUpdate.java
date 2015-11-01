package com.io.domain;


import javafx.util.Pair;

import java.util.ArrayList;

public class ConnectionUpdate extends Packet {
    private ArrayList<Pair<Integer, String>> userTable;

    public ConnectionUpdate(int userId, ArrayList<Pair<Integer, String>> userTable){
        super(userId, PacketType.CONNECTION_UPDATE);
        this.userTable = userTable;
    }

    public ArrayList<Pair<Integer, String>> getUserList() {
        return userTable;
    }
}
