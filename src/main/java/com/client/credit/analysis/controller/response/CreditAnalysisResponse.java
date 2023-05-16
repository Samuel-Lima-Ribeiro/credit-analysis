package com.client.credit.analysis.controller.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditAnalysisResponse(
        String id,
        Boolean approved,
        BigDecimal approvedLimit,
        BigDecimal withdraw,
        Double annualInterest,
        UUID clientId,
        LocalDateTime date
) {
}
