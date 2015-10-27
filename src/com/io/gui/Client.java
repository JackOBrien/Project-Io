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

    public Client (Editor editor) {

        setupListeners(editor);

        username = JOptionPane.showInputDialog("Please enter a username");
        System.out.println(username);

        final Connector connector;

        try {
            connector = new Connector();
        } catch(IOException ex) {
            System.out.println("Failed to connect to server");
            return;
        }



        listening.addEventListener(new EditorEvent() {
            @Override
            public void sendChange(UserEdit userEdit) {
                connector.sendUserEdit(userEdit);
            }
        });

        connector.addEventListener(new ConnectorEvent() {
            @Override
            public void applyUserEdit(UserEdit userEdit) {
                receiving.applyUserEditToDocument(editor, userEdit);
            }
        });

        (new Thread(connector)).start();
    }

    private void setupListeners(Editor editor) {
        listening = new StartListening(editor);
        receiving = new StartReceiving(editor, listening.getDocumentListener());
    }

    private void login() {

    }
}
