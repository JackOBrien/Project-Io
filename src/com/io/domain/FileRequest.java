package com.io.domain;

public class FileRequest extends Packet{
    public FileRequest(){
        super(PacketType.FILE_TRANSFER.id());
    }
}
