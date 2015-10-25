package com.io.domain;

public class FileRequest extends Packet{
    public FileRequest(){
        super(PacketType.DOCUMENT_EDIT.id());
    }
}
