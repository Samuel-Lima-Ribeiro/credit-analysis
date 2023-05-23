package com.client.credit.analysis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import com.client.credit.analysis.apiclient.ApiClient;
import com.client.credit.analysis.apiclient.dto.ApiClientDto;
import com.client.credit.analysis.controller.request.CreditAnalysisRequest;
import com.client.credit.analysis.controller.response.CreditAnalysisResponse;
import com.client.credit.analysis.exception.NumberNotNegativeException;
import com.client.credit.analysis.mapper.AnalysisEntityMapper;
import com.client.credit.analysis.mapper.AnalysisEntityMapperImpl;
import com.client.credit.analysis.mapper.CreditAnalysisMapper;
import com.client.credit.analysis.mapper.CreditAnalysisMapperImpl;
import com.client.credit.analysis.mapper.CreditAnalysisReponseMapper;
import com.client.credit.analysis.mapper.CreditAnalysisReponseMapperImpl;
import com.client.credit.analysis.repository.CreditAnalysisRepository;
import com.client.credit.analysis.repository.entity.AnalysisEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor ArgumentCaptor<AnalysisEntity> analysisEntityArgumentCaptor;

    @Test
    void deve_aprovar_30Porcent_da_renda_quando_valor_solicitado_for_menor_ou_igual_50Porcent_da_renda() {
        final CreditAnalysisRequest creditRequest = creditAnalysisRequest30PorcentFactory();

        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDtoFactory());
        when(creditAnalysisRepository.save(analysisEntityArgumentCaptor.capture()))
                .thenReturn(analysisEntity30PorcentFactory());

        final CreditAnalysisResponse creditResponse = creditAnalysisService.create(creditRequest);
        final AnalysisEntity analysisEntity = analysisEntityArgumentCaptor.getValue();

        assertEquals(creditResponse.approved(), analysisEntity.getApproved());
        assertEquals(creditResponse.approvedLimit(), analysisEntity.getApprovedLimit());
        assertEquals(creditResponse.withdraw(), analysisEntity.getWithdraw());
    }

    @Test
    void deve_aprovar_15Porcent_da_renda_quando_valor_solicitado_for_maior_que_50Porcent_da_renda() {
        final CreditAnalysisRequest creditAnalysisRequest = creditAnalysisRequest15PorcentFactory();

        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDtoFactory());

        when(creditAnalysisRepository.save(analysisEntityArgumentCaptor.capture()))
                .thenReturn(analysisEntity15PorcentFactory());

        final CreditAnalysisResponse creditResponse = creditAnalysisService.create(creditAnalysisRequest);
        final AnalysisEntity analysisEntity = analysisEntityArgumentCaptor.getValue();

        assertEquals(creditResponse.approved(), analysisEntity.getApproved());
        assertEquals(creditResponse.approvedLimit(), analysisEntity.getApprovedLimit());
        assertEquals(creditResponse.withdraw(), analysisEntity.getWithdraw());
    }

    @Test
    void nao_deve_aprovar_limite_quando_pedido_for_maior_que_renda() {
        final CreditAnalysisRequest creditAnalysisRequest = CreditAnalysisRequest.builder()
                .monthlyIncome(BigDecimal.valueOf(1000.00))
                .requestedAmount(BigDecimal.valueOf(2000.00)).build();

        final AnalysisEntity analysisEntity = analysisEntityApprovedFactory();

        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDtoFactory());
        when(creditAnalysisRepository.save(analysisEntityArgumentCaptor.capture())).thenReturn(analysisEntity);

        final CreditAnalysisResponse creditAnalysisResponse = creditAnalysisService.create(creditAnalysisRequest);
        final AnalysisEntity analysis = analysisEntityArgumentCaptor.getValue();

        assertEquals(creditAnalysisResponse.approved(), analysis.getApproved());
        assertEquals(creditAnalysisResponse.withdraw(), analysis.getWithdraw());
        assertEquals(creditAnalysisResponse.approvedLimit(), analysis.getApprovedLimit());
        assertEquals(creditAnalysisResponse.annualInterest(), analysis.getAnnualInterest());
    }

    @Test
    void deve_lancar_NumberNotNegativeException_ao_solicitar_analise_com_numeros_negativos_ou_zero_na_renda() {
        CreditAnalysisRequest creditAnalysisRequest = CreditAnalysisRequest.builder()
                .requestedAmount(BigDecimal.valueOf(1000.0))
                .monthlyIncome(BigDecimal.valueOf(-100.00)).build();
        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDtoFactory());

        final NumberNotNegativeException numberNotNegativeException = assertThrows(NumberNotNegativeException.class, () ->
                creditAnalysisService.create(creditAnalysisRequest));

        assertEquals(numberNotNegativeException.getMessage(), "MonthlyIncome cannot be negative or zero");
    }

    @Test
    void deve_lancar_NumberNotNegativeException_ao_solicitar_analise_com_numeros_negativos_ou_zero_no_pedido() {
        CreditAnalysisRequest creditAnalysisRequest = CreditAnalysisRequest.builder().requestedAmount(BigDecimal.ZERO).monthlyIncome(
                BigDecimal.valueOf(1000.00)).build();
        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDtoFactory());

        final NumberNotNegativeException numberNotNegativeException = assertThrows(NumberNotNegativeException.class, () ->
                creditAnalysisService.create(creditAnalysisRequest));

        assertEquals(numberNotNegativeException.getMessage(), "AmountRequest cannot be negative or zero");
    }

    public static AnalysisEntity analysisEntityApprovedFactory() {
        return AnalysisEntity.builder()
                .approved(false)
                .approvedLimit(BigDecimal.ZERO)
                .withdraw(BigDecimal.ZERO)
                .annualInterest(BigDecimal.ZERO)
                .build();
    }

    public static CreditAnalysisRequest creditAnalysisRequest30PorcentFactory() {
        return CreditAnalysisRequest.builder()
                .clientId(UUID.fromString("438b2f95-4560-415b-98c2-9770cc1c4d93"))
                .monthlyIncome(BigDecimal.valueOf(3000.00))
                .requestedAmount(BigDecimal.valueOf(1500.00))
                .build();
    }

    public static CreditAnalysisRequest creditAnalysisRequest15PorcentFactory() {
        return CreditAnalysisRequest.builder()
                .clientId(UUID.fromString("438b2f95-4560-415b-98c2-9770cc1c4d93"))
                .monthlyIncome(BigDecimal.valueOf(3000.00))
                .requestedAmount(BigDecimal.valueOf(2000.00))
                .build();
    }

    public static ApiClientDto apiClientDtoFactory() {
        return  ApiClientDto.builder()
                .id(UUID.fromString("438b2f95-4560-415b-98c2-9770cc1c4d93"))
                .build();
    }

    public static AnalysisEntity analysisEntity30PorcentFactory() {
        return AnalysisEntity.builder()
                .approved(true)
                .approvedLimit(BigDecimal.valueOf(900.00).setScale(2))
                .withdraw(BigDecimal.valueOf(90.00).setScale(2))
                .annualInterest(BigDecimal.valueOf(15))
                .clientId(UUID.fromString("438b2f95-4560-415b-98c2-9770cc1c4d93"))
                .build();
    }

    public static AnalysisEntity analysisEntity15PorcentFactory() {
        return AnalysisEntity.builder()
                .approved(true)
                .approvedLimit(BigDecimal.valueOf(450.00).setScale(2))
                .withdraw(BigDecimal.valueOf(45.00).setScale(2))
                .annualInterest(BigDecimal.valueOf(15))
                .clientId(UUID.fromString("438b2f95-4560-415b-98c2-9770cc1c4d93"))
                .build();
    }
}