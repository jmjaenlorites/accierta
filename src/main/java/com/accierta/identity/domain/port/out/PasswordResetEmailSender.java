package com.accierta.identity.domain.port.out;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.ResetToken;

public interface PasswordResetEmailSender {

    void sendPasswordResetEmail(Email email, ResetToken resetToken);
}
