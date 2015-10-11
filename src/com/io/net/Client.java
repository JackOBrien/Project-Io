package com.io.net;

import com.io.gui.UserEdit;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    private Socket clientSocket;

    public Client() throws IOException {
        clientSocket = new Socket("127.0.0.1", Server.PORT);
    }

    public void sendUserEdit(UserEdit userEdit) {
        try {

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectOutputStream.writeObject(userEdit);

        } catch (Exception e) {
            System.err.println("Client Error: " + e.getMessage());
            System.err.println("Localized: " + e.getLocalizedMessage());
            System.err.println("Stack Trace: " + e.getStackTrace());
        }
    }

}
