package com.io.gui;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class UserListWindow extends JPanel {

    private ArrayList<UserInfo> users;
    private ActionListener followUserListener;
    private ChatEvent chatEvent = null;

    private JTextArea chatArea;
    private JButton stopFollowing = null;
    private JPanel userListPanel;

    private ArrayList<JButton> buttonList;


    public UserListWindow(ActionListener followUserListener) {

        this.followUserListener = followUserListener;

        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        //Create user list
        userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userListPanel.add(new JLabel("Select a user to Follow"));

        buttonList = new ArrayList<>();
        users = new ArrayList<>();

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
        this.addUser(user, false);
    }

    public void addUser(UserInfo user, Boolean isSelf) {


        JButton button = new JButton(user.getUsername());
        button.setActionCommand(Integer.toString(user.getUserId()));
        button.addActionListener(followUserListener);

        if (isSelf) {
            button.setEnabled(false);
        }
        else {
            UserListWindow self = this;
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton source = (JButton) e.getSource();
                    JButton stopFollowing = new JButton("Stop Following " + source.getText());
                    stopFollowing.setActionCommand("-1");
                    stopFollowing.addActionListener(followUserListener);
                    stopFollowing.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JButton source = (JButton) e.getSource();
                            SwingUtilities.invokeLater(() -> {
                                userListPanel.remove(source);

                                self.revalidate();
                                self.repaint();
                            });
                        }
                    });

                    SwingUtilities.invokeLater(() -> {
                        if (self.stopFollowing != null) {
                            userListPanel.remove(self.stopFollowing);
                        }

                        self.stopFollowing = stopFollowing;
                        userListPanel.add(self.stopFollowing);

                        self.revalidate();
                        self.repaint();
                    });
                }
            });
        }

        buttonList.add(button);
        users.add(user);

        SwingUtilities.invokeLater(() -> {
            userListPanel.add(button);

            this.revalidate();
            this.repaint();
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

        if (users == null) {
            return "<Not Found>";
        }

        for (int i = 0; i < users.size(); i++) {
            UserInfo userInfo = users.get(i);
            if (userInfo.getUserId() == userId) {
                return userInfo.getUsername();
            }
        }
        return "<Not Found>";
    }

    public void removeUserById(int userId) {
        for (JButton button : buttonList) {
            if (Integer.parseInt(button.getActionCommand()) == userId) {
                users.removeIf(user -> user.getUserId() == userId);
                buttonList.remove(button);
                ApplicationManager.getApplication().invokeLater(() -> {
                    userListPanel.remove(button);
                    userListPanel.repaint();
                });
                break;
            }
        }
    }
}
