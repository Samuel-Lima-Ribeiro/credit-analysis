package com.client.credit.analysis.controller.request;

import java.math.BigDecimal;

public record CreditAnalysisRequest(
        String clientId,
        BigDecimal monthlyIncome,
        BigDecimal requestedAmount
) {
}
