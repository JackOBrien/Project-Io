package com.io.gui;


import com.intellij.ui.components.JBList;

import javax.swing.*;

public class UserListWindow extends JPanel {

    private DefaultListModel<String> users;

    public UserListWindow() {

        users = new DefaultListModel<>();
        JBList userList = new JBList(users);

        this.add(userList);

    }

    public void addUser(String user) {
        this.users.addElement(user);
    }

}
