package com.accierta.groups.api.rest;

import com.accierta.groups.application.query.GroupAccessDeniedException;
import com.accierta.groups.application.handler.AlreadyGroupMemberException;
import com.accierta.groups.application.handler.InvitationAlreadyUsedException;
import com.accierta.groups.application.handler.InvitationInvalidException;
import com.accierta.groups.application.handler.LeaveGroupBlockedException;
import com.accierta.groups.application.handler.NotGroupMemberException;
import com.accierta.groups.application.handler.UnverifiedUserException;
import com.accierta.identity.api.rest.ApiErrorResponse;
import com.accierta.shared.security.AuthenticationRequiredException;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GroupsExceptionHandler {

    @ExceptionHandler(AuthenticationRequiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiErrorResponse handleUnauthorized(AuthenticationRequiredException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(UnverifiedUserException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiErrorResponse handleForbidden(UnverifiedUserException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(GroupAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiErrorResponse handleNotMember(GroupAccessDeniedException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(NotGroupMemberException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiErrorResponse handleInviteForbidden(NotGroupMemberException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    ApiErrorResponse handleValidation(IllegalArgumentException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    ApiErrorResponse handleBeanValidation(MethodArgumentNotValidException exception) {
        return new ApiErrorResponse("validation error");
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiErrorResponse handleNotFound(NoSuchElementException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler({
            InvitationInvalidException.class,
            InvitationAlreadyUsedException.class,
            AlreadyGroupMemberException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse handleInvitationErrors(RuntimeException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(LeaveGroupBlockedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    com.accierta.groups.application.query.CanLeaveGroupView handleLeaveBlocked(LeaveGroupBlockedException exception) {
        return exception.canLeaveGroupView();
    }
}
