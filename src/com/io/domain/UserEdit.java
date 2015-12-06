package com.io.domain;

public class UserEdit extends Packet {

    private String filePath;
    private String oldFragment;
    private String newFragment;
    private int position;
    private int timestamp;

    public UserEdit(int userId, String filePath, String oldFragment, String newFragment, int position) {
        this(userId, filePath, oldFragment, newFragment, position, -1);
    }

    public UserEdit(int userId, String filePath, String oldFragment, String newFragment, int position, int timestamp) {

        super(userId, PacketType.DOCUMENT_EDIT);

        this.filePath = filePath;
        this.oldFragment = oldFragment;
        this.newFragment = newFragment;
        this.position = position;
        this.timestamp = timestamp;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getOldFragment() {
        return this.oldFragment;
    }

    public String getNewFragment() {
        return this.newFragment;
    }

    public int getPosition() {
        return this.position;
    }

    public int getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserEdit{" +
                "userId=" + super.getUserId() +
                ", filePath='" + filePath + "'" +
                ", oldFragment='" + oldFragment + "'" +
                ", newFragment='" + newFragment + "'" +
                ", position=" + position +
                ", timestamp=" + timestamp +
                "}";
    }
}
