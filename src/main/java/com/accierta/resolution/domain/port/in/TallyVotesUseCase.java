package com.accierta.resolution.domain.port.in;

public interface TallyVotesUseCase {

    void tally(TallyVotesCommand command);
}
