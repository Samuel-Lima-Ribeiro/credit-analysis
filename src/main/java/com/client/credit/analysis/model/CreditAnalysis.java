package com.client.credit.analysis.model;

//import com.client.credit.analysis.apiclient.dto.ApiClientDto;
import com.client.credit.analysis.apiclient.dto.ApiClientDto;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

// Falta id
public record CreditAnalysis(
        // APROVOU OU N
        Boolean approved,
        // LIMITE APROVADO
        BigDecimal approvedLimit,
        //
        BigDecimal withdraw,
        BigDecimal annualInterest,
        UUID clientId,
        String clientCpf

) {
    @Builder(toBuilder = true)
    public CreditAnalysis(Boolean approved, BigDecimal approvedLimit,
                          BigDecimal withdraw, BigDecimal annualInterest,
                          UUID clientId, String clientCpf) {
        this.approved = approved;
        this.approvedLimit = approvedLimit;
        this.withdraw = withdraw;
        this.annualInterest = annualInterest;
        this.clientId = clientId;
        this.clientCpf = clientCpf;
    }

    public CreditAnalysis updateFromClient(ApiClientDto client) {
        return this.toBuilder()
                .clientId(client.id())
                .clientCpf(client.cpf())
                .build();
    }
}
