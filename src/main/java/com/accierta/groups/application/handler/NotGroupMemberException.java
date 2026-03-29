package com.accierta.groups.application.handler;

public class NotGroupMemberException extends RuntimeException {

    public NotGroupMemberException(String message) {
        super(message);
    }
}
