package com.accierta.identity.application;

import com.accierta.identity.application.handler.RegisterUserHandler;
import com.accierta.identity.application.handler.AuthenticateHandler;
import com.accierta.identity.application.handler.LogoutHandler;
import com.accierta.identity.application.handler.RequestPasswordResetHandler;
import com.accierta.identity.application.handler.ResetPasswordHandler;
import com.accierta.identity.application.handler.ResendVerificationHandler;
import com.accierta.identity.application.handler.RefreshSessionHandler;
import com.accierta.identity.application.handler.VerifyEmailHandler;
import com.accierta.identity.domain.port.in.AuthenticateUseCase;
import com.accierta.identity.domain.port.in.LogoutUseCase;
import com.accierta.identity.domain.port.in.RequestPasswordResetUseCase;
import com.accierta.identity.domain.port.in.RegisterUserUseCase;
import com.accierta.identity.domain.port.in.RefreshSessionUseCase;
import com.accierta.identity.domain.port.in.ResetPasswordUseCase;
import com.accierta.identity.domain.port.in.ResendVerificationUseCase;
import com.accierta.identity.domain.port.in.VerifyEmailUseCase;
import com.accierta.identity.domain.port.out.AuthenticationFailureLogger;
import com.accierta.identity.domain.port.out.PasswordHasher;
import com.accierta.identity.domain.port.out.PasswordResetEmailSender;
import com.accierta.identity.domain.port.out.PendingInvitationAcceptor;
import com.accierta.identity.domain.port.out.RefreshTokenRepository;
import com.accierta.identity.domain.port.out.TokenIssuer;
import com.accierta.identity.domain.port.out.UserRepository;
import com.accierta.identity.domain.port.out.VerificationEmailSender;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityConfiguration {

    @Bean
    Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    RegisterUserUseCase registerUserUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            VerificationEmailSender verificationEmailSender,
            Clock clock) {
        return command -> {
            RegisterUserHandler handler = new RegisterUserHandler(userRepository, passwordHasher, clock);
            var userId = handler.register(command);
            userRepository.findByEmail(new com.accierta.identity.domain.model.Email(command.email()))
                    .ifPresent(user -> verificationEmailSender.sendVerificationEmail(user.email(), user.verificationToken()));
            return userId;
        };
    }

    @Bean
    VerifyEmailUseCase verifyEmailUseCase(
            UserRepository userRepository,
            PendingInvitationAcceptor pendingInvitationAcceptor,
            Clock clock) {
        return new VerifyEmailHandler(userRepository, pendingInvitationAcceptor, clock);
    }

    @Bean
    ResendVerificationUseCase resendVerificationUseCase(
            UserRepository userRepository,
            VerificationEmailSender verificationEmailSender,
            Clock clock) {
        return new ResendVerificationHandler(userRepository, verificationEmailSender, clock);
    }

    @Bean
    AuthenticateUseCase authenticateUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer,
            RefreshTokenRepository refreshTokenRepository,
            AuthenticationFailureLogger authenticationFailureLogger,
            Clock clock) {
        return new AuthenticateHandler(
                userRepository,
                passwordHasher,
                tokenIssuer,
                refreshTokenRepository,
                authenticationFailureLogger,
                clock);
    }

    @Bean
    RefreshSessionUseCase refreshSessionUseCase(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            TokenIssuer tokenIssuer,
            Clock clock) {
        return new RefreshSessionHandler(refreshTokenRepository, userRepository, tokenIssuer, clock);
    }

    @Bean
    RequestPasswordResetUseCase requestPasswordResetUseCase(
            UserRepository userRepository,
            PasswordResetEmailSender passwordResetEmailSender,
            Clock clock) {
        return new RequestPasswordResetHandler(userRepository, passwordResetEmailSender, clock);
    }

    @Bean
    ResetPasswordUseCase resetPasswordUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            RefreshTokenRepository refreshTokenRepository,
            Clock clock) {
        return new ResetPasswordHandler(userRepository, passwordHasher, refreshTokenRepository, clock);
    }

    @Bean
    LogoutUseCase logoutUseCase(RefreshTokenRepository refreshTokenRepository, Clock clock) {
        return new LogoutHandler(refreshTokenRepository, clock);
    }
}
