package com.io.net;

import com.io.domain.FileTransfer;
import com.io.domain.Login;
import com.io.domain.Packet;
import com.io.domain.UserEdit;
import com.io.domain.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.io.domain.PacketType.*;

public class Connector implements Runnable {

    private Socket socket;
    private List<ConnectorEvent> listeners;

    /** Client Constructor */
    public Connector() throws IOException {
        socket = new Socket("127.0.0.1", Server.PORT);
        listeners = new ArrayList<>();
    }

    /** Server Constructor */
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

                /* Looks for packets containing a change to the documents contents */
                if (packet.getPacketType() == DOCUMENT_EDIT) {

                    UserEdit userEdit = (UserEdit) packet;

                    for (ConnectorEvent connectorEvent : listeners) {
                        connectorEvent.applyUserEdit(userEdit);
                    }
                }

                /* Looks for packets signifying a new Client is logging on */
                else if (packet.getPacketType() == LOGIN) {

                    Login login = (Login) packet;

                    for (ConnectorEvent connectorEvent : listeners) {
                        connectorEvent.applyUserId(login, this);
                    }
                }

                /* Looks for packets signifying a new file to transfer */
                else if(packet.getPacketType() == FILE_TRANSFER) {

                    FileTransfer fileTransfer = (FileTransfer) packet;

                    for (ConnectorEvent connectorEvent : listeners) {
                        connectorEvent.applyNewFiles(fileTransfer);
                    }
                }

                /* Looks for packets signifying a connection update */
                else if (packet.getPacketType() == PacketType.CONNECTION_UPDATE) {

                    ConnectionUpdate connectionUpdate = (ConnectionUpdate) packet;

                    for (ConnectorEvent connectorEvent : listeners) {
                        connectorEvent.applyConnectionUpdate(connectionUpdate);
                    }
                }

                /* Looks for packets signifying a cursor was moved */
                else if (packet.getPacketType() == CURSOR_MOVE) {

                    UserEdit userEdit = (UserEdit) packet;

                    for (ConnectorEvent connectorEvent : listeners) {
                        connectorEvent.applyCursorMove(userEdit);
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendUserEdit(UserEdit userEdit) {
        sendObject(userEdit);
    }

    public void sendFileTransferRequest(FileTransfer fileTransfer) {
        sendObject(fileTransfer);
    }

    public void sendConnectionUpdate(ConnectionUpdate connectionUpdate) {
        sendObject(connectionUpdate);
    }

    public void sendLogin(Login login) {
        sendObject(login);
    }

}
