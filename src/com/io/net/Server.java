package com.io.net;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.io.domain.Login;
import com.io.domain.UserEdit;
import com.io.gui.EditorEvent;
import com.io.gui.StartListening;
import com.io.gui.StartReceiving;
import com.io.gui.UserListWindow;

import javax.swing.*;
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

    public Server(final Editor editor) {

        listening = new StartListening(editor);
        receiving = new StartReceiving(editor, listening);

        userId = INITIAL_USER_ID;
        username = JOptionPane.showInputDialog("Please enter a username");
        if (username.isEmpty()) {
            username = INITIAL_USER_NAME;
        }

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(editor.getProject());
        ToolWindow toolWindow = toolWindowManager.registerToolWindow("User List", true, ToolWindowAnchor.LEFT);

        UserListWindow userListWindow = new UserListWindow();

        Content content = ContentFactory.SERVICE.getInstance().createContent(userListWindow, "", true);
        toolWindow.getContentManager().addContent(content);

        userListWindow.addUser(username);

        this.addListener(new ConnectorEvent() {
            @Override
            public void applyUserEdit(UserEdit userEdit) {
                receiving.applyUserEditToDocument(editor, userEdit);

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
                userListWindow.addUser(login.getUsername());
                System.out.println("Sending login with user id " + login.getUserId());
                sendLogin(login);
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
