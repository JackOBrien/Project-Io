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

    public UserListWindow(Project project) {

        //Add panel as a Tool Window
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow("User List", true, ToolWindowAnchor.LEFT);
        Content content = ContentFactory.SERVICE.getInstance().createContent(this, "", true);
        toolWindow.getContentManager().addContent(content);

        users = new DefaultListModel<>();
        JBList userList = new JBList(users);

        this.add(userList);

    }

    public void addUser(UserInfo user) {
        ApplicationManager.getApplication().invokeLater(() -> {
            this.users.addElement(user);
        });
    }

}
