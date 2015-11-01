package com.io.domain;

import java.io.File;
import java.nio.file.Files;

public class FileTransfer extends Packet{
    byte[] content;
    public FileTransfer(int userId){
        super(userId, PacketType.FILE_TRANSFER);
        this.content = null;
    }
    public FileTransfer(int userId, String filePath) throws java.io.IOException {
        super(userId, PacketType.FILE_TRANSFER);

        File file = new File(filePath);

        try {
            this.content = Files.readAllBytes(file.toPath());
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public void writeFile(String dest) throws java.io.IOException {
        File file = new File(dest);
        Files.write(file.toPath(), this.content);
    }
}
