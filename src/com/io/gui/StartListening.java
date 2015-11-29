package com.io.gui;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.io.domain.UserEdit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which sets up the document to track changes and send them to the server.
 */
public class StartListening {

    private List<EditorEvent> events = new ArrayList<>();

    private EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();

    public boolean isListening = true;
    private Project project;

    public StartListening(Editor editor) {
        eventMulticaster.addDocumentListener(documentListener);
        eventMulticaster.addCaretListener(caretListener);
        project = editor.getProject();
    }

    public void addEventListener(EditorEvent editorEvent) {
        events.add(editorEvent);
    }

    private DocumentListener documentListener = new DocumentListener() {
        @Override
        public void beforeDocumentChange(DocumentEvent event) {

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

                    UserEdit edit = new UserEdit(0, event.getNewFragment().toString(), relativeFilePath, event.getOffset(), lengthDifference);

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
            VirtualFile file = FileDocumentManager.getInstance().getFile(event.getEditor().getDocument());

            int offset = event.getEditor().logicalPositionToOffset(event.getNewPosition());

            //Get path relative to project root (e.g. src/Sample.java)
            Path basePath = Paths.get(project.getBasePath());
            Path absoluteFilePath = Paths.get(file.getPath());
            String relativeFilePath = basePath.relativize(absoluteFilePath).toString();

            UserEdit edit = new UserEdit(-1, relativeFilePath, offset, 0);

            for (EditorEvent editorEvent : events) {
                editorEvent.sendChange(edit);
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
