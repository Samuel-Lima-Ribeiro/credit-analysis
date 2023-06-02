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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditAnalysisService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreditAnalysisService.class);

    private final CreditAnalysisRepository creditAnalysisRepository;
    private final AnalysisEntityMapper analysisEntityMapper;
    private final ApiClient apiClient;
    private final CreditAnalysisReponseMapper creditAnalysisReponseMapper;
    private final CreditAnalysisMapper creditAnalysisMapper;

    private static String formatCpf(String cpf) {
        return cpf.replaceAll("[-.]", "");
    }

    public CreditAnalysisResponse create(CreditAnalysisRequest creditAnalysisRequest) {
        final CreditAnalysis creditAnalysis = creditAnalysisMapper.from(creditAnalysisRequest);
        // Pq o clientid é transformado em string?
        final ApiClientDto apiClientDto = searchClient(String.valueOf(creditAnalysis.clientId()));
        // Não tem necessidade deste log
        LOGGER.info("Cliente encontrado");
        // Não tem necessidade deste log
        LOGGER.info("Fazendo análise de credito");
        final CreditAnalysis creditAnalysisUpdateAnalysis = creditAnalysis.updateFromAnalysis(analisar(creditAnalysisRequest));

        final CreditAnalysis creditAnalysisUpdateClient = creditAnalysisUpdateAnalysis.updateFromClient(apiClientDto);

        final AnalysisEntity analysisEntity = analysisEntityMapper.from(creditAnalysisUpdateClient);
        // Não tem necessidade deste log
        LOGGER.info("Salvando análise");
        final AnalysisEntity analysisSaved = creditAnalysisRepository.save(analysisEntity);

        return creditAnalysisReponseMapper.from(analysisSaved);
    }

    public CreditAnalysis analisar(CreditAnalysisRequest request) {
        final BigDecimal requestedAmount = request.requestedAmount();
        final BigDecimal monthlyIncome = request.monthlyIncome();
        final int checkingRequestedAmount = requestedAmount.compareTo(BigDecimal.ZERO);
        final int checkingMonthlyIncome = monthlyIncome.compareTo(BigDecimal.ZERO);

        if (checkingRequestedAmount <= 0) {
            throw new NumberNotNegativeException("AmountRequest cannot be negative or zero");
        } else if (checkingMonthlyIncome <= 0) {
            throw new NumberNotNegativeException("MonthlyIncome cannot be negative or zero");
        }

        final int checkingRequestAmountGreaterThanMonthlyIncome = requestedAmount.compareTo(monthlyIncome);

        if (checkingRequestAmountGreaterThanMonthlyIncome > 0) {
            LOGGER.info("Análise não aprovada");
            return CreditAnalysis.builder().approved(false).approvedLimit(BigDecimal.ZERO).withdraw(BigDecimal.ZERO).annualInterest(BigDecimal.ZERO)
                    .build();
        }
        BigDecimal monthlyIncomeLimitForCalculate = monthlyIncome;
        // BigDecimal.valueOf(50000) é uma constante
        final BigDecimal amountLimit = BigDecimal.valueOf(50000);
        final int checkingMonthlyIncomeValue = monthlyIncome.compareTo(amountLimit);

        if (checkingMonthlyIncomeValue > 0) {
            monthlyIncomeLimitForCalculate = amountLimit;
        }
        //  BigDecimal.valueOf(0.50) é uma constante
        final BigDecimal fiftyPercentOfIncome = monthlyIncomeLimitForCalculate.multiply(BigDecimal.valueOf(0.50));

        final BigDecimal approvalLimitPercentage;

        if (requestedAmount.compareTo(fiftyPercentOfIncome) > 0) {
            // constante
            final BigDecimal percentageToCalculateLimit = BigDecimal.valueOf(0.15);
            approvalLimitPercentage = percentageToCalculateLimit;
        } else {
            // constante
            final BigDecimal percentageToCalculateLimit = BigDecimal.valueOf(0.30);
            approvalLimitPercentage = percentageToCalculateLimit;
        }

        final BigDecimal approvedLimit = monthlyIncomeLimitForCalculate.multiply(approvalLimitPercentage).setScale(2, RoundingMode.HALF_EVEN);

        // constante
        final BigDecimal withdrawalLimitPercentage = BigDecimal.valueOf(0.10);

        // constante
        final BigDecimal annualInterest = BigDecimal.valueOf(15);

        final BigDecimal withdraw = approvedLimit.multiply(withdrawalLimitPercentage).setScale(2, RoundingMode.HALF_EVEN);
        LOGGER.info("Análise aprovada");
        return CreditAnalysis.builder().approved(true).approvedLimit(approvedLimit).withdraw(withdraw).annualInterest(annualInterest).build();
    }

    // criar consultas especificas, não utilize atributos com multiplos significados
    public ApiClientDto searchClient(String id) {
        final int LengthMaxCpf = 15;
        try {
            if (id.length() < LengthMaxCpf) {
                final String idFormat = formatCpf(id);
                // Log desnecessario
                LOGGER.info("Buscando na api o cliente do cpf %s".formatted(idFormat));

                return apiClient.getClientByCpf(idFormat)
                        .orElseThrow(() -> new ClientNotFoundException("Client not found by cpf %s".formatted(idFormat)));
            } else {
                // seu codigo pode quebrar aqui
                final UUID idFormated = UUID.fromString(id);
                // Log desnecessario
                LOGGER.info("Buscando na api o cliente do ID %s".formatted(idFormated));
                return apiClient.getClientById(idFormated);
            }

        } catch (FeignException e) {
            // PQ esta exceção é lançada aqui?
            throw new ClientNotFoundException("Client not found by id %s".formatted(id));
        }
    }

    public List<CreditAnalysisResponse> findAllAnalysis() {
        LOGGER.info("Mostrando todas análises cadastradas");
        final List<AnalysisEntity> analysis;
        analysis = creditAnalysisRepository.findAll();
        return analysis.stream().map(creditAnalysisReponseMapper::from).collect(Collectors.toList());
    }

    public CreditAnalysisResponse getAnalysisById(UUID id) {
        // log desnecessario
        LOGGER.info("Consultando análise pelo id %s".formatted(id));
        final AnalysisEntity analysis =
                creditAnalysisRepository.findById(id).orElseThrow(() -> new AnalysisNotFoundException("Analysis not found by id %s".formatted(id)));
        // log desnecessario
        LOGGER.info("Análise encontrada");
        return creditAnalysisReponseMapper.from(analysis);
    }

    public List<CreditAnalysisResponse> getAnalysisByClient(String id) {
        final ApiClientDto client = searchClient(id);
        // log desnecessario
        LOGGER.info("Cliente encontrado, consultando análises pelo ID do cliente");

        final List<AnalysisEntity> analysis;
        analysis = creditAnalysisRepository.findByClientId(client.id());

        if (analysis.isEmpty()) {
            throw new AnalysisNotFoundException("Analysis not found by client ID %s".formatted(client.id()));
        }
        // log desnecessario
        LOGGER.info("Retornando análises encontradas");
        return analysis.stream().map(creditAnalysisReponseMapper::from).collect(Collectors.toList());
    }
}
