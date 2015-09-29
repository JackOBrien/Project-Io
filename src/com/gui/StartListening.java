package com.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.*;


public class StartListening extends AnAction {

    private EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();

    public void actionPerformed(AnActionEvent e) {
        eventMulticaster.addDocumentListener(documentListener);
        eventMulticaster.addCaretListener(caretListener);
    }

    private DocumentListener documentListener = new DocumentListener() {
        @Override
        public void beforeDocumentChange(DocumentEvent event) {
            String dummyIdentifier = "IntelliJIdeaRulezzz";

            if (!dummyIdentifier.equals(event.getNewFragment().toString())) {
                if (!event.isWholeTextReplaced()) {
                    UserEdit edit = new UserEdit(0, event.getNewFragment().toString(), event.getOffset());

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

            System.out.println(edit);
        }

        @Override
        public void caretAdded(CaretEvent e) {}

        @Override
        public void caretRemoved(CaretEvent e) {}
    };
}
