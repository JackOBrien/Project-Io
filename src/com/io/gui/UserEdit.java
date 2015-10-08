package com.io.gui;

/**
 * Created by Jack on 9/28/2015.
 */
public class UserEdit {
    private int userId;

    private String editText;

    private int lengthDifference;

    private int offset;

    public UserEdit(int userId, String editText, int offset) {
        this(userId, editText, offset, editText.length());
    }

    public UserEdit(int userId, String editText, int offset, int lengthDifference) {
        this.userId = userId;
        this.editText = editText;
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
