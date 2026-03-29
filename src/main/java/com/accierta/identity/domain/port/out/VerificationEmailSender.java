package com.accierta.identity.domain.port.out;

import com.accierta.identity.domain.model.Email;
import com.accierta.identity.domain.model.VerificationToken;

public interface VerificationEmailSender {

    void sendVerificationEmail(Email email, VerificationToken verificationToken);
}
