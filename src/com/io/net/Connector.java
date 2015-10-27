package com.io.net;

import com.io.domain.Login;
import com.io.domain.Packet;
import com.io.domain.PacketType;
import com.io.domain.UserEdit;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Connector implements Runnable {

    private Socket socket;
    private List<ConnectorEvent> listeners;

    public Connector() throws IOException {
        socket = new Socket("127.0.0.1", Server.PORT);
        listeners = new ArrayList<>();
    }

    public Connector(Socket socket, List<ConnectorEvent> listeners) {
        this.socket = socket;
        this.listeners = listeners;
    }

    public void addEventListener(ConnectorEvent connectorEvent) {
        listeners.add(connectorEvent);
    }

    @Override
    public void run() {
        System.out.println("Connection Established...");

        while (true) {

            try {
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

                Packet packet = (Packet) inputStream.readObject();

                if (packet.getPacketType() == PacketType.DOCUMENT_EDIT.id()) {

                    UserEdit userEdit = (UserEdit) packet;

                    for (ConnectorEvent connectorEvent : listeners) {
                        connectorEvent.applyUserEdit(userEdit);
                    }
                }
            }
            catch (IOException ex) {

            }
            catch (ClassNotFoundException ex) {

            }

        }
    }

    public void sendObject(Serializable object) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(object);
            System.out.println("Sent object: " + object.getClass());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendUserEdit(UserEdit userEdit) {
        sendObject(userEdit);
    }

    public int login(String username) {
        Login login = new Login(username);
        sendObject(login);
        //TODO: Get server

        return -1;
    }
}
