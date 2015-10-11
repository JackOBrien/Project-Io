package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.io.net.Client;

import java.io.IOException;
import java.util.ArrayList;

public class StartIoClient extends AnAction {

    ArrayList<Editor> editors;
    StartReceiving receiving;

    public void actionPerformed(AnActionEvent e) {
        editors = new ArrayList<>();
        editors.add(e.getData(LangDataKeys.EDITOR));

        StartListening listening = new StartListening();

        listening.actionPerformed(e);

        receiving = new StartReceiving(listening.getDocumentListener());

        try {
            listening.client = new Client();
        }
        catch(IOException ex) {
            System.out.println("Failed to connect to server");
        }
    }

}