package com.io.gui;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserListWindow extends JPanel {

    private DefaultListModel<UserInfo> users;
    private ChatEvent chatEvent = null;

    private JTextArea chatArea;

    private ActionListener followUserListener;

    public UserListWindow(ActionListener followUserListener) {

        this.followUserListener = followUserListener;

        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        //Create user list
//        users = new DefaultListModel<>();
//        JBList userList = new JBList(users);

        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        JButton testButton = new JButton("TEST (01)");
        testButton.setActionCommand("0");
        testButton.addActionListener(followUserListener);
        userListPanel.add(testButton);

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 2;
        c.ipadx = 50;
        this.add(userListPanel, c);

        //Create chat controls
        chatArea = new JTextArea(10, 50);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);

        JBScrollPane scrollPane = new JBScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JTextField chatInput = new JTextField(30);
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

        c.ipadx = 0;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.ipady = 150;
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.PAGE_END;
        this.add(scrollPane, c);

        c.ipady = 0;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.PAGE_START;
        this.add(chatInput, c);

    }

    public UserListWindow(Project project, ActionListener followUserListener) {
        this(followUserListener);
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
        String output = message.trim();

        ApplicationManager.getApplication().invokeLater(() -> {
            chatArea.append(output + System.lineSeparator());
        });
    }

    public String getUsernameById(int userId) {
        for (int i = 0; i < users.size(); i++) {
            UserInfo userInfo = users.getElementAt(i);
            if (userInfo.getUserId() == userId) {
                return userInfo.getUsername();
            }
        }
        return "<Not Found>";
    }

    public void removeUserById(int userId) {
        for (int i = 0; i < users.size(); i++) {
            final UserInfo userInfo = users.getElementAt(i);
            if (userInfo.getUserId() == userId) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    users.removeElement(userInfo);
                });
                return;
            }
        }
    }
}
