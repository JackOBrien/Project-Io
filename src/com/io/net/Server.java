package com.io.net;

import com.io.domain.Login;
import com.io.domain.UserEdit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server implements Runnable {

    public final static int PORT = 49578;

    private int nextClientId = 0;

    private List<ConnectorEvent> listeners = new ArrayList<>();
    private List<ServerConnection> connections = new ArrayList<>();
    private Hashtable<Connector, ServerConnection> connectionLookup = new Hashtable<>();

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

                Connector connector = new Connector(clientSocket, listeners);

                ServerConnection serverConnection = new ServerConnection(nextClientId++, connector);

                connectionLookup.put(connector, serverConnection);

                connections.add(serverConnection);
                executorService.execute(connector);
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

    public ServerConnection findServerConnection(Connector connector) {
        return connectionLookup.get(connector);
    }

    public void broadcastEdit(UserEdit userEdit) {
        for (ServerConnection connection : connections) {
            if (connection.getUserId() != userEdit.getUserId()) {
                connection.getConnector().sendUserEdit(userEdit);
            }
        }
    }

    public void sendLogin(Login login) {
        int id = login.getUserId();

        for (ServerConnection connection : connections) {
            if (connection.getUserId() == id) {
                connection.getConnector().sendObject(login);
                break;
            }
        }
    }

    @Override
    public void run() {
        startServer();
    }


}
