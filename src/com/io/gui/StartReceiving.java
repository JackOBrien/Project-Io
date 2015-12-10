package com.io.gui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.LogicalPosition;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StartReceiving {

    StartListening listener;

    private Map<Integer, Integer> cursorPositions;

    public StartReceiving(Project project, StartListening listener) {
        this.listener = listener;
        cursorPositions = new HashMap<>();
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
                    return;
                }

                Editor[] editors = EditorFactory.getInstance().getEditors(document);
                Editor editor = null;
                int cursorPosition = 0;

                if (editors.length > 0) {
                    editor = editors[0];
                    cursorPosition = editor.getCaretModel().getOffset();
                }

                try {
                    String currentText = document.getText();
                    IOPatcher patcher = new IOPatcher();

                    Object[] output = patcher.patch_apply(userEdit.getPatches(), currentText);
                    String newText = (String)output[0];
                    document.setText(newText);

                    //If editor is open, try to keep cursor still
                    if (editor != null) {

                        //Get the list of positions where the patch was applied
                        int[] patchPositions = (int[]) output[2];
                        int patchPosition = -1;

                        //Get the first non-negative position
                        for (int i = 0; i < patchPositions.length; i++) {
                            if (patchPositions[i] >= 0) {
                                patchPosition = patchPositions[i];
                                break;
                            }
                        }

                        //If we found a patch position and the cursor is after that position
                        //move the cursor position to whatever length the patch difference is
                        if (patchPosition >= 0 && cursorPosition > patchPosition) {
                            cursorPosition += newText.length() - currentText.length();
                        }

                        try {
                            editor.getCaretModel().moveToOffset(cursorPosition);
                        }
                        catch (Exception ex) {
                            System.out.println("An error occurred when moving cursor after applying a patch");
                        }
                    }
                }
                catch(NullPointerException ex) {
                    System.out.println("Failed to insert into document.");
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

            if (document == null) {
                System.out.println("Could not find document to highlight");
                return;
            }

            int textLength = document.getTextLength();

            if (textLength == 0) {
                return;
            }

            Editor[] editors = EditorFactory.getInstance().getEditors(document, project);

            final TextAttributes attributes = new TextAttributes();

            attributes.setEffectType(EffectType.SEARCH_MATCH);
            attributes.setForegroundColor(JBColor.WHITE);



            // Records this cursor's location
            cursorPositions.put(cursorMovement.getUserId(), cursorMovement.getPosition());

            for (Editor e : editors) {
                for (RangeHighlighter highlighter : e.getMarkupModel().getAllHighlighters()) {
                    highlighter.dispose();
                }

                for (Map.Entry<Integer, Integer> pair : cursorPositions.entrySet()) {


                    int start = pair.getValue();

                    int end = start + 1;

                    if (end > textLength) {
                        end = textLength;
                    }
                    if (start >= textLength) {
                        start = textLength - 1;
                    }

                    final JBColor color = Colors.getColorById(pair.getKey());

                    attributes.setEffectColor(color);
                    attributes.setBackgroundColor(color);

                    e.getMarkupModel().addRangeHighlighter(start, end,
                            HighlighterLayer.ERROR + 100, attributes, HighlighterTargetArea.EXACT_RANGE);
                }
            }
        });
    }
}
