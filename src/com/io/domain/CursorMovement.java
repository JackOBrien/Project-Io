package com.io.domain;


public class CursorMovement extends Packet {

    private String filePath;
    private int position;

    public CursorMovement(int userId, String filePath, int position) {
        super(userId, PacketType.CURSOR_MOVE);

        this.filePath = filePath;
        this.position = position;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public int getPosition() {
        return this.position;
    }

}
