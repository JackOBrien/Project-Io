package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;

public class StartIoClient extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(LangDataKeys.EDITOR);
        new Client(editor);
    }

}