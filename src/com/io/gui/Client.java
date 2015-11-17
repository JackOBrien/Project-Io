package com.io.gui;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.util.InvalidDataException;
import com.io.domain.FileTransfer;
import com.io.domain.ConnectionUpdate;
import com.io.domain.Login;
import com.io.domain.UserEdit;
import com.io.net.Connector;
import com.io.net.ConnectorEvent;
import com.io.net.UnZip;
import org.jdom.JDOMException;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Client {

    public static final int INITIAL_USER_ID = -1;

    public static final String INITIAL_USER_NAME = "user";

    public StartListening listening;  //TODO: Make private

    public StartReceiving receiving;

    private String username;

    private int userId;

    private Connector connector;

    private UserListWindow userListWindow;

    public Client (final Project project) {

        listening = new StartListening(project);
        receiving = new StartReceiving(project, listening);

        userListWindow = new UserListWindow(project);

        try {
            connector = new Connector();
        } catch(IOException ex) {
            System.out.println("Failed to connect to server");
            return;
        }

        listening.addEventListener(new EditorEvent() {
            @Override
            public void sendChange(UserEdit userEdit) {
                userEdit.setUserId(userId);
                connector.sendUserEdit(userEdit);
            }
        });

        connector.addEventListener(new ConnectorEvent() {
            @Override
            public void applyUserEdit(UserEdit userEdit) {
                userEdit.setUserId(userId);
                receiving.applyUserEditToDocument(project, userEdit);
            }

            @Override
            public void applyUserId(Login login, Connector connector) {
                userId = login.getUserId();
                username = login.getUsername();
                userListWindow.addUser(new UserInfo(userId, username));
                System.out.println(project.getName() + ": User id is now " + userId);
                requestFiles();
            }

            @Override
            public void applyNewFiles(FileTransfer fileTransfer){
                try {

                    //Get parent directory
                    String dir = Paths.get(project.getBasePath()).getParent().toString();

                    //For testing on one computer
                    dir = Paths.get(dir).getParent().toString() + File.separator + "IdeaProjectsClone";


                    UnZip unZip = new UnZip(fileTransfer.getContent(), dir);
                    unZip.unZipIt();


                    final String dircopy = dir;
                    ApplicationManager.getApplication().invokeLater(() -> {
                        ProjectManagerEx pm = ProjectManagerEx.getInstanceEx();
                        Project p = null;
                        System.out.println("Loading project: " + dircopy);

                        try {
                            p = pm.loadProject(dircopy);
                        }
                        catch (IOException ex) {
                            System.out.println("Failed to open project");
                        }
                        catch (JDOMException ex) {
                            ex.printStackTrace();
                        }
                        catch (InvalidDataException ex) {
                            System.out.println("Invalid project!");
                        }

                        if (p != null) {
                            System.out.println("Opening project.");
                            pm.openProject(p);
                        }
                    });

                    //TODO Still needs to update the current project to the new project from the new dir
                }catch (Exception e){
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
        });

        (new Thread(connector)).start();

        login();
    }

    private void login() {
        username = JOptionPane.showInputDialog("Please enter a username");

        Login login = new Login(INITIAL_USER_ID, username);
        connector.sendObject(login);
    }

    private void requestFiles(){
        FileTransfer fileTransferRequest = new FileTransfer(this.userId);

        connector.sendFileTransferRequest(fileTransferRequest);
    }
}