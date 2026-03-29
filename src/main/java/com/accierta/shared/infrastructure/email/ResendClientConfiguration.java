package com.accierta.shared.infrastructure.email;

import com.resend.Resend;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Creates the Resend client bean when running with prod or staging profiles.
 * In dev (default profile), no Resend client is created — the logging adapters handle email.
 */
@Configuration
@EnableConfigurationProperties(AcciertaEmailProperties.class)
@Profile({"prod", "staging"})
public class ResendClientConfiguration {

    @Bean
    Resend resend(AcciertaEmailProperties emailProperties) {
        return new Resend(emailProperties.apiKey());
    }
}
