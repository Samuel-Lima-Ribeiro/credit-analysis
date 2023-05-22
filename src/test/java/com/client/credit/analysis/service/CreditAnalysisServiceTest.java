package com.client.credit.analysis.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import com.client.credit.analysis.apiclient.ApiClient;
import com.client.credit.analysis.controller.request.CreditAnalysisRequest;
import com.client.credit.analysis.mapper.AnalysisEntityMapper;
import com.client.credit.analysis.mapper.AnalysisEntityMapperImpl;
import com.client.credit.analysis.mapper.CreditAnalysisMapper;
import com.client.credit.analysis.mapper.CreditAnalysisMapperImpl;
import com.client.credit.analysis.mapper.CreditAnalysisReponseMapper;
import com.client.credit.analysis.mapper.CreditAnalysisReponseMapperImpl;
import com.client.credit.analysis.repository.CreditAnalysisRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CreditAnalysisServiceTest {

    @Mock
    private CreditAnalysisRepository creditAnalysisRepository;
    @Mock
    private ApiClient apiClient;
    @Spy
    private AnalysisEntityMapper analysisEntityMapper = new AnalysisEntityMapperImpl();
    @Spy
    private CreditAnalysisMapper creditAnalysisMapper = new CreditAnalysisMapperImpl();
    @Spy
    private CreditAnalysisReponseMapper creditAnalysisReponseMapper = new CreditAnalysisReponseMapperImpl();

    @InjectMocks
    private CreditAnalysisService creditAnalysisService;

    @Test
    void nao_deve_aprovar_limite_quando_pedido_for_maior_que_renda() {
        when()
    }

    public static CreditAnalysisRequest creditAnalysisRequestFactory() {
        return CreditAnalysisRequest.builder()
                .clientId(UUID.fromString("438b2f95-4560-415b-98c2-9770cc1c4d93"))
                .monthlyIncome(BigDecimal.valueOf(10000.00))
                .requestedAmount(BigDecimal.valueOf(20000.00))
                .build();
    }
}