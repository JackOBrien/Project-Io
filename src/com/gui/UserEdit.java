package com.gui;

/**
 * Created by Jack on 9/28/2015.
 */
public class UserEdit {
    private int userId;

    private String editText;

    private int offset;

    public UserEdit(int userId, String editText, int offset) {
        this.userId = userId;
        this.editText = editText;
        this.offset = offset;
    }

    public String getEditText() {
        return this.editText;
    }

    public int getOffset() {
        return this.offset;
    }

    @Override
    public String toString() {
        return "UserEdit{" +
                "userId=" + userId +
                ", editText='" + editText + '\'' +
                ", offset=" + offset +
                '}';
    }
}
