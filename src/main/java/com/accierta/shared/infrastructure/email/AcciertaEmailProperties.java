package com.accierta.shared.infrastructure.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "accierta.email")
public record AcciertaEmailProperties(
        String apiKey,
        String from,
        String subjectPrefix,
        String verificationBaseUrl,
        String resetPasswordBaseUrl) {

    public AcciertaEmailProperties {
        subjectPrefix = subjectPrefix == null ? "" : subjectPrefix;
    }
}
