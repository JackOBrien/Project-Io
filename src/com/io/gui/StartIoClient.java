package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.io.net.Connector;
import com.io.net.ConnectorEvent;

import java.io.IOException;
import java.util.ArrayList;

public class StartIoClient extends AnAction {

    ArrayList<Editor> editors;
    public StartListening listening;
    public StartReceiving receiving;

    public void actionPerformed(AnActionEvent e) {
        editors = new ArrayList<>();
        Editor editor = e.getData(LangDataKeys.EDITOR);
        editors.add(editor);

        listening = new StartListening(editor);
        receiving = new StartReceiving(editor, listening.getDocumentListener());

        Connector connector;

        try {
            connector = new Connector();
        }
        catch(IOException ex) {
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

}