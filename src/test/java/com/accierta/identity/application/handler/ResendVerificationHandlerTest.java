package com.accierta.identity.application.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.HashedPassword;
import com.accierta.identity.domain.model.RawPassword;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.model.UserId;
import com.accierta.identity.domain.port.in.ResendVerificationCommand;
import com.accierta.identity.domain.port.out.UserRepository;
import com.accierta.identity.domain.port.out.VerificationEmailSender;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ResendVerificationHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationEmailSender verificationEmailSender;

    private ResendVerificationHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new ResendVerificationHandler(userRepository, verificationEmailSender, FIXED_CLOCK);
    }

    @Test
    void should_resend_verification_email_for_unverified_user() {
        User user = User.register(
                UserId.newId(),
                new Email("resend@example.com"),
                new RawPassword("secure123"),
                new HashedPassword("$2a$10$example-hash"),
                null,
                FIXED_CLOCK);
        when(userRepository.findByEmail(new Email("resend@example.com"))).thenReturn(Optional.of(user));

        handler.resend(new ResendVerificationCommand("resend@example.com"));

        verify(userRepository).save(user);
        verify(verificationEmailSender).sendVerificationEmail(user.email(), user.verificationToken());
    }

    @Test
    void should_reject_resend_when_user_does_not_exist() {
        when(userRepository.findByEmail(new Email("missing@example.com"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.resend(new ResendVerificationCommand("missing@example.com")))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("user not found");
    }
}
