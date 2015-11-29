package com.io.gui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.io.domain.UserEdit;

import java.awt.*;
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

    public void applyHighlightToDocument(Editor editor, UserEdit userEdit) {
        Project project = editor.getProject();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String filePath = Paths.get(project.getBasePath(), userEdit.getFilePath()).toString();

            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);

            if (file == null || !ProjectFileIndex.SERVICE.getInstance(project).isInSource(file)) {
                System.out.println("Could not find file.");
                return;
            }

            Document document = FileDocumentManager.getInstance().getDocument(file);
            Editor[] editors = EditorFactory.getInstance().getEditors(document, project);

            final TextAttributes attributes = new TextAttributes();
            final JBColor color = JBColor.BLUE;

            attributes.setEffectColor(color);
            attributes.setEffectType(EffectType.SEARCH_MATCH);
            attributes.setBackgroundColor(color);
            attributes.setForegroundColor(Color.WHITE);

            int offset = userEdit.getOffset();

            for (Editor e : editors) {
                for (RangeHighlighter highlighter : e.getMarkupModel().getAllHighlighters()) {
                    highlighter.dispose();
                }

                e.getMarkupModel().addRangeHighlighter(offset, offset + 1,
                        HighlighterLayer.ERROR + 100, attributes, HighlighterTargetArea.EXACT_RANGE);
            }
        });
    }
}
