package com.io.net;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.io.domain.*;
import com.io.gui.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private int followingUserId;

    private Thread executionThread = null;

    public Server(final Project project) {

        listening = new StartListening(project);
        receiving = new StartReceiving(project, listening);

        followingUserId = -1; //Not following a user initially

        ActionListener followListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                followingUserId = Integer.parseInt(e.getActionCommand());
            }
        };


        userId = INITIAL_USER_ID;
        username = JOptionPane.showInputDialog("Please enter a username");
        if (username.isEmpty()) {
            username = INITIAL_USER_NAME;
        }

        userListWindow = new UserListWindow(project, followListener);
        userListWindow.addUser(new UserInfo(userId, username), true);


        //Broadcast chat messages from server user
        userListWindow.onNewChatMessage((message) -> {
            System.out.println("User [" + username + "] says: " + message);

            ChatMessage chatMessage = new ChatMessage(userId, message);

            String output = username + ": " + message;
            userListWindow.appendChatMessage(output);

            //Sent to all clients
            for (ServerConnection connection : connections) {
                connection.getConnector().sendChatMessage(chatMessage);
            }
        });

        IOProject.getInstance(project).addProjectClosedListener(() -> {
            try {
                Logout logout = new Logout(userId);
                for (ServerConnection connection : connections) {
                    connection.getConnector().sendLogout(logout);
                    connection.getConnector().disconnect();
                    System.out.println("Closed connection to client");
                }
            }
            catch (IOException ex) {
                System.out.println("Failed to disconnect from clients");
            }

            this.executionThread.interrupt();
        });

        this.addListener(new ConnectorEvent() {
            @Override
            public void applyUserEdit(UserEdit userEdit) {
                receiving.applyUserEditToDocument(project, userEdit);

                String editorsName = "<Not Found>";

                for (ServerConnection connection : connections) {
                    if (connection.getUserId() == userEdit.getUserId()) {
                        editorsName = connection.getUsername();
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
            public void applyLogout(Logout logout, Connector connector) {
                logout(connector);
            }


            @Override
            public void applyConnectionUpdate(ConnectionUpdate connectionUpdate) {
                //Should never get one
            }

            @Override
            public void applyCursorMove(CursorMovement cursorMovement) {
                receiving.applyHighlightToDocument(project, cursorMovement, followingUserId);
                broadcastCursorMovement(cursorMovement);
            }

            @Override
            public void applyNewFiles(FileTransfer fileTransferRequest){
                try {
                    String dir = project.getBasePath();

                    ApplicationManager.getApplication().getInvokator().invokeLater(() -> {
                        byte[] content;

                        ApplicationManager.getApplication().saveAll();
                        System.out.println("Flushed changes to disk");

                        try {
                            content = Zip.zip(dir);
                        }
                        catch (IOException ex) {
                            System.out.println("Failed to zip project files.");
                            return;
                        }

                        FileTransfer fileTransfer = new FileTransfer(userId, project.getName(), content);

                        sendFileTransfer(fileTransferRequest.getUserId(), fileTransfer);
                    });
                } catch (Exception e){
                    e.getMessage();
                    e.printStackTrace();
                }
            }

            @Override
            public void applyChatMessage(ChatMessage chatMessage, Connector connector) {

                //Build message output
                ServerConnection connection = findServerConnection(connector);
                String output = connection.getUsername() + ": " + chatMessage.getMessage();

                //Print chat message to screen
                userListWindow.appendChatMessage(output);

                //Broadcast message to all other clients
                for (ServerConnection conn : connections) {
                    if (conn.getUserId() != chatMessage.getUserId()) {
                        conn.getConnector().sendChatMessage(chatMessage);
                    }
                }
            }

            @Override
            public void onDisconnect(Connector connector) {

                ServerConnection serverConnection = findServerConnection(connector);
                int userId = serverConnection.getUserId();
                String username = serverConnection.getUsername();

                System.out.println("[" + username + "](" + userId + ") has disconnected from server");
            }

            @Override
            public void onSendFail(Connector connector) {
                System.out.println("Write Fail. Disconnecting from client");

                ApplicationManager.getApplication().getInvokator().invokeLater(() -> {
                    logout(connector);
                });
            }
        });

        listening.addEventListener(new EditorEvent() {
            @Override
            public void sendChange(UserEdit userEdit) {
                userEdit.setUserId(userId);
                broadcastEdit(userEdit);
            }

            @Override
            public void sendCursorMovement(CursorMovement cursorMovement) {
                cursorMovement.setUserId(userId);
                broadcastCursorMovement(cursorMovement);
            }
        });

        this.executionThread = new Thread(this);
        this.executionThread.start();

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

    public void broadcastCursorMovement(CursorMovement cursorMovement) {
        for (ServerConnection connection: connections) {
            if (connection.getUserId() != cursorMovement.getUserId()) {
                connection.getConnector().sendCursorMovement(cursorMovement);
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

    private void logout(Connector connector) {
        ServerConnection serverConnection = findServerConnection(connector);

        if (serverConnection == null) {
            System.out.println("Could not find server connection");
            return;
        }

        int userId = serverConnection.getUserId();
        String username = serverConnection.getUsername();

        //Remove all connection objects belonging to the user
        connections.remove(serverConnection);
        connectionLookup.remove(connector);
        userListWindow.removeUserById(userId);

        System.out.println("[" + username + "](" + userId + ") disconnected");

        Logout logout = new Logout(userId);

        //Broadcast logout to other clients
        for (ServerConnection connection : connections) {
            connection.getConnector().sendLogout(logout);
        }
    }

    public void sendFileTransfer(int userId, FileTransfer fileTransfer) {
        for (ServerConnection connection : connections) {
            if (connection.getUserId() == userId) {
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
