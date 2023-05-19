package com.client.credit.analysis.service;

import com.client.credit.analysis.apiclient.ApiClient;
import com.client.credit.analysis.apiclient.dto.ApiClientDto;
import com.client.credit.analysis.controller.request.CreditAnalysisRequest;
import com.client.credit.analysis.controller.response.CreditAnalysisResponse;
import com.client.credit.analysis.exception.AnalysisNotFoundException;
import com.client.credit.analysis.exception.ClientNotFoundException;
import com.client.credit.analysis.exception.NumberNotNegativeException;
import com.client.credit.analysis.mapper.AnalysisEntityMapper;
import com.client.credit.analysis.mapper.CreditAnalysisMapper;
import com.client.credit.analysis.mapper.CreditAnalysisReponseMapper;
import com.client.credit.analysis.model.CreditAnalysis;
import com.client.credit.analysis.repository.CreditAnalysisRepository;
import com.client.credit.analysis.repository.entity.AnalysisEntity;
import feign.FeignException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditAnalysisService {

    private final CreditAnalysisMapper creditAnalysisMapper;

    private final CreditAnalysisRepository creditAnalysisRepository;
    private final AnalysisEntityMapper analysisEntityMapper;
    private final ApiClient apiClient;
    private final CreditAnalysisReponseMapper creditAnalysisReponseMapper;

    private static String formatCpf(String cpf) {
        return cpf.replaceAll("[-.]", "");
    }

    public CreditAnalysisResponse create(CreditAnalysisRequest creditAnalysisRequest) {
        // Entity salva cpf e id, salvo tudo no model, model salvo pra entity, entiy pra response tiro oq n me interessa

        // segundo vejo se tem o client ou não
        final ApiClientDto apiClientDto = searchClient(creditAnalysisRequest.clientId());

        // Terceiro faço analise e já buildo
        final CreditAnalysis creditAnalysis = analisar(creditAnalysisRequest);

        System.out.println("Depois da analise foi isso que ocorreu: " + creditAnalysis);

        // por último atualizo meu client com o cpf se tudo tiver ok
        final CreditAnalysis creditAnalysisUpdateClient = creditAnalysis.updateFromClient(apiClientDto);
        System.out.println("Cliente Atualizado " + creditAnalysisUpdateClient);

        //Transformo em entity agora
        final AnalysisEntity analysisEntity = analysisEntityMapper.from(creditAnalysisUpdateClient);
        final AnalysisEntity analysisSaved = creditAnalysisRepository.save(analysisEntity);

        System.out.println("Está salvando esse cara aq " + analysisSaved);
        final CreditAnalysisResponse creditAnalysisResponse = creditAnalysisReponseMapper.from(analysisSaved);
        System.out.println("Retorna isso + " + creditAnalysisResponse);

        return creditAnalysisReponseMapper.from(analysisSaved);
    }

    public CreditAnalysis analisar(CreditAnalysisRequest request) {
        final BigDecimal requestedAmount = request.requestedAmount();
        final BigDecimal monthlyIncome = request.monthlyIncome();
        final int checkingRequestedAmount = requestedAmount.compareTo(BigDecimal.ZERO);
        final int checkingMonthlyIncome = monthlyIncome.compareTo(BigDecimal.ZERO);

        //Verifico se os números são negativos
        if (checkingRequestedAmount <= 0) {
            throw new NumberNotNegativeException("AmountRequest cannot be negative or zero");
        } else if (checkingMonthlyIncome <= 0) {
            throw new NumberNotNegativeException("MonthlyIncome cannot be negative or zero");
        }

        //Talvez esse daq seja da linha 27, ou colocar isso para validar dps do MaiorQue
        //Verifico se o limite pasosu de 50, entao vira 50
        BigDecimal monthlyIncomeLimitForCalculate = monthlyIncome;
        final BigDecimal amountLimit = BigDecimal.valueOf(50000);
        final int checkingMonthlyIncomeValue = monthlyIncome.compareTo(amountLimit);

        if (checkingMonthlyIncomeValue > 0) {
            monthlyIncomeLimitForCalculate = amountLimit;
            System.out.println("Reatribuindo valor do amountRequest");
        }

        final int checkingRequestAmountGreaterThanMonthlyIncome = requestedAmount.compareTo(monthlyIncome);

        // verifico se pedido é maior que renda
        if (checkingRequestAmountGreaterThanMonthlyIncome > 0) {
            System.out.println("Request foi maior que o salario");

            return CreditAnalysis.builder()
                    .approved(false)
                    .build();
        }

        // Verificando se o pedido é maior que 50 porcento da renda ou n
        final BigDecimal fiftyPercentOfIncome = monthlyIncomeLimitForCalculate.multiply(BigDecimal.valueOf(0.50));
        final BigDecimal approvalLimitPercentage;

        if (requestedAmount.compareTo(fiftyPercentOfIncome) > 0) {
            final BigDecimal percentageToCalculateLimit = BigDecimal.valueOf(0.15);
            approvalLimitPercentage = percentageToCalculateLimit;
            System.out.println("Caiu no maior que 50, retorna 15");
        } else {
            final BigDecimal percentageToCalculateLimit = BigDecimal.valueOf(0.30);
            approvalLimitPercentage = percentageToCalculateLimit;
            System.out.println("Caiu no maior que 30, retorna 30");
        }

        final BigDecimal approvedLimit = monthlyIncomeLimitForCalculate.multiply(approvalLimitPercentage).setScale(2, RoundingMode.HALF_EVEN);

        // limite saque
        final BigDecimal withdrawalLimitPercentage = BigDecimal.valueOf(0.10);

        // taxa anual
        final BigDecimal annualInterest = BigDecimal.valueOf(0.15);

        final BigDecimal withdraw = approvedLimit.multiply(withdrawalLimitPercentage).setScale(2, RoundingMode.HALF_EVEN);
        System.out.printf("Limite aprovado do quantia mensal %.2f do pedido %.2f foi de %.2f limite do saque 10%% %.2f %n", monthlyIncome,
                requestedAmount, approvedLimit, withdraw);

        return CreditAnalysis.builder()
                .approved(true)
                .approvedLimit(approvedLimit)
                .withdraw(withdraw)
                .annualInterest(annualInterest)
                .build();
    }

    public void save() {

    }

    public ApiClientDto searchClient(UUID id) {
        try {
            final ApiClientDto apiClientDto = apiClient.getClient(id);
            return apiClientDto;
        } catch (FeignException e) {
            throw new ClientNotFoundException("Client not found by id %s".formatted(id));
        }
    }

    public List<CreditAnalysisResponse> findAllClients() {
        final List<AnalysisEntity> analysis;
        analysis = creditAnalysisRepository.findAll();
        return analysis.stream()
                .map(creditAnalysisReponseMapper::from)
                .collect(Collectors.toList());
    }

    public CreditAnalysisResponse getAnalysisById(UUID id) {
        final AnalysisEntity analysis = creditAnalysisRepository.findById(id)
                .orElseThrow(() ->
                        new AnalysisNotFoundException("Analysis not found by id %s".formatted(id)));
        return creditAnalysisReponseMapper.from(analysis);
    }

    public List<CreditAnalysisResponse> getAnalysisByClient(String id) {
        final Integer LengthMaxCpf = 15;
        final List<AnalysisEntity> analysis;
        if (id.length() < LengthMaxCpf) {
            id = formatCpf(id);
            analysis = creditAnalysisRepository.findByClientCpf(id);
        } else {
            analysis = creditAnalysisRepository.findByClientId(UUID.fromString(id));
        }
        return analysis.stream()
                .map(creditAnalysisReponseMapper::from)
                .collect(Collectors.toList());
    }
}
