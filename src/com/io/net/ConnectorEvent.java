package com.io.net;

import com.io.domain.UserEdit;

public interface ConnectorEvent {
    void applyUserEdit(UserEdit userEdit);
}
