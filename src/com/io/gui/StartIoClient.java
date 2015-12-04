package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;

public class StartIoClient extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(LangDataKeys.PROJECT);
        new Client(project);
    }

}