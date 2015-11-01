package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.io.net.Server;

public class StartIo extends AnAction {

    public void actionPerformed(AnActionEvent e) {

        final Editor editor = e.getData(LangDataKeys.EDITOR);
        new Server(editor);
    }
}
