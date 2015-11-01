package com.io.domain;

public class UserEdit extends Packet {

    private String editText;
    private String filePath;
    private int lengthDifference;
    private int offset;

    public UserEdit(int userId, String editText, String filePath, int offset, int lengthDifference) {

        super(userId, PacketType.DOCUMENT_EDIT);

        this.editText = editText;
        this.filePath = filePath;
        this.offset = offset;
        this.lengthDifference = lengthDifference;
    }

    public UserEdit(int userId, String filePath, int offset, int lengthDifference) {

        super(userId, PacketType.CURSOR_MOVE);

        this.editText = null;
        this.filePath = filePath;
        this.offset = offset;
        this.lengthDifference = lengthDifference;
    }

    public String getEditText() {
        return this.editText;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getLengthDifference() {
        return this.lengthDifference;
    }

    public String getFilePath() {
        return this.filePath;
    }

    @Override
    public String toString() {
        return "UserEdit{" +
                "userId=" + super.getUserId() +
                ", editText='" + editText + "'" +
                ", filePath='" + filePath + "'" +
                ", offset=" + offset +
                ", lengthDifference=" + lengthDifference +
                "}";
    }
}
