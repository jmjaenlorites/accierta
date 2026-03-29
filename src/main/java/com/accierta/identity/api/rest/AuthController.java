package com.accierta.identity.api.rest;

import com.accierta.identity.api.dto.MessageResponse;
import com.accierta.identity.api.dto.LoginRequest;
import com.accierta.identity.api.dto.RegisterUserRequest;
import com.accierta.identity.api.dto.RegisterUserResponse;
import com.accierta.identity.api.dto.RefreshRequest;
import com.accierta.identity.api.dto.RequestPasswordResetRequest;
import com.accierta.identity.api.dto.ResetPasswordRequest;
import com.accierta.identity.api.dto.ResendVerificationRequest;
import com.accierta.identity.api.dto.TokenResponse;
import com.accierta.identity.api.dto.VerifyEmailRequest;
import com.accierta.identity.domain.port.in.AuthenticateQuery;
import com.accierta.identity.domain.port.in.AuthenticateUseCase;
import com.accierta.identity.domain.port.in.LogoutCommand;
import com.accierta.identity.domain.port.in.LogoutUseCase;
import com.accierta.identity.domain.port.in.RegisterUserCommand;
import com.accierta.identity.domain.port.in.RegisterUserUseCase;
import com.accierta.identity.domain.port.in.RefreshSessionCommand;
import com.accierta.identity.domain.port.in.RefreshSessionUseCase;
import com.accierta.identity.domain.port.in.RequestPasswordResetCommand;
import com.accierta.identity.domain.port.in.RequestPasswordResetUseCase;
import com.accierta.identity.domain.port.in.ResetPasswordCommand;
import com.accierta.identity.domain.port.in.ResetPasswordUseCase;
import com.accierta.identity.domain.port.in.ResendVerificationCommand;
import com.accierta.identity.domain.port.in.ResendVerificationUseCase;
import com.accierta.identity.domain.port.in.VerifyEmailCommand;
import com.accierta.identity.domain.port.in.VerifyEmailUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;
    private final AuthenticateUseCase authenticateUseCase;
    private final RefreshSessionUseCase refreshSessionUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            VerifyEmailUseCase verifyEmailUseCase,
            ResendVerificationUseCase resendVerificationUseCase,
            AuthenticateUseCase authenticateUseCase,
            RefreshSessionUseCase refreshSessionUseCase,
            RequestPasswordResetUseCase requestPasswordResetUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            LogoutUseCase logoutUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.resendVerificationUseCase = resendVerificationUseCase;
        this.authenticateUseCase = authenticateUseCase;
        this.refreshSessionUseCase = refreshSessionUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        var userId = registerUserUseCase.register(
                new RegisterUserCommand(request.email(), request.password(), request.invitationToken()));
        return new RegisterUserResponse(userId.value(), "Verification email sent");
    }

    @PostMapping("/verify-email")
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        verifyEmailUseCase.verify(new VerifyEmailCommand(request.token()));
        return new MessageResponse("Email verified");
    }

    @PostMapping("/resend-verification")
    public MessageResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        resendVerificationUseCase.resend(new ResendVerificationCommand(request.email()));
        return new MessageResponse("Verification email sent");
    }

    @PostMapping("/request-password-reset")
    public MessageResponse requestPasswordReset(@Valid @RequestBody RequestPasswordResetRequest request) {
        requestPasswordResetUseCase.requestPasswordReset(new RequestPasswordResetCommand(request.email()));
        return new MessageResponse("If this email is registered, a reset link has been sent");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.resetPassword(new ResetPasswordCommand(request.token(), request.newPassword()));
        return new MessageResponse("Password updated successfully");
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        var tokenPair = authenticateUseCase.authenticate(new AuthenticateQuery(request.email(), request.password()));
        return new TokenResponse(tokenPair.accessToken(), tokenPair.refreshToken(), tokenPair.expiresIn());
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        var tokenPair = refreshSessionUseCase.refresh(new RefreshSessionCommand(request.refreshToken()));
        return new TokenResponse(tokenPair.accessToken(), tokenPair.refreshToken(), tokenPair.expiresIn());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest request) {
        logoutUseCase.logout(new LogoutCommand(request.refreshToken()));
    }
}
