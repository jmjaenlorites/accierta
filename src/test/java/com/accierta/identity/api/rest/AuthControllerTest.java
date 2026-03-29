package com.accierta.identity.api.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.accierta.identity.application.handler.AlreadyVerifiedException;
import com.accierta.identity.application.handler.DuplicateEmailException;
import com.accierta.identity.application.handler.EmailNotVerifiedException;
import com.accierta.identity.application.handler.InvalidCredentialsException;
import com.accierta.identity.application.handler.InvalidRefreshTokenException;
import com.accierta.identity.application.handler.ResetTokenExpiredException;
import com.accierta.identity.application.handler.ResetTokenNotFoundException;
import com.accierta.identity.application.handler.UserNotFoundException;
import com.accierta.identity.application.handler.VerificationTokenNotFoundException;
import com.accierta.identity.domain.model.UserId;
import com.accierta.identity.domain.port.in.AuthenticateUseCase;
import com.accierta.identity.domain.port.in.LogoutUseCase;
import com.accierta.identity.domain.port.in.RegisterUserUseCase;
import com.accierta.identity.domain.port.in.RefreshSessionUseCase;
import com.accierta.identity.domain.port.in.RequestPasswordResetUseCase;
import com.accierta.identity.domain.port.in.ResetPasswordUseCase;
import com.accierta.identity.domain.port.in.ResendVerificationUseCase;
import com.accierta.identity.domain.port.in.VerifyEmailUseCase;
import com.accierta.identity.domain.port.out.TokenPair;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private VerifyEmailUseCase verifyEmailUseCase;

    @MockBean
    private ResendVerificationUseCase resendVerificationUseCase;

    @MockBean
    private AuthenticateUseCase authenticateUseCase;

    @MockBean
    private RefreshSessionUseCase refreshSessionUseCase;

    @MockBean
    private RequestPasswordResetUseCase requestPasswordResetUseCase;

    @MockBean
    private ResetPasswordUseCase resetPasswordUseCase;

    @MockBean
    private LogoutUseCase logoutUseCase;

    @Test
    void should_return_created_when_registering_user() throws Exception {
        UUID userId = UUID.randomUUID();
        when(registerUserUseCase.register(any())).thenReturn(new UserId(userId));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "secure123",
                                  "invitationToken": "invite-123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.message").value("Verification email sent"));
    }

    @Test
    void should_return_conflict_for_duplicate_email() throws Exception {
        when(registerUserUseCase.register(any())).thenThrow(new DuplicateEmailException("email already in use"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "secure123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("email already in use"));
    }

    @Test
    void should_return_ok_when_verifying_email() throws Exception {
        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "valid-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified"));
    }

    @Test
    void should_return_bad_request_for_invalid_token() throws Exception {
        doThrow(new VerificationTokenNotFoundException("verification token is invalid"))
                .when(verifyEmailUseCase).verify(any());

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "missing-token"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("verification token is invalid"));
    }

    @Test
    void should_return_not_found_for_unknown_email_on_resend() throws Exception {
        doThrow(new UserNotFoundException("user not found")).when(resendVerificationUseCase).resend(any());

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "missing@example.com"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("user not found"));
    }

    @Test
    void should_return_bad_request_when_email_is_already_verified() throws Exception {
        doThrow(new AlreadyVerifiedException("email is already verified"))
                .when(resendVerificationUseCase).resend(any());

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "verified@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email is already verified"));
    }

    @Test
    void should_return_tokens_when_login_succeeds() throws Exception {
        when(authenticateUseCase.authenticate(any())).thenReturn(new TokenPair("jwt-token", "refresh-token", 900));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "secure123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void should_return_unauthorized_for_invalid_credentials() throws Exception {
        when(authenticateUseCase.authenticate(any())).thenThrow(new InvalidCredentialsException("invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "wrongpass"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid credentials"));
    }

    @Test
    void should_return_forbidden_for_unverified_user_on_login() throws Exception {
        when(authenticateUseCase.authenticate(any())).thenThrow(new EmailNotVerifiedException("email not verified"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "secure123"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("email not verified"));
    }

    @Test
    void should_return_rotated_tokens_when_refresh_succeeds() throws Exception {
        when(refreshSessionUseCase.refresh(any())).thenReturn(new TokenPair("new-jwt", "new-refresh", 900));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "old-refresh"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void should_return_unauthorized_for_invalid_refresh_token() throws Exception {
        when(refreshSessionUseCase.refresh(any()))
                .thenThrow(new InvalidRefreshTokenException("invalid or expired refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "bad-refresh"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid or expired refresh token"));
    }

    @Test
    void should_return_no_content_on_logout() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_return_ok_when_requesting_password_reset_for_registered_email() throws Exception {
        mockMvc.perform(post("/api/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If this email is registered, a reset link has been sent"));
    }

    @Test
    void should_return_ok_when_resetting_password() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "reset-token",
                                  "newPassword": "newsecure123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }

    @Test
    void should_return_bad_request_for_invalid_reset_token() throws Exception {
        doThrow(new ResetTokenNotFoundException("reset token is invalid"))
                .when(resetPasswordUseCase).resetPassword(any());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "missing-token",
                                  "newPassword": "newsecure123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("reset token is invalid"));
    }

    @Test
    void should_return_bad_request_for_expired_reset_token() throws Exception {
        doThrow(new ResetTokenExpiredException("reset token has expired"))
                .when(resetPasswordUseCase).resetPassword(any());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "expired-token",
                                  "newPassword": "newsecure123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("reset token has expired"));
    }
}
