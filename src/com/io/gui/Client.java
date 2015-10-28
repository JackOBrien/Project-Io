package com.io.gui;


import com.intellij.openapi.editor.Editor;
import com.io.domain.UserEdit;
import com.io.net.Connector;
import com.io.net.ConnectorEvent;

import javax.swing.*;
import java.io.IOException;

public class Client {

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

        listening.addEventListener(userEdit -> {
            userEdit.setUserId(userId);
            connector.sendUserEdit(userEdit);
        });

        connector.addEventListener(userEdit -> {
            userEdit.setUserId(userId);
            receiving.applyUserEditToDocument(editor, userEdit);
        });

        (new Thread(connector)).start();
    }

    private void setupListeners(Editor editor) {
        listening = new StartListening(editor);
        receiving = new StartReceiving(editor, listening.getDocumentListener());
    }

    private void login() {
        username = JOptionPane.showInputDialog("Please enter a username");
        System.out.println(username);

//        userId = connector.login(username);

        //TODO: REMOVE THIS -- TESTING ONLY
        if (username.toLowerCase().startsWith("a")) {
            userId = 1;
        } else {
            userId = 2;
        }
    }
}
