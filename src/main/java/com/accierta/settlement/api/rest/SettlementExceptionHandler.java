package com.accierta.settlement.api.rest;

import com.accierta.identity.api.rest.ApiErrorResponse;
import com.accierta.settlement.application.handler.SettlementForbiddenException;
import com.accierta.shared.security.AuthenticationRequiredException;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = {DebtsController.class, LeaderboardController.class})
public class SettlementExceptionHandler {

    @ExceptionHandler(AuthenticationRequiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiErrorResponse handleAuthentication(AuthenticationRequiredException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(SettlementForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiErrorResponse handleForbidden(SettlementForbiddenException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiErrorResponse handleIllegalArgument(IllegalArgumentException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse handleBadRequest(IllegalStateException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiErrorResponse handleNotFound(NoSuchElementException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }
}
