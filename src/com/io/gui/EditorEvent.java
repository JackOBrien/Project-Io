package com.io.gui;

import com.io.domain.CursorMovement;
import com.io.domain.UserEdit;

public interface EditorEvent {
    void sendChange(UserEdit useredit);
    void sendCursorMovement(CursorMovement cursorMovement);
}
