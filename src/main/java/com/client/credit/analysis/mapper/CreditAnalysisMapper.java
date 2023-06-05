package com.client.credit.analysis.mapper;

import com.client.credit.analysis.controller.request.CreditAnalysisRequest;
import com.client.credit.analysis.model.CreditAnalysis;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreditAnalysisMapper {
    CreditAnalysis from(CreditAnalysisRequest creditAnalysisRequest);
}
