package com.client.credit.analysis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.client.credit.analysis.apiclient.ApiClient;
import com.client.credit.analysis.apiclient.dto.ApiClientDto;
import com.client.credit.analysis.controller.request.CreditAnalysisRequest;
import com.client.credit.analysis.controller.response.CreditAnalysisResponse;
import com.client.credit.analysis.exception.AnalysisNotFoundException;
import com.client.credit.analysis.exception.ClientNotFoundException;
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
import java.util.List;
import java.util.Optional;
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

    private static final UUID id = UUID.fromString("438b2f95-4560-415b-98c2-9770cc1c4d93");
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
    @Captor
    private ArgumentCaptor<AnalysisEntity> analysisEntityArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> cpfArgumentCaptor;

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
                .clientId(id)
                .monthlyIncome(BigDecimal.valueOf(3000.00))
                .requestedAmount(BigDecimal.valueOf(1500.00))
                .build();
    }

    public static CreditAnalysisRequest creditAnalysisRequest15PorcentFactory() {
        return CreditAnalysisRequest.builder()
                .clientId(id)
                .monthlyIncome(BigDecimal.valueOf(3000.00))
                .requestedAmount(BigDecimal.valueOf(2000.00))
                .build();
    }

    public static AnalysisEntity analysisEntityCalculateMaxFactory() {
        return AnalysisEntity.builder()
                .clientId(id)
                .approved(true)
                .approvedLimit(BigDecimal.valueOf(7500.00).setScale(2))
                .withdraw(BigDecimal.valueOf(750.00).setScale(2))
                .build();
    }

    public static ApiClientDto apiClientDtoFactory() {
        return ApiClientDto.builder()
                .id(id)
                .build();
    }

    public static AnalysisEntity analysisEntity30PorcentFactory() {
        return AnalysisEntity.builder()
                .approved(true)
                .approvedLimit(BigDecimal.valueOf(900.00).setScale(2))
                .withdraw(BigDecimal.valueOf(90.00).setScale(2))
                .annualInterest(BigDecimal.valueOf(15))
                .clientId(id)
                .build();
    }

    public static AnalysisEntity analysisEntity15PorcentFactory() {
        return AnalysisEntity.builder()
                .approved(true)
                .approvedLimit(BigDecimal.valueOf(450.00).setScale(2))
                .withdraw(BigDecimal.valueOf(45.00).setScale(2))
                .annualInterest(BigDecimal.valueOf(15))
                .clientId(id)
                .build();
    }

    @Test
    void deve_aprovar_30Porcento_da_renda_quando_valor_solicitado_for_menor_ou_igual_50Porcento_da_renda() {
        final CreditAnalysisRequest request = creditAnalysisRequest30PorcentFactory();

        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDtoFactory());
        when(creditAnalysisRepository.save(analysisEntityArgumentCaptor.capture()))
                .thenReturn(analysisEntity30PorcentFactory());

        final CreditAnalysisResponse response = creditAnalysisService.create(request);
        final AnalysisEntity entity = analysisEntityArgumentCaptor.getValue();

        assertEquals(response.approved(), entity.getApproved());
        assertEquals(response.approvedLimit(), entity.getApprovedLimit());
        assertEquals(response.withdraw(), entity.getWithdraw());
    }

    @Test
    void deve_aprovar_15Porcent_da_renda_quando_valor_solicitado_for_maior_que_50Porcent_da_renda() {
        final CreditAnalysisRequest request = creditAnalysisRequest15PorcentFactory();

        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDtoFactory());

        when(creditAnalysisRepository.save(analysisEntityArgumentCaptor.capture())).thenReturn(analysisEntity15PorcentFactory());

        final CreditAnalysisResponse response = creditAnalysisService.create(request);
        final AnalysisEntity entity = analysisEntityArgumentCaptor.getValue();

        assertEquals(response.approved(), entity.getApproved());
        assertEquals(response.approvedLimit(), entity.getApprovedLimit());
        assertEquals(response.withdraw(), entity.getWithdraw());
    }

    @Test
    void nao_deve_aprovar_limite_quando_pedido_for_maior_que_renda() {
        final CreditAnalysisRequest request = CreditAnalysisRequest.builder()
                .clientId(id)
                .monthlyIncome(BigDecimal.valueOf(1000.00))
                .requestedAmount(BigDecimal.valueOf(2000.00)).build();

        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDtoFactory());

        when(creditAnalysisRepository.save(analysisEntityArgumentCaptor.capture())).thenReturn(analysisEntityApprovedFactory());

        final CreditAnalysisResponse response = creditAnalysisService.create(request);
        final AnalysisEntity analysis = analysisEntityArgumentCaptor.getValue();

        assertEquals(response.approved(), analysis.getApproved());
        assertEquals(response.withdraw(), analysis.getWithdraw());
        assertEquals(response.approvedLimit(), analysis.getApprovedLimit());
        assertEquals(response.annualInterest(), analysis.getAnnualInterest());
    }

    @Test
    void deve_fazer_caculo_da_analise_utilizando_valor_maximo_da_renda_considerado() {
        CreditAnalysisRequest request = CreditAnalysisRequest.builder()
                .clientId(id)
                .monthlyIncome(BigDecimal.valueOf(100000.00))
                .requestedAmount(BigDecimal.valueOf(40000.00))
                .build();

        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDtoFactory());

        when(creditAnalysisRepository.save(analysisEntityArgumentCaptor.capture())).thenReturn(analysisEntityCalculateMaxFactory());

        final CreditAnalysisResponse response = creditAnalysisService.create(request);
        final AnalysisEntity entity = analysisEntityArgumentCaptor.getValue();

        assertEquals(response.approved(), entity.getApproved());
        assertEquals(response.approvedLimit(), entity.getApprovedLimit());
        assertEquals(response.withdraw(), entity.getWithdraw());
    }

    @Test
    void deve_lancar_NumberNotNegativeException_ao_solicitar_analise_com_numeros_negativos_ou_zero_na_renda() {
        CreditAnalysisRequest request = CreditAnalysisRequest.builder()
                .clientId(id)
                .requestedAmount(BigDecimal.valueOf(1000.0))
                .monthlyIncome(BigDecimal.valueOf(-100.00)).build();
        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDtoFactory());

        final NumberNotNegativeException numberNotNegativeException = assertThrows(NumberNotNegativeException.class,
                () -> creditAnalysisService.create(request));

        assertEquals("MonthlyIncome cannot be negative or zero", numberNotNegativeException.getMessage());
    }

    @Test
    void deve_lancar_ClientNotFoundException_ao_consultar_api_por_cliente_id_inexistente() {
        final ApiClientDto apiClientDto = ApiClientDto.builder().id(null).build();
        final CreditAnalysisRequest request = creditAnalysisRequest15PorcentFactory();

        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDto);
        ClientNotFoundException clientNotFoundException = assertThrows(ClientNotFoundException.class,
                () -> creditAnalysisService.create(request));

        assertEquals("Client not found by id 438b2f95-4560-415b-98c2-9770cc1c4d93", clientNotFoundException.getMessage());
    }

    @Test
    void deve_lancar_AnalysisNotFoundException_ao_consultar_por_id_de_analise_inexistente() {
        when(creditAnalysisRepository.findById(uuidArgumentCaptor.capture())).thenReturn(Optional.empty());
        AnalysisNotFoundException exception = assertThrows(AnalysisNotFoundException.class,
                () -> creditAnalysisService.getAnalysisById(id));

        assertEquals("Analysis not found by id %s".formatted(id), exception.getMessage());
    }

    @Test
    void deve_lancar_NumberNotNegativeException_ao_solicitar_analise_com_numeros_negativos_ou_zero_no_pedido() {
        CreditAnalysisRequest request = CreditAnalysisRequest.builder()
                .clientId(id)
                .requestedAmount(BigDecimal.ZERO).
                monthlyIncome(BigDecimal.valueOf(1000.00)).build();

        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDtoFactory());

        final NumberNotNegativeException numberNotNegativeException = assertThrows(NumberNotNegativeException.class,
                () -> creditAnalysisService.create(request));

        assertEquals("AmountRequest cannot be negative or zero", numberNotNegativeException.getMessage());
    }

    @Test
    void deve_retornar_vazio_quando_consultar_por_id_do_cliente_e_analise_nao_existir() {

        when(apiClient.getClientById(uuidArgumentCaptor.capture())).thenReturn(apiClientDtoFactory());
        when(creditAnalysisRepository.findByClientId(uuidArgumentCaptor.capture())).thenReturn(List.of());

        List<CreditAnalysisResponse> creditAnalysisResponses = creditAnalysisService.getAnalysisByClientId(id);

        assertEquals(List.of(), creditAnalysisResponses);
    }

    @Test
    void deve_lancar_ClientNotFoundException_quando_consultar_analise_pelo_cpf_do_cliente_e_cliente_nao_existir() {
        when(apiClient.getClientByCpf(cpfArgumentCaptor.capture())).thenReturn(List.of());

        ClientNotFoundException clientNotFoundException = assertThrows(ClientNotFoundException.class,
                () -> creditAnalysisService.getAnalysisByClientCpf("927.064.820-60"));

        assertEquals("92706482060", cpfArgumentCaptor.getValue());
        assertEquals("Client not found by cpf 92706482060", clientNotFoundException.getMessage());
    }

    @Test
    void deve_retornar_todas_analises_cadastradas_quando_nao_passar_nenhum_parametro() {
        final AnalysisEntity entity = analysisEntity30PorcentFactory();
        final List<AnalysisEntity> entities = List.of(entity, entity);
        when(creditAnalysisRepository.findAll()).thenReturn(entities);

        List<CreditAnalysisResponse> creditAnalysisResponses = creditAnalysisService.getAnalysisByClientId(null);
        assertEquals(entities.size(), creditAnalysisResponses.size());
    }

    @Test
    void deve_retornar_uma_lista_de_analises_de_credito_buscando_pelo_cpf_do_cliente() {
        final AnalysisEntity entity = analysisEntity30PorcentFactory();
        final List<AnalysisEntity> entities = List.of(entity, entity);
        when(apiClient.getClientByCpf(cpfArgumentCaptor.capture())).thenReturn(List.of(apiClientDtoFactory()));
        when(creditAnalysisRepository.findByClientId(uuidArgumentCaptor.capture())).thenReturn(entities);

        List<CreditAnalysisResponse> creditAnalysisResponses = creditAnalysisService.getAnalysisByClientCpf("927.064.820-60");
        assertEquals("92706482060", cpfArgumentCaptor.getValue());
        assertEquals(entities.size(), creditAnalysisResponses.size());
    }

    @Test
    void deve_retornar_uma_analise_de_credito_quando_consultada_pelo_id_da_analise() {
        final AnalysisEntity entity = analysisEntityApprovedFactory();
        when(creditAnalysisRepository.findById(uuidArgumentCaptor.capture())).thenReturn(Optional.ofNullable(entity));

        final CreditAnalysisResponse response = creditAnalysisService.getAnalysisById(id);

        assertEquals(id , uuidArgumentCaptor.getValue());
        assertEquals(entity.getId(), response.id());
        assertEquals(entity.getApproved(), response.approved());
        assertEquals(entity.getApprovedLimit(), response.approvedLimit());
        assertEquals(entity.getWithdraw(), response.withdraw());
        assertEquals(entity.getClientId(), response.clientId());
        assertEquals(entity.getDate(), response.date());
    }
}