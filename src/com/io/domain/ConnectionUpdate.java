package com.io.domain;


import com.io.gui.UserInfo;

import java.util.ArrayList;

public class ConnectionUpdate extends Packet {
    private ArrayList<UserInfo> userTable;

    public ConnectionUpdate(int userId, ArrayList<UserInfo> userTable){
        super(userId, PacketType.CONNECTION_UPDATE);
        this.userTable = userTable;
    }

    public ArrayList<UserInfo> getUserList() {
        return userTable;
    }
}
