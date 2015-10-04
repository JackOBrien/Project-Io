package com.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.editor.impl.DocumentImpl;

import javax.swing.*;


public class StartListening extends AnAction {

    private EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();

    public void actionPerformed(AnActionEvent e) {
        e.getData(LangDataKeys.EDITOR).getComponent().setName("1");
        e.getData(LangDataKeys.EDITOR).getDocument().addDocumentListener(documentListener);

//        eventMulticaster.addDocumentListener(documentListener);
        eventMulticaster.addCaretListener(caretListener);

        System.out.println("b4");
        JFrame frame = new JFrame("TEST");
        JPanel panel = new JPanel();
//        panel.setPreferredSize(new Dimension(400, 800));

        Document doc = new DocumentImpl(e.getData(LangDataKeys.EDITOR).getDocument().getText());
        doc.addDocumentListener(documentListener);

        Editor editor = EditorFactory.getInstance().createEditor(doc);
        editor.getComponent().setName("2");

        panel.add(editor.getComponent());

        frame.add(panel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        System.out.println("after");
    }

    private DocumentListener documentListener = new DocumentListener() {
        @Override
        public void beforeDocumentChange(DocumentEvent event) {
            String dummyIdentifier = "IntellijIdeaRulezzz";

            if (!event.getNewFragment().toString().contains(dummyIdentifier)) {
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
