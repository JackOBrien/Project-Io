package com.io.gui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.*;
import com.io.domain.UserEdit;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which sets up the document to track changes and send them to the server.
 */
public class StartListening {

    private List<EditorEvent> events = new ArrayList<>();

    public StartListening(Editor editor) {
        Document document = editor.getDocument();
        document.addDocumentListener(documentListener);
    }

    public void addEventListener(EditorEvent editorEvent) {
        events.add(editorEvent);
    }

    private DocumentListener documentListener = new DocumentListener() {
        @Override
        public void beforeDocumentChange(DocumentEvent event) {
            String dummyIdentifier = "IntellijIdeaRulezzz";

            if (!event.getNewFragment().toString().contains(dummyIdentifier)) {
                if (!event.isWholeTextReplaced()) {
                    int lengthDifference = event.getNewLength() - event.getOldLength();

                    UserEdit edit = new UserEdit(0, event.getNewFragment().toString(), event.getOffset(), lengthDifference);

                    for (EditorEvent editorEvent : events) {
                        editorEvent.sendChange(edit);
                    }

                    //TODO: Send UserEdits to server. Println serves as a placeholder.
                    System.out.println(edit);
                }
            }
        }

        @Override
        public void documentChanged(DocumentEvent event) {}
    };

    private CaretListener caretListener = new CaretListener() {
        @Override
        public void caretPositionChanged(CaretEvent e) {
            int offset = e.getEditor().logicalPositionToOffset(e.getNewPosition());

            UserEdit edit = new UserEdit(0, null, offset);

            //TODO: Send caret position to server. Println serves as a placeholder.
            System.out.println(edit);
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

    //TODO: Remove this. Testing only.
    public void setDocumentListener(DocumentListener dl) {
        documentListener = dl;
    }
}
