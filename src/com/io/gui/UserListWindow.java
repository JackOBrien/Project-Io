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

public class UserListWindow extends JPanel {

    private DefaultListModel<UserInfo> users;

    public UserListWindow() {
        users = new DefaultListModel<>();
        JBList userList = new JBList(users);

        this.add(userList);
    }

    public UserListWindow(Project project) {
        this();
        attachToProject(project);
    }

    public void attachToProject(Project project) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow("User List", true, ToolWindowAnchor.LEFT);
        Content content = ContentFactory.SERVICE.getInstance().createContent(this, "", true);
        toolWindow.getContentManager().addContent(content);
    }

    public void addUser(UserInfo user) {
        ApplicationManager.getApplication().invokeLater(() -> {
            this.users.addElement(user);
        });
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
