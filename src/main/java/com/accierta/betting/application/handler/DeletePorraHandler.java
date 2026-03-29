package com.accierta.betting.application.handler;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.port.in.DeletePorraCommand;
import com.accierta.betting.domain.port.in.DeletePorraUseCase;
import com.accierta.betting.domain.port.out.BetCounter;
import com.accierta.betting.domain.port.out.PorraRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class DeletePorraHandler implements DeletePorraUseCase {

    private final PorraRepository porraRepository;
    private final BetCounter betCounter;
    private final Clock clock;

    public DeletePorraHandler(
            PorraRepository porraRepository,
            BetCounter betCounter,
            Clock clock) {
        this.porraRepository = Objects.requireNonNull(porraRepository, "porraRepository cannot be null");
        this.betCounter = Objects.requireNonNull(betCounter, "betCounter cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void delete(DeletePorraCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        var porra = porraRepository.findById(new PorraId(command.porraId()))
                .orElseThrow(() -> new java.util.NoSuchElementException("porra not found"));
        porra.deleteByCreator(command.requesterId(), Instant.now(clock), betCounter.countBets(new PorraId(command.porraId())));
        porraRepository.save(porra);
    }
}
