package com.accierta.settlement.api.rest;

import com.accierta.identity.api.dto.MessageResponse;
import com.accierta.settlement.application.query.PersonalDebtsView;
import com.accierta.settlement.application.query.SettlementQueryService;
import com.accierta.settlement.domain.port.in.RequestPaymentCommand;
import com.accierta.settlement.domain.port.in.RequestPaymentUseCase;
import com.accierta.settlement.domain.port.in.SettleDebtCommand;
import com.accierta.settlement.domain.port.in.SettleDebtUseCase;
import com.accierta.shared.security.JwtCurrentUserService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class DebtsController {

    private final SettlementQueryService settlementQueryService;
    private final RequestPaymentUseCase requestPaymentUseCase;
    private final SettleDebtUseCase settleDebtUseCase;
    private final JwtCurrentUserService jwtCurrentUserService;

    public DebtsController(
            SettlementQueryService settlementQueryService,
            RequestPaymentUseCase requestPaymentUseCase,
            SettleDebtUseCase settleDebtUseCase,
            JwtCurrentUserService jwtCurrentUserService) {
        this.settlementQueryService = settlementQueryService;
        this.requestPaymentUseCase = requestPaymentUseCase;
        this.settleDebtUseCase = settleDebtUseCase;
        this.jwtCurrentUserService = jwtCurrentUserService;
    }

    @GetMapping("/api/groups/{groupId}/debts/mine")
    public PersonalDebtsView mine(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID groupId) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        return settlementQueryService.getMyDebts(groupId, currentUser.userId().value());
    }

    @PostMapping("/api/debts/{debtId}/request-payment")
    public MessageResponse requestPayment(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID debtId) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        requestPaymentUseCase.request(new RequestPaymentCommand(debtId, currentUser.userId()));
        return new MessageResponse("Payment confirmation requested");
    }

    @PostMapping("/api/debts/{debtId}/settle")
    public MessageResponse settle(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID debtId) {
        var currentUser = jwtCurrentUserService.requireUser(authorizationHeader);
        settleDebtUseCase.settle(new SettleDebtCommand(debtId, currentUser.userId()));
        return new MessageResponse("Debt settled");
    }
}
