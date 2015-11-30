package com.io.net;

import com.io.domain.FileTransfer;
import com.io.domain.ConnectionUpdate;
import com.io.domain.Login;
import com.io.domain.UserEdit;

public interface ConnectorEvent {
    void applyUserEdit(UserEdit userEdit);

    void applyUserId(Login login, Connector connector);

    void applyNewFiles(FileTransfer fileTransfer);
    void applyConnectionUpdate(ConnectionUpdate connectionUpdate);

    void applyCursorMove(UserEdit userEdit);
}
