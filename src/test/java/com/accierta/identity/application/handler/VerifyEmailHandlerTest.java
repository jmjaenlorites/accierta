package com.accierta.identity.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accierta.identity.domain.model.HashedPassword;
import com.accierta.identity.domain.model.RawPassword;
import com.accierta.identity.domain.model.User;
import com.accierta.identity.domain.model.UserId;
import com.accierta.identity.domain.port.in.VerifyEmailCommand;
import com.accierta.identity.domain.port.out.PendingInvitationAcceptor;
import com.accierta.identity.domain.port.out.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class VerifyEmailHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PendingInvitationAcceptor pendingInvitationAcceptor;

    private VerifyEmailHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new VerifyEmailHandler(userRepository, pendingInvitationAcceptor, FIXED_CLOCK);
    }

    @Test
    void should_verify_user_when_token_exists() {
        User user = User.register(
                UserId.newId(),
                new com.accierta.identity.domain.model.Email("verify@example.com"),
                new RawPassword("secure123"),
                new HashedPassword("$2a$10$example-hash"),
                null,
                FIXED_CLOCK);
        when(userRepository.findByVerificationToken(user.verificationToken().value())).thenReturn(Optional.of(user));

        handler.verify(new VerifyEmailCommand(user.verificationToken().value()));

        assertThat(user.isVerified()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void should_reject_when_token_does_not_exist() {
        when(userRepository.findByVerificationToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.verify(new VerifyEmailCommand("missing")))
                .isInstanceOf(VerificationTokenNotFoundException.class)
                .hasMessage("verification token is invalid");
    }
}
