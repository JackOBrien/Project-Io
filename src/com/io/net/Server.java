package com.io.net;

import com.intellij.openapi.project.Project;
import com.io.domain.FileTransfer;
import com.io.domain.ConnectionUpdate;
import com.io.domain.Login;
import com.io.domain.UserEdit;
import com.io.gui.*;

import javax.swing.*;
import java.io.File;
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

    public static final int INITIAL_USER_ID = 0;

    public static final String INITIAL_USER_NAME = "host";

    private String username;

    private int userId;

    public StartListening listening;
    public StartReceiving receiving;

    //Needs start at 1 because Server is 0
    private int nextClientId = 1;

    private List<ConnectorEvent> listeners = new ArrayList<>();
    private List<ServerConnection> connections = new ArrayList<>();
    private Hashtable<Connector, ServerConnection> connectionLookup = new Hashtable<>();

    private UserListWindow userListWindow;

    public Server(final Project project) {

        listening = new StartListening(project);
        receiving = new StartReceiving(project, listening);

        userId = INITIAL_USER_ID;
        username = JOptionPane.showInputDialog("Please enter a username");
        if (username.isEmpty()) {
            username = INITIAL_USER_NAME;
        }

        userListWindow = new UserListWindow(project);
        userListWindow.addUser(new UserInfo(userId, username));

        this.addListener(new ConnectorEvent() {
            @Override
            public void applyUserEdit(UserEdit userEdit) {
                receiving.applyUserEditToDocument(project, userEdit);

                String editorsName = "<Not Found>";

                for (ServerConnection s : connectionLookup.values()) {
                    if (s.getUserId() == userEdit.getUserId()) {
                        editorsName = s.getUsername();
                    }
                }

                System.out.println(" -- Server received edit from: " + editorsName);
                broadcastEdit(userEdit);
            }

            @Override
            public void applyUserId(Login login, Connector connector) {
                ServerConnection serverConnection = findServerConnection(connector);
                login.setUserId(serverConnection.getUserId());
                serverConnection.setUsername(login.getUsername());

                userListWindow.addUser(new UserInfo(login.getUserId(), login.getUsername()));

                sendCurrentUserList(serverConnection.getUserId(), connector);
                broadcastNewUser(serverConnection.getUserId(), serverConnection.getUsername());

                System.out.println("Sending login with user id " + login.getUserId());

                connector.sendLogin(login);
            }

            @Override
            public void applyConnectionUpdate(ConnectionUpdate connectionUpdate) {
                //Should never get one
            }

            @Override
            public void applyNewFiles(FileTransfer fileTransferRequest){
                try {
                    String zipFile = project.getBasePath() + "/test.zip";
                    String dir = project.getBasePath();

                    Zip zip = new Zip(dir, zipFile);
                    zip.generateFileList(new File(dir));
                    zip.zipIt(zipFile);

                    FileTransfer fileTransfer = new FileTransfer(fileTransferRequest.getUserId(), zipFile);

                    sendFileTransfer(fileTransfer);
                }catch (Exception e){
                    e.getMessage();
                    e.printStackTrace();
                }
            }
        });

        listening.addEventListener(new EditorEvent() {
            @Override
            public void sendChange(UserEdit userEdit) {
                userEdit.setUserId(userId);
                broadcastEdit(userEdit);
            }
        });

        (new Thread(this)).start();
        System.out.println("Server started");

    }

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
        for (ServerConnection connection: connections) {
            if (connection.getUserId() != userEdit.getUserId()) {
                connection.getConnector().sendUserEdit(userEdit);
            }
        }
    }

    public void sendCurrentUserList(int userId, Connector connector) {

        ArrayList<UserInfo> newUsers = new ArrayList<>();

        //Add self as user
        newUsers.add(new UserInfo(this.userId, this.username));

        //Add connections other than destination connections
        for (ServerConnection connection: connections) {
            if (connection.getUserId() != userId) {
                newUsers.add(new UserInfo(connection.getUserId(), connection.getUsername()));
            }
        }

        ConnectionUpdate connectionUpdate = new ConnectionUpdate(0, newUsers);
        connector.sendConnectionUpdate(connectionUpdate);
    }

    public void broadcastNewUser(int userId, String username) {
        ArrayList<UserInfo> newUsers = new ArrayList<UserInfo>();
        newUsers.add(new UserInfo(userId, username));
        ConnectionUpdate connectionUpdate = new ConnectionUpdate(0, newUsers);
        for (ServerConnection connection: connections) {
            if (connection.getUserId() != userId) {
                connection.getConnector().sendConnectionUpdate(connectionUpdate);
            }
        }
    }

    public void sendFileTransfer(FileTransfer fileTransfer) {
        int id = fileTransfer.getUserId();

        for (ServerConnection connection : connections) {
            if (connection.getUserId() == id) {
                connection.getConnector().sendObject(fileTransfer);
                break;
            }
        }
    }

    @Override
    public void run() {
        startServer();
    }


}
