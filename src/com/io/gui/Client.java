package com.io.gui;


import com.intellij.openapi.editor.Editor;
import com.io.domain.FileTransfer;
import com.io.domain.Login;
import com.io.domain.UserEdit;
import com.io.net.Connector;
import com.io.net.ConnectorEvent;
import com.io.net.UnZip;

import javax.swing.*;
import java.io.IOException;

public class Client {

    public static final int INITIAL_USER_ID = -1;

    public static final String INITIAL_USER_NAME = "user";

    public StartListening listening;  //TODO: Make private

    public StartReceiving receiving;

    private String username;

    private int userId;

    private Connector connector;

    public Client (final Editor editor) {

        listening = new StartListening(editor);
        receiving = new StartReceiving(editor, listening);

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
                receiving.applyUserEditToDocument(editor, userEdit);
            }

            @Override
            public void applyUserId(Login login, Connector connector) {
                userId = login.getUserId();
                username = login.getUsername();
                System.out.println(editor.getProject().getName() + ": User id is now " + userId);
                requestFiles();
            }

            @Override
            public void applyNewFiles(FileTransfer fileTransfer){
                try {
                    String zipFile = editor.getProject().getBasePath() + "/test.zip";
                    String dir = editor.getProject().getBasePath();
                    fileTransfer.writeFile(zipFile);

                    UnZip unZip = new UnZip(zipFile, dir);
                    unZip.unZipIt();

                    //TODO Still needs to update the current project to the new project from the new dir
                }catch (Exception e){
                    e.getMessage();
                    e.printStackTrace();
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
