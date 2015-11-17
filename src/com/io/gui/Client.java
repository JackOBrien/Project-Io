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

    public Client (final Project currentProject) {

        userListWindow = new UserListWindow();

        try {
            connector = new Connector();
        } catch(IOException ex) {
            System.out.println("Failed to connect to server");
            return;
        }

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
            public void applyNewFiles(FileTransfer fileTransfer){
                try {

                    //Get parent directory
                    String dir = Paths.get(currentProject.getBasePath()).getParent().toString();

                    //For testing on one computer
                    String parent = Paths.get(dir).getParent().toString();
                    dir = Paths.get(parent, "IdeaProjectIO", fileTransfer.getProjectName()).toString();

                    System.out.println("Saving project to: " + dir);

                    UnZip unZip = new UnZip(fileTransfer.getContent(), dir);
                    unZip.unZipIt();


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
                        catch (JDOMException ex) {
                            ex.printStackTrace();
                        }
                        catch (InvalidDataException ex) {
                            System.out.println("Invalid project!");
                        }

                        if (newProject != null) {
                            System.out.println("Opening project.");
                            pm.openProject(newProject);
                            loadProject(newProject);
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
            public void applyNewFiles(FileTransfer fileTransfer) {

            }

            @Override
            public void applyConnectionUpdate(ConnectionUpdate connectionUpdate) {

            }
        });

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