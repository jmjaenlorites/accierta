package com.accierta.betting.application.query;

import com.accierta.betting.domain.model.PorraStatus;
import com.accierta.identity.domain.model.UserId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PorraQueryService {

    List<PorraSummaryView> listGroupPorras(UUID groupId, PorraStatus status, UserId requesterId);

    Optional<PorraDetailView> findPorra(UUID porraId, UserId requesterId);
}
