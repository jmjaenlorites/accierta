package com.accierta.identity.application.handler;

public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
