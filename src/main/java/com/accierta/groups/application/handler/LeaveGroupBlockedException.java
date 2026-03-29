package com.accierta.groups.application.handler;

import com.accierta.groups.application.query.CanLeaveGroupView;

public class LeaveGroupBlockedException extends RuntimeException {

    private final CanLeaveGroupView canLeaveGroupView;

    public LeaveGroupBlockedException(String message, CanLeaveGroupView canLeaveGroupView) {
        super(message);
        this.canLeaveGroupView = canLeaveGroupView;
    }

    public CanLeaveGroupView canLeaveGroupView() {
        return canLeaveGroupView;
    }
}
