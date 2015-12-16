package com.io.gui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.io.domain.CursorMovement;
import com.io.domain.UserEdit;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

public class StartReceiving {

    StartListening listener;

    private Map<Integer, CursorPosition> cursorPositions;

    public StartReceiving(Project project, StartListening listener) {
        this.listener = listener;
        cursorPositions = new Hashtable<>();
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

    public void applyHighlightToDocument(Project project, CursorMovement cursorMovement, int followingUserId) {

        if (project.isDisposed()) {
            return;
        }

        String basePath = project.getBasePath();
        if (basePath == null) {
            return;
        }

        String filePath = Paths.get(basePath, cursorMovement.getFilePath()).toString();

        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (file == null || !ProjectFileIndex.SERVICE.getInstance(project).isInSource(file)) {
            System.out.println("Could not find file.");
            return;
        }

        // Records this cursor's location
        CursorPosition cursorPosition = new CursorPosition(file, cursorMovement.getPosition());
        cursorPositions.put(cursorMovement.getUserId(), cursorPosition);

        WriteCommandAction.runWriteCommandAction(project, () -> {

            //Remove all highlights
            Editor[] editors = EditorFactory.getInstance().getAllEditors();
            Arrays.stream(editors)
                    .filter((editor) -> editor.getProject() == project)
                    .forEach((editor) -> editor.getMarkupModel().removeAllHighlighters());

            //Apply each highlight to the specific editor
            cursorPositions.forEach((key, value) -> {

                Document doc = FileDocumentManager.getInstance().getDocument(value.File);
                if (doc == null) {
                    return;
                }

                int textLength = doc.getTextLength();
                if (textLength == 0) {
                    return;
                }

                Range range = new Range(value.Position, doc.getTextLength());
                TextAttributes attributes = buildAttributes(key);

                //If we are following this edit, make sure the editor is active
                if (followingUserId >= 0 && followingUserId == key) {
                    FileEditorManager.getInstance(project).openFile(file, true);
                }

                Editor[] docEditors = EditorFactory.getInstance().getEditors(doc);

                if (docEditors.length > 0) {
                    Editor firstEditor = docEditors[0];
                    firstEditor.getMarkupModel().addRangeHighlighter(range.Start, range.End, HighlighterLayer.ERROR + 100, attributes, HighlighterTargetArea.EXACT_RANGE);

                    if (followingUserId >= 0 && followingUserId == key) {
                        ScrollingModel scrollingModel = firstEditor.getScrollingModel();
                        scrollingModel.disableAnimation();

                        try {
                            LogicalPosition logicalPosition = firstEditor.offsetToLogicalPosition(value.Position);
                            scrollingModel.scrollTo(logicalPosition, ScrollType.MAKE_VISIBLE);
                        }
                        catch (IndexOutOfBoundsException ex) {
                            System.out.println("Out of bounds scrolling");
                        }
                    }
                }

            });
        });
    }

    private TextAttributes buildAttributes(int id) {
        JBColor color = Colors.getColorById(id);

        TextAttributes attributes = new TextAttributes();

        attributes.setEffectType(EffectType.SEARCH_MATCH);
        attributes.setForegroundColor(JBColor.WHITE);

        attributes.setEffectColor(color);
        attributes.setBackgroundColor(color);

        return attributes;
    }

    private class CursorPosition {
        public VirtualFile File;
        public int Position;

        public CursorPosition(VirtualFile file, int position) {
            File = file;
            Position = position;
        }
    }

    private class Range {
        public int Start, End;

        public Range(int position, int documentLength) {
            Start = position;
            End = position + 1;

            if (End > documentLength) {
                End = documentLength;
            }

            if (Start >= documentLength) {
                Start = documentLength - 1;
            }
        }
    }
}
