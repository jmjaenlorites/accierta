package com.accierta.groups.domain.port.out;

import com.accierta.identity.domain.model.UserId;

public interface VerifiedUserChecker {

    boolean isVerified(UserId userId);
}
