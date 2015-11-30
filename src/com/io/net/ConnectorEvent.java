package com.io.net;

import com.io.domain.*;

public interface ConnectorEvent {
    void applyUserEdit(UserEdit userEdit);

    void applyUserId(Login login, Connector connector);
    void applyLogout(Logout logout, Connector connector);

    void applyNewFiles(FileTransfer fileTransfer);
    void applyConnectionUpdate(ConnectionUpdate connectionUpdate);

    void onDisconnect(Connector connector);

    void applyCursorMove(UserEdit userEdit);
}
