package com.io.domain;

public class UserEdit extends Packet {

    private String editText;

    private int lengthDifference;

    private int offset;

    public UserEdit(int userId, String editText, int offset, int lengthDifference) {

        super(userId, PacketType.DOCUMENT_EDIT);

        this.editText = editText;
        this.offset = offset;
        this.lengthDifference = lengthDifference;
    }

    public UserEdit(int userId, int offset, int lengthDifference) {

        super(userId, PacketType.CURSOR_MOVE);

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
                "userId=" + super.getUserId() +
                ", editText='" + editText + '\'' +
                ", offset=" + offset +
                ", lengthDifference=" + lengthDifference +
                '}';
    }
}
