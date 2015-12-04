package com.io.domain;

public class ChatMessage extends Packet {

    private String message;

    public ChatMessage(int userId, String message) {
        super(userId, PacketType.CHAT_MESSAGE);

        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
