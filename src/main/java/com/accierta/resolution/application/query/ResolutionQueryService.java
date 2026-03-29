package com.accierta.resolution.application.query;

import java.util.Optional;
import java.util.UUID;

public interface ResolutionQueryService {

    Optional<ResolutionView> findByPorraId(UUID porraId);

    Optional<ResolutionVotesView> findVotesByPorraId(UUID porraId);
}
