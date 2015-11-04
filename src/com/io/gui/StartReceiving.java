package com.io.gui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.io.domain.UserEdit;

import java.nio.file.Paths;

public class StartReceiving {

    StartListening listener;

    public StartReceiving(Editor editor, StartListening listener) {
        this.listener = listener;
    }

    public void applyUserEditToDocument(Editor editor, UserEdit userEdit) {

        Project project = editor.getProject();

        System.out.println(userEdit.getFilePath());

        String filePath = Paths.get(project.getBasePath(), userEdit.getFilePath()).toString();
        System.out.println(filePath);

        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);

        if (file == null || !ProjectFileIndex.SERVICE.getInstance(project).isInSource(file)) {
            System.out.println("Could not find file.");
            return;
        }

        // TODO: Make sure userEdit is not my id
        //...

        // TODO: Remove this. -- TESTING ONLY --
        System.out.println("Applying edit from: " + userEdit.getUserId());

        //Apply userEdit
        WriteCommandAction.runWriteCommandAction(project, () -> {
            if (userEdit.getEditText() == null) {
                editor.getCaretModel().moveToOffset(userEdit.getOffset());
            }
            else {
                synchronized (this) {
                    listener.isListening = false;


                    Document document = FileDocumentManager.getInstance().getDocument(file);

                    if (document == null) {
                        System.out.println("Failed to find document.");
                    }
                    else {
                        System.out.println("Found document");
                    }

                    int diff = userEdit.getLengthDifference();
                    int offset = userEdit.getOffset();

                    try {
                        if (diff < 0) {
                            document.deleteString(offset, offset + (-1 * diff));
                        } else {
                            document.insertString(offset, userEdit.getEditText());
                        }
                    }
                    catch(NullPointerException ex) {
                        System.out.println("Failed to insert into document.");
                    }

                    listener.isListening = true;
                }
            }
        });

    }
}
