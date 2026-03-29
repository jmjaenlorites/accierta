package com.accierta.identity.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.HashedPassword;
import com.accierta.identity.domain.model.RawPassword;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.model.UserId;
import com.accierta.identity.domain.port.in.RequestPasswordResetCommand;
import com.accierta.identity.domain.port.out.PasswordResetEmailSender;
import com.accierta.identity.domain.port.out.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class RequestPasswordResetHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetEmailSender passwordResetEmailSender;

    private RequestPasswordResetHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new RequestPasswordResetHandler(userRepository, passwordResetEmailSender, FIXED_CLOCK);
    }

    @Test
    void should_request_reset_and_send_email_for_existing_user() {
        User user = User.register(
                UserId.newId(),
                new Email("reset@example.com"),
                new RawPassword("secure123"),
                new HashedPassword("$2a$10$example-hash"),
                null,
                FIXED_CLOCK);
        when(userRepository.findByEmail(new Email("reset@example.com"))).thenReturn(Optional.of(user));

        handler.requestPasswordReset(new RequestPasswordResetCommand("reset@example.com"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().resetToken()).isNotNull();
        verify(passwordResetEmailSender).sendPasswordResetEmail(user.email(), userCaptor.getValue().resetToken());
    }

    @Test
    void should_return_success_without_sending_email_for_unknown_email() {
        when(userRepository.findByEmail(new Email("missing@example.com"))).thenReturn(Optional.empty());

        handler.requestPasswordReset(new RequestPasswordResetCommand("missing@example.com"));

        verify(passwordResetEmailSender, never()).sendPasswordResetEmail(any(), any());
        verify(userRepository, never()).save(any());
    }
}
