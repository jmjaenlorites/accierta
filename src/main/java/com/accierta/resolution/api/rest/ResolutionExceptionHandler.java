package com.accierta.resolution.api.rest;

import com.accierta.identity.api.rest.ApiErrorResponse;
import com.accierta.resolution.application.handler.ResolutionForbiddenException;
import com.accierta.shared.security.AuthenticationRequiredException;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ResolutionExceptionHandler {

    @ExceptionHandler(AuthenticationRequiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiErrorResponse handleUnauthorized(AuthenticationRequiredException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(ResolutionForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiErrorResponse handleForbidden(ResolutionForbiddenException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse handleBadRequest(IllegalStateException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    ApiErrorResponse handleValidation(IllegalArgumentException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiErrorResponse handleNotFound(NoSuchElementException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }
}
