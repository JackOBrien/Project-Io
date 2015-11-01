package com.io.domain;

public class FileTransfer extends Packet{
    public FileTransfer(int userId){
        super(userId, PacketType.FILE_TRANSFER);
    }
}
