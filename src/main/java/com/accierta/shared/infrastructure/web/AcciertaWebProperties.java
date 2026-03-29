package com.accierta.shared.infrastructure.web;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "accierta.web")
public record AcciertaWebProperties(List<String> allowedOrigins) {

    public AcciertaWebProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }

    public String[] allowedOriginsArray() {
        return allowedOrigins.toArray(String[]::new);
    }
}
