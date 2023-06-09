package com.client.credit.analysis.controller.request;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

public record CreditAnalysisRequest(

        UUID clientId,
        BigDecimal monthlyIncome,
        BigDecimal requestedAmount
) {
    @Builder
    public CreditAnalysisRequest {

    }
}
