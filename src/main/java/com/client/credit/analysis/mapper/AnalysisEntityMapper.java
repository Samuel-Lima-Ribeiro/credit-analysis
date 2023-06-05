package com.client.credit.analysis.mapper;

import com.client.credit.analysis.model.CreditAnalysis;
import com.client.credit.analysis.repository.entity.AnalysisEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnalysisEntityMapper {
    AnalysisEntity from(CreditAnalysis creditAnalysis);
}
