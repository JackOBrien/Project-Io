package com.io.gui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.io.domain.CursorMovement;
import com.io.domain.UserEdit;

import java.awt.*;
import java.nio.file.Paths;
import java.util.Hashtable;

public class StartReceiving {

    StartListening listener;

    public StartReceiving(Project project, StartListening listener) {
        this.listener = listener;
    }

    public void applyUserEditToDocument(Project project, UserEdit userEdit) {

        if (project.isDisposed()) {
            return;
        }

        System.out.println(userEdit.getFilePath());

        String filePath = Paths.get(project.getBasePath(), userEdit.getFilePath()).toString();
        System.out.println(filePath);

        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);

        if (file == null || !ProjectFileIndex.SERVICE.getInstance(project).isInSource(file)) {
            System.out.println("Could not find file.");
            return;
        }

        // TODO: Remove this. -- TESTING ONLY --
        System.out.println("Applying edit from: " + userEdit.getUserId());

        //Apply userEdit
        WriteCommandAction.runWriteCommandAction(project, () -> {
            synchronized (this) {
                listener.isListening = false;

                Document document = FileDocumentManager.getInstance().getDocument(file);
                if (document == null) {
                    System.out.println("Failed to find document.");
                    listener.isListening = true;
                    return;
                }

                Editor[] editors = EditorFactory.getInstance().getEditors(document);
                Editor editor = null;
                int cursorPosition = 0;

                if (editors.length > 0) {
                    editor = editors[0];
                    cursorPosition = editor.getCaretModel().getOffset();
                }

                Hashtable<Document, IOPatcher> patchers = IOProject.getInstance(project).patchers;
                IOPatcher patcher = patchers.get(document);
                if (patcher == null) {
                    //First time editing, so make patcher with current text as the base
                    String text = document.getText();
                    patcher = new IOPatcher(text);
                    patchers.put(document, patcher);
                    System.out.println("Created new patcher for: " + filePath);
                }

                String oldFragment = userEdit.getOldFragment();
                String newFragment = userEdit.getNewFragment();
                int position = userEdit.getPosition();
                int timestamp = userEdit.getTimestamp();

                if (timestamp < 0) {
                    System.out.println("Error! Timestamp was not set by the server!");
                    listener.isListening = true;
                    return;
                }

                patcher.addPatch(oldFragment, newFragment, position, timestamp);
                patcher.rebaseFile();

                String newText = patcher.buildFile();
                int diff = newText.length() - document.getTextLength();

                document.setText(newText);

                //If editor is open, try to keep cursor still
                if (editor != null) {
                    int lastChangePosition = patcher.getLastChangePosition();

                    if (cursorPosition > lastChangePosition) {
                        cursorPosition += diff;
                        System.out.println("Moving cursor " + diff + " positions");
                    }
                    else {
                        System.out.println("Keeping cursor still at " + cursorPosition);
                    }

                    try {
                        editor.getCaretModel().moveToOffset(cursorPosition);
                    }
                    catch (Exception ex) {
                        System.out.println("An error occurred when moving cursor after applying a patch");
                    }
                }

                listener.isListening = true;
            }
        });
    }

    public void applyHighlightToDocument(Project project, CursorMovement cursorMovement) {

        if (project.isDisposed()) {
            return;
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String filePath = Paths.get(project.getBasePath(), cursorMovement.getFilePath()).toString();

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

            int start = cursorMovement.getPosition();
            int end = start + 1;
            int textLength = document.getTextLength();

            if (end > textLength) {
                end = textLength;
            }
            if (start >= textLength) {
                start = textLength - 1;
            }

            for (Editor e : editors) {
                for (RangeHighlighter highlighter : e.getMarkupModel().getAllHighlighters()) {
                    highlighter.dispose();
                }

                e.getMarkupModel().addRangeHighlighter(start, end,
                        HighlighterLayer.ERROR + 100, attributes, HighlighterTargetArea.EXACT_RANGE);
            }
        });
    }
}
