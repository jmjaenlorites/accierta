package com.accierta.betting.application.handler;

import com.accierta.betting.domain.model.PorraId;
import com.accierta.betting.domain.port.in.EnableResolutionCommand;
import com.accierta.betting.domain.port.in.EnableResolutionUseCase;
import com.accierta.betting.domain.port.out.PorraRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class EnableResolutionHandler implements EnableResolutionUseCase {

    private final PorraRepository porraRepository;
    private final Clock clock;

    public EnableResolutionHandler(PorraRepository porraRepository, Clock clock) {
        this.porraRepository = Objects.requireNonNull(porraRepository, "porraRepository cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void enable(EnableResolutionCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        var porra = porraRepository.findById(new PorraId(command.porraId()))
                .orElseThrow(() -> new java.util.NoSuchElementException("porra not found"));
        porra.enableResolution(Instant.now(clock));
        porraRepository.save(porra);
    }
}
