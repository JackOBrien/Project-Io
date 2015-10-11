package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.io.net.Server;
import com.io.net.ConnectorEvent;

import java.util.ArrayList;

public class StartIo extends AnAction {

    ArrayList<Editor> editors;
    public StartListening listening;
    public StartReceiving receiving;

    public void actionPerformed(AnActionEvent e) {

        editors = new ArrayList<>();
        Editor editor = e.getData(LangDataKeys.EDITOR);
        editors.add(editor);


        listening = new StartListening(editor);
        receiving = new StartReceiving(editor, listening.getDocumentListener());


        Server server = new Server();

        server.addListener(new ConnectorEvent() {
            @Override
            public void applyUserEdit(UserEdit userEdit) {
                receiving.applyUserEditToDocument(editor, userEdit);
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
