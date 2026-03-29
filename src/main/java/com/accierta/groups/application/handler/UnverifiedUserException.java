package com.accierta.groups.application.handler;

public class UnverifiedUserException extends RuntimeException {

    public UnverifiedUserException(String message) {
        super(message);
    }
}
