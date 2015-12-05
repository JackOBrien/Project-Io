package com.io.domain;

import com.io.gui.diff_match_patch.Patch;

import java.util.LinkedList;

public class UserEdit extends Packet {

    private String filePath;
    private LinkedList<Patch> patches;

    public UserEdit(int userId, String filePath, LinkedList<Patch> patches) {

        super(userId, PacketType.DOCUMENT_EDIT);

        this.filePath = filePath;
        this.patches = patches;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public LinkedList<Patch> getPatches() {
        return patches;
    }

    @Override
    public String toString() {
        return "UserEdit{" +
                "userId=" + super.getUserId() +
                ", filePath='" + filePath + "'" +
                "}";
    }
}
