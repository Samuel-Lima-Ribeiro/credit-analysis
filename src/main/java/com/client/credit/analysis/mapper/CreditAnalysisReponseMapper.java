package com.client.credit.analysis.mapper;

import com.client.credit.analysis.controller.response.CreditAnalysisResponse;
import com.client.credit.analysis.repository.entity.AnalysisEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreditAnalysisReponseMapper {
    CreditAnalysisResponse from(AnalysisEntity analysisEntity);
}
