package com.client.credit.analysis.repository;

import com.client.credit.analysis.repository.entity.AnalysisEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditAnalysisRepository extends JpaRepository<AnalysisEntity, UUID> {
}
