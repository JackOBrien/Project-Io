package com.io.gui;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserListWindow extends JPanel {

    private DefaultListModel<UserInfo> users;
    private ChatEvent chatEvent = null;

    private JTextArea chatArea;

    public UserListWindow() {

        //Create user list
        users = new DefaultListModel<>();
        JBList userList = new JBList(users);
        this.add(userList);

        //Create chat controls
        chatArea = new JTextArea(10, 100);
        JTextField chatInput = new JTextField();
        chatInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = chatInput.getText().trim();
                chatInput.setText("");

                if (chatEvent != null) {
                    chatEvent.onNewChatMessage(message);
                }
            }
        });
        this.add(chatArea);
        this.add(chatInput);

    }

    public UserListWindow(Project project) {
        this();
        attachToProject(project);
    }

    public void attachToProject(Project project) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow("User List", true, ToolWindowAnchor.BOTTOM);
        Content content = ContentFactory.SERVICE.getInstance().createContent(this, "", true);
        toolWindow.getContentManager().addContent(content);
    }

    public void addUser(UserInfo user) {
        ApplicationManager.getApplication().invokeLater(() -> {
            this.users.addElement(user);
        });
    }

    public void onNewChatMessage(ChatEvent chatEvent) {
        this.chatEvent = chatEvent;
    }

    public void appendChatMessage(String message) {
        message = message.trim();
        chatArea.append(message + System.lineSeparator());
    }

}
