package com.io.gui;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.wm.WindowManager;
import com.io.domain.*;
import com.io.net.Connector;
import com.io.net.ConnectorEvent;
import com.io.net.Server;
import com.io.net.UnZip;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Client {

    public static final int INITIAL_USER_ID = -1;
    public static final String INITIAL_USER_NAME = "user";

    public StartListening listening;  //TODO: Make private
    public StartReceiving receiving;
    private Connector connector;

    private int userId;
    private String username;

    private UserListWindow userListWindow;
    private Project project;

    public Client () {

        userListWindow = new UserListWindow();

        try {
            String ip = askForIP();
            connector = new Connector(ip);
        } catch(IOException ex) {
            System.out.println("Failed to connect to server");
            return;
        }

        //Send chat messages to server
        userListWindow.onNewChatMessage((message) -> {
            System.out.println("User [" + username + "] says: " + message);

            ChatMessage chatMessage = new ChatMessage(userId, message);

            String output = username + ": " + message;
            userListWindow.appendChatMessage(output);

            connector.sendChatMessage(chatMessage);
        });

        connector.addEventListener(new ConnectorEvent() {
            @Override
            public void applyUserEdit(UserEdit userEdit) {

            }

            @Override
            public void applyUserId(Login login, Connector connector) {
                userId = login.getUserId();
                username = login.getUsername();
                userListWindow.addUser(new UserInfo(userId, username));
                System.out.println("User id is now " + userId);
                requestFiles();
            }

            @Override
            public void applyLogout(Logout logout, Connector connector) {
                userListWindow.removeUserById(logout.getUserId());

                //If server logged out, we are done
                if (logout.getUserId() == Server.INITIAL_USER_ID) {

                    try {
                        connector.disconnect();
                    }
                    catch (IOException ex) {
                        System.out.println("Failed to disconnect from server");
                    }

                    ApplicationManager.getApplication().invokeLater(() -> {
                        ProjectManagerEx.getInstance().closeProject(project);
                    });
                }
            }

            @Override
            public void applyNewFiles(FileTransfer fileTransfer){
                try {

                    String dir = "";

                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fileChooser.setDialogTitle("Select a location to save the project folder in");
                    int ret = fileChooser.showDialog(WindowManager.getInstance().findVisibleFrame().getGlassPane(), "Choose Folder");
                    if (ret == JFileChooser.APPROVE_OPTION) {
                        String chosenDirectory = fileChooser.getSelectedFile().getAbsolutePath();
                        dir = Paths.get(chosenDirectory, fileTransfer.getProjectName()).toString();
                    }
                    else {
                        logout();
                        return;
                    }

                    System.out.println("Saving project to: " + dir);

                    try {
                        UnZip.unZip(fileTransfer.getContent(), dir);
                    }
                    catch (IOException ex) {
                        System.out.println("Failed to unzip project.");
                        logout();
                        return;
                    }

                    final String dircopy = dir;
                    ApplicationManager.getApplication().invokeLater(() -> {
                        ProjectManagerEx pm = ProjectManagerEx.getInstanceEx();
                        Project newProject = null;
                        System.out.println("Loading project: " + dircopy);

                        try {
                            newProject = pm.loadProject(dircopy);
                        }
                        catch (IOException ex) {
                            System.out.println("Failed to open project");
                        }

                        if (newProject != null) {
                            System.out.println("Opening project.");
                            pm.openProject(newProject);
                            loadProject(newProject);
                        }
                    });

                } catch (Exception e) {
                    e.getMessage();
                    e.printStackTrace();
                }
            }

            @Override
            public void applyConnectionUpdate(ConnectionUpdate connectionUpdate) {
                ArrayList<UserInfo> users = connectionUpdate.getUserList();

                System.out.println("Client received " + users.size());

                for (UserInfo user : users) {
                    userListWindow.addUser(user);
                }
            }

            @Override
            public void applyChatMessage(ChatMessage chatMessage, Connector connector) {

                //Build message output
                String username = userListWindow.getUsernameById(chatMessage.getUserId());
                String output = username + ": " + chatMessage.getMessage();

                //Print chat message to screen
                userListWindow.appendChatMessage(output);

            }

            @Override
            public void onDisconnect(Connector connector) {
                System.out.println("Client has disconnected");
            }

            @Override
            public void applyCursorMove(CursorMovement cursorMovement) {

            }
        });

        (new Thread(connector)).start();

        login();
    }

    private void loadProject(Project project) {
        this.project = project;

        listening = new StartListening(project);
        receiving = new StartReceiving(project, listening);

        userListWindow.attachToProject(project);

        listening.addEventListener(new EditorEvent() {
            @Override
            public void sendChange(UserEdit userEdit) {
                userEdit.setUserId(userId);
                connector.sendUserEdit(userEdit);
            }

            @Override
            public void sendCursorMovement(CursorMovement cursorMovement) {
                cursorMovement.setUserId(userId);
                connector.sendCursorMovement(cursorMovement);
            }
        });

        IOProject.getInstance(project).addProjectClosedListener(() -> {
            logout();
        });

        connector.addEventListener(new ConnectorEvent() {
            @Override
            public void applyUserEdit(UserEdit userEdit) {
                userEdit.setUserId(userId);
                receiving.applyUserEditToDocument(project, userEdit);
            }

            @Override
            public void applyUserId(Login login, Connector connector) {

            }

            @Override
            public void applyLogout(Logout logout, Connector connector) {

            }

            @Override
            public void applyNewFiles(FileTransfer fileTransfer) {

            }

            @Override
            public void applyConnectionUpdate(ConnectionUpdate connectionUpdate) {

            }

            @Override
            public void applyChatMessage(ChatMessage chatMessage, Connector connector) {

            }

            @Override
            public void onDisconnect(Connector connector) {

            }

            @Override
            public void applyCursorMove(CursorMovement cursorMovement) {
//                if (userId == cursorMovement.getUserId()) {
//                    return;
//                }

                receiving.applyHighlightToDocument(project, cursorMovement);
            }
        });

    }

    private String askForIP() {
        String ip = JOptionPane.showInputDialog("Please enter the server IP");

        return ip;
    }

    private void login() {
        username = JOptionPane.showInputDialog("Please enter a username");

        Login login = new Login(INITIAL_USER_ID, username);
        connector.sendObject(login);
    }

    private void logout() {
        try {
            Logout logout = new Logout(userId);
            connector.sendLogout(logout);
            connector.disconnect();
            System.out.println("Closed connection to server");
        }
        catch (IOException ex) {
            System.out.println("Failed to disconnect from server");
        }
    }

    private void requestFiles(){
        FileTransfer fileTransferRequest = new FileTransfer(this.userId);

        connector.sendFileTransferRequest(fileTransferRequest);
    }
}