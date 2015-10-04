package com.gui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

import java.util.Stack;

/**
 * Created by Mark Knapp on 10/4/2015.
 */
public class StartReceiving implements Runnable {

    Editor editor;

    Stack<UserEdit> fakeEdits;

    public StartReceiving(Editor editor) {
        this.editor = editor;
        fakeEdits = new Stack<>();

        //Inserting text
        fakeEdits.add(new UserEdit(2, "This is a fake edit", 0));
    }

    private static void applyUserEditToDocument(Editor editor, UserEdit userEdit) {

        Project project = editor.getProject();
        Document document = editor.getDocument();

        //Make sure userEdit is not my id
        //...

        //Apply userEdit
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.insertString(userEdit.getOffset(), userEdit.getEditText());
        });

    }


    @Override
    public void run() {

        while(true) {
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException ex) {
                System.out.println("Sleep interrupted.");
                break;
            }

            System.out.println("Checking for user edits...");

            if (fakeEdits.empty()) {
                System.out.println("No changes found.");
                continue;
            }

            UserEdit userEdit = fakeEdits.pop();

            applyUserEditToDocument(editor, userEdit);

        }

    }

    public void start() {
        (new Thread(this)).start();
    }

}
