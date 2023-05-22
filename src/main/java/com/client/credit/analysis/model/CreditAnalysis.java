package com.client.credit.analysis.model;

import com.client.credit.analysis.apiclient.dto.ApiClientDto;
import com.client.credit.analysis.utils.ValidationCustom;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

public record CreditAnalysis(
        Boolean approved,
        @PositiveOrZero(message = "approvedLimit negative")
        BigDecimal approvedLimit,
        @PositiveOrZero(message = "withdraw negative")
        BigDecimal withdraw,
        BigDecimal annualInterest,
        UUID clientId

) {
    @Builder(toBuilder = true)
    public CreditAnalysis(Boolean approved, BigDecimal approvedLimit,
                          BigDecimal withdraw, BigDecimal annualInterest,
                          UUID clientId) {
        this.approved = approved;
        this.approvedLimit = approvedLimit;
        this.withdraw = withdraw;
        this.annualInterest = annualInterest;
        this.clientId = clientId;
        ValidationCustom.validator(this);
    }

    public CreditAnalysis updateFromAnalysis(CreditAnalysis analysis) {
        return this.toBuilder()
                .approved(analysis.approved)
                .approvedLimit(analysis.approvedLimit)
                .withdraw(analysis.withdraw)
                .annualInterest(analysis.annualInterest)
                .build();
    }

    public CreditAnalysis updateFromClient(ApiClientDto client) {
        return this.toBuilder()
                .clientId(client.id())
                .build();
    }
}
