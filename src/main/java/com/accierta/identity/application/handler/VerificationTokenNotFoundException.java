package com.accierta.identity.application.handler;

public class VerificationTokenNotFoundException extends RuntimeException {

    public VerificationTokenNotFoundException(String message) {
        super(message);
    }
}
