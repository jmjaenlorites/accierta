package com.accierta.betting.api.dto;

import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.betting.domain.model.PorraType;
import java.util.UUID;

public record CreatePorraResponse(
        UUID porraId,
        String title,
        PorraType type,
        PorraStatus status) {
}
