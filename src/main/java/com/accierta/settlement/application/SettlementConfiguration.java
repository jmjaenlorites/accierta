package com.accierta.settlement.application;

import com.accierta.settlement.application.handler.RequestPaymentHandler;
import com.accierta.settlement.application.handler.SettleDebtHandler;
import com.accierta.settlement.domain.port.in.RequestPaymentUseCase;
import com.accierta.settlement.domain.port.in.SettleDebtUseCase;
import com.accierta.settlement.domain.port.out.DebtRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettlementConfiguration {

    @Bean
    RequestPaymentUseCase requestPaymentUseCase(DebtRepository debtRepository, Clock clock) {
        return new RequestPaymentHandler(debtRepository, clock);
    }

    @Bean
    SettleDebtUseCase settleDebtUseCase(DebtRepository debtRepository, Clock clock) {
        return new SettleDebtHandler(debtRepository, clock);
    }
}
