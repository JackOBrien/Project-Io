package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.io.net.Server;

public class StartIoServer extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(LangDataKeys.PROJECT);
        new Server(project);
    }
}
