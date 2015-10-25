package com.io.domain;

public class UserEdit extends Packet {
    private int userId;

    private String editText;

    private int lengthDifference;

    private int offset;

    public UserEdit(int userId, String editText, int offset, int lengthDifference) {

        super(PacketType.DOCUMENT_EDIT.id());

        this.userId = userId;
        this.editText = editText;
        this.offset = offset;
        this.lengthDifference = lengthDifference;
    }

    public UserEdit(int userId, int offset, int lengthDifference) {

        super(PacketType.CURSOR_MOVE.id());

        this.userId = userId;
        this.editText = null;
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

    @Override
    public String toString() {
        return "UserEdit{" +
                "userId=" + userId +
                ", editText='" + editText + '\'' +
                ", offset=" + offset +
                ", lengthDifference=" + lengthDifference +
                '}';
    }
}
