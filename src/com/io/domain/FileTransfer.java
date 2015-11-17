package com.io.domain;

public class FileTransfer extends Packet {

    String projectName;
    byte[] content;

    public FileTransfer(int userId) {
        this(userId, "", null);
    }

    public FileTransfer(int userId, String projectName, byte[] content) {
        super(userId, PacketType.FILE_TRANSFER);
        this.projectName = projectName;
        this.content = content;
    }

    public String getProjectName() {
        return projectName;
    }

    public byte[] getContent() {
        return content;
    }

}
