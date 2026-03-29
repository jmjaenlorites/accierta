package com.accierta.groups.application.query;

public class GroupAccessDeniedException extends RuntimeException {

    public GroupAccessDeniedException(String message) {
        super(message);
    }
}
