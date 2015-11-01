package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.io.domain.Login;
import com.io.domain.UserEdit;
import com.io.net.Connector;
import com.io.net.Server;
import com.io.net.ConnectorEvent;
import com.io.net.ServerConnection;

import java.util.ArrayList;

public class StartIo extends AnAction {

    public StartListening listening;
    public StartReceiving receiving;

    public void actionPerformed(AnActionEvent e) {

        final Editor editor = e.getData(LangDataKeys.EDITOR);

        listening = new StartListening(editor);
        receiving = new StartReceiving(editor, listening);

        final Server server = new Server();

        server.addListener(new ConnectorEvent() {
            @Override
            public void applyUserEdit(UserEdit userEdit) {
                receiving.applyUserEditToDocument(editor, userEdit);
                server.broadcastEdit(userEdit);
            }

            @Override
            public void applyUserId(Login login, Connector connector) {
                ServerConnection serverConnection = server.findServerConnection(connector);
                login.setUserId(serverConnection.getUserId());
                serverConnection.setUsername(login.getUsername());
                System.out.println("Sending login with user id " + login.getUserId());
                server.sendLogin(login);
            }
        });

        listening.addEventListener(new EditorEvent() {
            @Override
            public void sendChange(UserEdit userEdit) {
                server.broadcastEdit(userEdit);
            }
        });

        (new Thread(server)).start();
        System.out.println("Server started");

    }

}
