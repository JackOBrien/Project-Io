package com.io.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.impl.DocumentImpl;

import javax.swing.*;
import java.util.ArrayList;

public class StartIo extends AnAction {

    ArrayList<Editor> editors;
    StartReceiving receiving;

    public void actionPerformed(AnActionEvent e) {
        editors = new ArrayList<>();
        editors.add(e.getData(LangDataKeys.EDITOR));

        StartListening listening = new StartListening();
        listening.setDocumentListener(testDocumentListener);

        listening.actionPerformed(e);

//        DocumentListener documentListener = listening.getDocumentListener();

        receiving = new StartReceiving(testDocumentListener);

        editors.add(createDummyEditor(e, testDocumentListener));
    }

    //TODO: Remove this. Testing only.
    private Editor createDummyEditor(AnActionEvent e, DocumentListener documentListener) {
        JFrame frame = new JFrame("TEST");
        JPanel panel = new JPanel();

        Document doc = new DocumentImpl(e.getData(LangDataKeys.EDITOR).getDocument().getText());
        doc.addDocumentListener(documentListener);

        Editor editor = EditorFactory.getInstance().createEditor(doc);

        panel.add(editor.getComponent());
        frame.add(panel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        return editor;
    }

    DocumentListener testDocumentListener = new DocumentListener() {
        @Override
        public void beforeDocumentChange(DocumentEvent event) {
            String dummyIdentifier = "IntellijIdeaRulezzz";

            if (!event.getNewFragment().toString().contains(dummyIdentifier)) {
                if (!event.isWholeTextReplaced()) {
                    UserEdit edit = new UserEdit(0, event.getNewFragment().toString(), event.getOffset());

                    for (Editor e : editors) {
                        if (e.getDocument().equals(event.getDocument())) continue;

                        receiving.applyUserEditToDocument(e, edit);
                        System.out.println("Sending edit: " + edit.getEditText() + " @ " + edit.getOffset());
                    }
                }
            }
        }

        @Override
        public void documentChanged(DocumentEvent event) {}
    };
}
