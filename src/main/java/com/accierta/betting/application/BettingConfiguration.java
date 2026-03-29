package com.accierta.betting.application;

import com.accierta.betting.application.handler.CloseBettingHandler;
import com.accierta.betting.application.handler.CreatePorraHandler;
import com.accierta.betting.application.handler.DeletePorraHandler;
import com.accierta.betting.application.handler.EnableResolutionHandler;
import com.accierta.betting.application.handler.PlaceBetHandler;
import com.accierta.betting.domain.port.in.CloseBettingUseCase;
import com.accierta.betting.domain.port.in.CreatePorraUseCase;
import com.accierta.betting.domain.port.in.DeletePorraUseCase;
import com.accierta.betting.domain.port.in.EnableResolutionUseCase;
import com.accierta.betting.domain.port.in.PlaceBetUseCase;
import com.accierta.betting.domain.port.out.BetCounter;
import com.accierta.betting.domain.port.out.GroupMemberChecker;
import com.accierta.betting.domain.port.out.PorraRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BettingConfiguration {

    @Bean
    CreatePorraUseCase createPorraUseCase(
            PorraRepository porraRepository,
            GroupMemberChecker groupMemberChecker,
            Clock clock) {
        return new CreatePorraHandler(porraRepository, groupMemberChecker, clock);
    }

    @Bean
    DeletePorraUseCase deletePorraUseCase(
            PorraRepository porraRepository,
            BetCounter betCounter,
            Clock clock) {
        return new DeletePorraHandler(porraRepository, betCounter, clock);
    }

    @Bean
    PlaceBetUseCase placeBetUseCase(
            PorraRepository porraRepository,
            GroupMemberChecker groupMemberChecker,
            BetCounter betCounter,
            Clock clock) {
        return new PlaceBetHandler(porraRepository, groupMemberChecker, betCounter, clock);
    }

    @Bean
    CloseBettingUseCase closeBettingUseCase(
            PorraRepository porraRepository,
            Clock clock) {
        return new CloseBettingHandler(porraRepository, clock);
    }

    @Bean
    EnableResolutionUseCase enableResolutionUseCase(
            PorraRepository porraRepository,
            Clock clock) {
        return new EnableResolutionHandler(porraRepository, clock);
    }
}
