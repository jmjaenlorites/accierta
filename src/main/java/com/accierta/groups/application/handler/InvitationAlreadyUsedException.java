package com.accierta.groups.application.handler;

public class InvitationAlreadyUsedException extends RuntimeException {

    public InvitationAlreadyUsedException(String message) {
        super(message);
    }
}
