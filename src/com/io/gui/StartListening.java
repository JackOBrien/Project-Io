package com.io.gui;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.io.domain.CursorMovement;
import com.io.domain.UserEdit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.io.gui.diff_match_patch.Patch;

/**
 * Class which sets up the document to track changes and send them to the server.
 */
public class StartListening {

    private List<EditorEvent> events = new ArrayList<>();

    private EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();

    public boolean isListening = true;
    private Project project;

    public StartListening(Project project) {
        eventMulticaster.addDocumentListener(documentListener);
        eventMulticaster.addCaretListener(caretListener);
        this.project = project;
    }

    public void addEventListener(EditorEvent editorEvent) {
        events.add(editorEvent);
    }

    private DocumentListener documentListener = new DocumentListener() {
        @Override
        public void beforeDocumentChange(DocumentEvent event) {

            if (project.isDisposed()) {
                return;
            }

            if (!isListening) {
                System.out.println("Ignoring change.");
                return;
            }

            VirtualFile file = FileDocumentManager.getInstance().getFile(event.getDocument());

            //Make sure file exists
            if (file == null) {
                return;
            }

            //Make sure file is in the project
            if (!ProjectFileIndex.SERVICE.getInstance(project).isInSource(file)) {
                return;
            }

            String dummyIdentifier = "IntellijIdeaRulezzz";

            if (!event.getNewFragment().toString().contains(dummyIdentifier)) {
                if (!event.isWholeTextReplaced()) {
                    int lengthDifference = event.getNewLength() - event.getOldLength();

                    //Get path relative to project root (e.g. src/Sample.java)
                    Path basePath = Paths.get(project.getBasePath());
                    Path absoluteFilePath = Paths.get(file.getPath());
                    String relativeFilePath = basePath.relativize(absoluteFilePath).toString();

                    IOPatcher patcher = new IOPatcher();
                    LinkedList<Patch> patches = null;

                    //Inserting else deleting
                    if (lengthDifference > 0) {
                        patches = patcher.makeInsertPatchAsList(event.getDocument().getText(), event.getNewFragment().toString(), event.getOffset());
                    }
                    else {
                        patches = patcher.makeDeletePatchAsList(event.getDocument().getText(), event.getOldFragment().toString(), event.getOffset());
                    }

                    UserEdit edit = new UserEdit(0, relativeFilePath, patches);

                    for (EditorEvent editorEvent : events) {
                        editorEvent.sendChange(edit);
                    }
                }
            }
        }

        @Override
        public void documentChanged(DocumentEvent event) {}
    };

    private CaretListener caretListener = new CaretListener() {
        @Override
        public void caretPositionChanged(CaretEvent event) {

            if (project.isDisposed()) {
                return;
            }

            VirtualFile file = FileDocumentManager.getInstance().getFile(event.getEditor().getDocument());

            //Make sure file exists
            if (file == null) {
                return;
            }

            //Make sure file is in the project
            if (!ProjectFileIndex.SERVICE.getInstance(project).isInSource(file)) {
                return;
            }

            int offset = event.getEditor().logicalPositionToOffset(event.getNewPosition());

            //Get path relative to project root (e.g. src/Sample.java)
            Path basePath = Paths.get(project.getBasePath());
            Path absoluteFilePath = Paths.get(file.getPath());
            String relativeFilePath = basePath.relativize(absoluteFilePath).toString();

            CursorMovement cursorMovement = new CursorMovement(-1, relativeFilePath, offset);

            for (EditorEvent editorEvent : events) {
                editorEvent.sendCursorMovement(cursorMovement);
            }
        }

        @Override
        public void caretAdded(CaretEvent e) {}

        @Override
        public void caretRemoved(CaretEvent e) {}
    };

    /**
     * Returns the DocumentListener which sends document changes to the server.
     *
     * @return DocumentListener which sends document changes to the server.
     */
    public DocumentListener getDocumentListener() {
        return documentListener;
    }
}
