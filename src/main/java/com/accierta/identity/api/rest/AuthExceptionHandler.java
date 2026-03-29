package com.accierta.identity.api.rest;

import com.accierta.identity.application.handler.AlreadyVerifiedException;
import com.accierta.identity.application.handler.DuplicateEmailException;
import com.accierta.identity.application.handler.EmailNotVerifiedException;
import com.accierta.identity.application.handler.InvalidCredentialsException;
import com.accierta.identity.application.handler.InvalidRefreshTokenException;
import com.accierta.identity.application.handler.ResetTokenExpiredException;
import com.accierta.identity.application.handler.ResetTokenNotFoundException;
import com.accierta.identity.application.handler.UserNotFoundException;
import com.accierta.identity.application.handler.VerificationTokenNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiErrorResponse handleDuplicateEmail(DuplicateEmailException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    ApiErrorResponse handleValidation(IllegalArgumentException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler({
            IllegalStateException.class,
            VerificationTokenNotFoundException.class,
            ResetTokenNotFoundException.class,
            ResetTokenExpiredException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse handleBadRequest(RuntimeException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiErrorResponse handleNotFound(UserNotFoundException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(AlreadyVerifiedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse handleAlreadyVerified(AlreadyVerifiedException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiErrorResponse handleInvalidCredentials(InvalidCredentialsException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiErrorResponse handleEmailNotVerified(EmailNotVerifiedException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiErrorResponse handleInvalidRefreshToken(InvalidRefreshTokenException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    ApiErrorResponse handleBeanValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "validation error" : error.getDefaultMessage())
                .orElse("validation error");
        return new ApiErrorResponse(message);
    }
}
