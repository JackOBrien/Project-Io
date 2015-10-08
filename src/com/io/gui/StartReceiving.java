package com.io.gui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.project.Project;

import java.util.Stack;

public class StartReceiving {

    DocumentListener documentListener;

    public StartReceiving(DocumentListener documentListener) {
        this.documentListener = documentListener;
    }

    public void applyUserEditToDocument(Editor editor, UserEdit userEdit) {

        Project project = editor.getProject();
        Document document = editor.getDocument();

        //Make sure userEdit is not my id
        //...

        //Apply userEdit
        WriteCommandAction.runWriteCommandAction(project, () -> {
            if (userEdit.getEditText() == null) {
                editor.getCaretModel().moveToOffset(userEdit.getOffset());
            }
            else {
                // Removes DocumentListener temporarily so inserting the UserEdit doesn't get resent.
                document.removeDocumentListener(documentListener);

                int diff = userEdit.getLengthDifference();
                int offset = userEdit.getOffset();

                if (diff < 0) {
                    document.deleteString(offset, offset + (-1 * diff));
                }
                else {
                    document.insertString(offset, userEdit.getEditText());
                }

                document.addDocumentListener(documentListener);
            }
        });

    }
}
