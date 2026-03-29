package com.accierta.identity.domain.port.out;

import com.accierta.identity.domain.event.LoginFailed;

public interface AuthenticationFailureLogger {

    void log(LoginFailed event);
}
