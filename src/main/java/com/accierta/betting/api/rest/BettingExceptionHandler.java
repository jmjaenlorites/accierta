package com.accierta.betting.api.rest;

import com.accierta.betting.application.handler.PorraForbiddenException;
import com.accierta.identity.api.rest.ApiErrorResponse;
import com.accierta.shared.security.AuthenticationRequiredException;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BettingExceptionHandler {

    @ExceptionHandler(AuthenticationRequiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiErrorResponse handleUnauthorized(AuthenticationRequiredException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler({PorraForbiddenException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiErrorResponse handleForbidden(PorraForbiddenException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    ApiErrorResponse handleValidation(IllegalArgumentException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse handleDeleteWindow(IllegalStateException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiErrorResponse handleNotFound(NoSuchElementException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }
}
