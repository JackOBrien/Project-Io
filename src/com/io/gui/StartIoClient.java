package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.io.domain.UserEdit;
import com.io.net.Connector;
import com.io.net.ConnectorEvent;

import java.io.IOException;
import java.util.ArrayList;

public class StartIoClient extends AnAction {

    public void actionPerformed(AnActionEvent e) {

        Editor editor = e.getData(LangDataKeys.EDITOR);
        Client client = new Client(editor);


    }

}