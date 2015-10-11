package com.io.net;

import com.io.gui.UserEdit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server implements Runnable {

    public final static int PORT = 49578;

    private List<ConnectorEvent> listeners = new ArrayList<>();
    private List<Connector> workers = new ArrayList<>();

    public void startServer() {

        ExecutorService executorService = Executors.newCachedThreadPool();

        try {
            ServerSocket serverSocket;
            try {
                serverSocket = new ServerSocket(PORT);
            }
            catch (IOException ex) {
                serverSocket = new ServerSocket(PORT + 1);
            }

            System.out.println("Listening for clients");

            while (true) {
                // Create the Client Socket
                Socket clientSocket = serverSocket.accept();

                Connector serverWorker = new Connector(clientSocket, listeners);
                workers.add(serverWorker);
                executorService.execute(serverWorker);
            }

        } catch (Exception e) {
            System.err.println("Server Error: " + e.getMessage());
            System.err.println("Localized: " + e.getLocalizedMessage());
            System.err.println("Stack Trace: " + e.getStackTrace());
            System.err.println("To String: " + e.toString());
        }
    }

    public void addListener(ConnectorEvent connectorEvent) {
        listeners.add(connectorEvent);
    }

    public void broadcastEdit(UserEdit userEdit) {
        for (Connector worker : workers) {
            worker.sendUserEdit(userEdit);
        }
    }

    @Override
    public void run() {
        startServer();
    }


}
