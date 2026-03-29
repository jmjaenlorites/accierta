package com.accierta.groups.application.handler;

public class AlreadyGroupMemberException extends RuntimeException {

    public AlreadyGroupMemberException(String message) {
        super(message);
    }
}
