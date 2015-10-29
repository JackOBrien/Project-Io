package com.io.gui;


import com.intellij.openapi.editor.Editor;
import com.io.domain.Login;
import com.io.domain.UserEdit;
import com.io.net.Connector;
import com.io.net.ConnectorEvent;

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

        setupListeners(editor);

        login();

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
                receiving.applyUserEditToDocument(editor, userEdit);
            }

            @Override
            public void applyUserId(Login login) {
                userId = login.getUserId();
            }
        });

        (new Thread(connector)).start();
    }

    private void setupListeners(Editor editor) {
        listening = new StartListening(editor);
        receiving = new StartReceiving(editor, listening.getDocumentListener());
    }

    private void login() {
        username = JOptionPane.showInputDialog("Please enter a username");

        connector.login(username);
    }
}
