package com.accierta.identity.application.handler;

public class AlreadyVerifiedException extends RuntimeException {

    public AlreadyVerifiedException(String message) {
        super(message);
    }
}
