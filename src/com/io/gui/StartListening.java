package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.*;

/**
 * Class which sets up the document to track changes and send them to the server.
 */
public class StartListening extends AnAction {

    private EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();

    /**
     * Adds DocumentListener to the current editor
     * @param e Used to pull the current editor.
     */
    public void actionPerformed(AnActionEvent e) {

        Editor editor = e.getData(LangDataKeys.EDITOR);

        if (editor == null) {
            System.err.println("Could not get editor.");
            return;
        }

        Document document = editor.getDocument();
        document.addDocumentListener(documentListener);

        eventMulticaster.addCaretListener(caretListener);
    }

    private DocumentListener documentListener = new DocumentListener() {
        @Override
        public void beforeDocumentChange(DocumentEvent event) {
            String dummyIdentifier = "IntellijIdeaRulezzz";

            if (!event.getNewFragment().toString().contains(dummyIdentifier)) {
                if (!event.isWholeTextReplaced()) {
                    UserEdit edit = new UserEdit(0, event.getNewFragment().toString(), event.getOffset());

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
}
