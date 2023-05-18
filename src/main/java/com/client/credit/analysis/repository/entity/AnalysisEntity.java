package com.client.credit.analysis.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "ANALYSIS")
@Immutable
public class AnalysisEntity {
    @Id
    UUID id;
    Boolean approved;
    BigDecimal approvedLimit;
    BigDecimal withdraw;
    BigDecimal annualInterest;
    UUID clientId;
    String clientCpf;
    @CreationTimestamp
    @Column(name = "date")
    LocalDateTime date;

    private AnalysisEntity() {

    }

    @Builder
    public AnalysisEntity(Boolean approved, BigDecimal approvedLimit, BigDecimal withdraw, BigDecimal annualInterest, UUID clientId,
                          String clientCpf) {
        this.id = UUID.randomUUID();
        this.approved = approved;
        this.approvedLimit = approvedLimit;
        this.withdraw = withdraw;
        this.annualInterest = annualInterest;
        this.clientId = clientId;
        this.clientCpf = clientCpf;
    }

    @Override
    public String toString() {
        return "AnalysisEntity{" + "id=" + id + ", approved=" + approved + ", approvedLimit=" + approvedLimit + ", withdraw=" + withdraw
                + ", annualInterest=" + annualInterest + ", clientId=" + clientId + ", clientCpf='" + clientCpf + '\'' + ", date=" + date + '}';
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public BigDecimal getApprovedLimit() {
        return approvedLimit;
    }

    public void setApprovedLimit(BigDecimal approvedLimit) {
        this.approvedLimit = approvedLimit;
    }

    public BigDecimal getWithdraw() {
        return withdraw;
    }

    public void setWithdraw(BigDecimal withdraw) {
        this.withdraw = withdraw;
    }

    public BigDecimal getAnnualInterest() {
        return annualInterest;
    }

    public void setAnnualInterest(BigDecimal annualInterest) {
        this.annualInterest = annualInterest;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getClientCpf() {
        return clientCpf;
    }

    public void setClientCpf(String clientCpf) {
        this.clientCpf = clientCpf;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
