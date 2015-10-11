package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.io.net.Server;
import com.io.net.ServerListener;

import java.util.ArrayList;

public class StartIo extends AnAction {

    ArrayList<Editor> editors;
    StartReceiving receiving;

    public void actionPerformed(AnActionEvent e) {
        editors = new ArrayList<>();
        editors.add(e.getData(LangDataKeys.EDITOR));

        StartListening listening = new StartListening();

        listening.actionPerformed(e);

        receiving = new StartReceiving(listening.getDocumentListener());

        Server server = new Server();

        server.addListener(new ServerListener() {
            @Override
            public void applyUserEdit(UserEdit userEdit) {
                Editor editor = e.getData(LangDataKeys.EDITOR);
                receiving.applyUserEditToDocument(editor, userEdit);
            }
        });

        (new Thread(server)).start();
        System.out.println("Server started");
    }

}
