package com.accierta.identity.application.handler;

public class ResetTokenNotFoundException extends RuntimeException {

    public ResetTokenNotFoundException(String message) {
        super(message);
    }
}
