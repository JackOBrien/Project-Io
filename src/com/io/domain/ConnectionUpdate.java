package com.io.domain;

import java.util.Map;

public class ConnectionUpdate extends Packet {
    private Map<Integer, String> userMap;

    public ConnectionUpdate(int userId, Map<Integer, String> userMap){
        super(userId, PacketType.CONNECTION_UPDATE);
        this.userMap = userMap;
    }

    public Map<Integer, String> getUserMap() {
        return userMap;
    }
}
