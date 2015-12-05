package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class StartIoClient extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        new Client();
    }

}