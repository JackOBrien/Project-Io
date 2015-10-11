package com.io.net;

import com.io.gui.UserEdit;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;

public class ServerWorker implements Runnable {

    private Socket clientSocket;
    private List<ServerListener> listeners;

    public ServerWorker(Socket socket, List<ServerListener> listeners) {
        clientSocket = socket;
        this.listeners = listeners;
    }

    @Override
    public void run() {
        System.out.println("Socket Established...");

        while (true) {

            try {
                ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());

                UserEdit userEdit = (UserEdit)inFromClient.readObject();

                for (ServerListener serverListener : listeners) {
                    serverListener.applyUserEdit(userEdit);
                }
            }
            catch (IOException ex) {

            }
            catch (ClassNotFoundException ex) {

            }

        }
    }
}