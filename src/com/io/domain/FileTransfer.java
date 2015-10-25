package com.io.domain;

public class FileTransfer extends Packet{
    public FileTransfer(){
        super(PacketType.FILE_TRANSFER.id());
    }
}
