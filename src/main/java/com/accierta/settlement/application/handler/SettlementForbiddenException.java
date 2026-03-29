package com.accierta.settlement.application.handler;

public class SettlementForbiddenException extends RuntimeException {

    public SettlementForbiddenException(String message) {
        super(message);
    }
}
