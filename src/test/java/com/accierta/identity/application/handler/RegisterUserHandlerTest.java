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
import com.accierta.identity.domain.port.in.RegisterUserCommand;
import com.accierta.identity.domain.port.out.PasswordHasher;
import com.accierta.identity.domain.port.out.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class RegisterUserHandlerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-03-26T10:15:30Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    private RegisterUserHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new RegisterUserHandler(userRepository, passwordHasher, FIXED_CLOCK);
    }

    @Test
    void should_reject_registration_when_email_already_exists() {
        when(userRepository.existsByEmail(new Email("taken@example.com"))).thenReturn(true);

        assertThatThrownBy(() -> handler.register(new RegisterUserCommand("taken@example.com", "secure123", null)))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("email already in use");

        verify(passwordHasher, never()).hash(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void should_save_new_user_when_email_is_available() {
        when(userRepository.existsByEmail(new Email("new@example.com"))).thenReturn(false);
        when(passwordHasher.hash(new RawPassword("secure123")))
                .thenReturn(new HashedPassword("$2a$10$example-hash"));

        var userId = handler.register(new RegisterUserCommand("new@example.com", "secure123", "invite-123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userId).isEqualTo(userCaptor.getValue().id());
        assertThat(userCaptor.getValue().email()).isEqualTo(new Email("new@example.com"));
    }
}
