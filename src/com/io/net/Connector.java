package com.io.net;

import com.io.domain.Login;
import com.io.domain.Packet;
import com.io.domain.PacketType;
import com.io.domain.UserEdit;
import com.io.gui.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.io.domain.PacketType.DOCUMENT_EDIT;
import static com.io.domain.PacketType.LOGIN;

public class Connector implements Runnable {

    private Socket socket;
    private List<ConnectorEvent> listeners;

    //Used only by the server, because the server has multiple connectors
    //These probably should not even be in here
    private int userId;
    private String username;

    /** Client Constructor */
    public Connector() throws IOException {
        socket = new Socket("127.0.0.1", Server.PORT);
        listeners = new ArrayList<>();
    }

    /** Server Constructor */
    public Connector(int userId, Socket socket, List<ConnectorEvent> listeners) {
        this.userId = userId;
        this.username = Client.INITIAL_USER_NAME;

        this.socket = socket;
        this.listeners = listeners;
    }

    public int getUserId() {
        return userId;
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

                /* Looks for packets containing a change to the documents contents */
                if (packet.getPacketType() == DOCUMENT_EDIT.id()) {

                    UserEdit userEdit = (UserEdit) packet;

                    for (ConnectorEvent connectorEvent : listeners) {
                        connectorEvent.applyUserEdit(userEdit);
                    }
                }

                /* Looks for packets signifying a new Client is logging on */
                else if (packet.getPacketType() == LOGIN.id()) {

                    Login login = (Login) packet;

                    // Logs this user's username
                    username = login.getUsername();

                    for (ConnectorEvent connectorEvent : listeners) {
                        connectorEvent.applyUserId(login, this);
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

}
