package com.accierta.identity.application.handler;

public class ResetTokenExpiredException extends RuntimeException {

    public ResetTokenExpiredException(String message) {
        super(message);
    }
}
