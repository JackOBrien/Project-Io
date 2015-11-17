package com.io.domain;

public class FileTransfer extends Packet {
    byte[] content;
    public FileTransfer(int userId) {
        super(userId, PacketType.FILE_TRANSFER);
        this.content = null;
    }

    public FileTransfer(int userId, byte[] content) {
        super(userId, PacketType.FILE_TRANSFER);
        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }

}
