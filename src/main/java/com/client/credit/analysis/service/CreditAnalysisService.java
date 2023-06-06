package com.client.credit.analysis.service;

import com.client.credit.analysis.apiclient.ApiClient;
import com.client.credit.analysis.apiclient.dto.ApiClientDto;
import com.client.credit.analysis.controller.request.CreditAnalysisRequest;
import com.client.credit.analysis.controller.response.CreditAnalysisResponse;
import com.client.credit.analysis.exception.AnalysisNotFoundException;
import com.client.credit.analysis.exception.ClientNotFoundException;
import com.client.credit.analysis.exception.NumberNotNegativeException;
import com.client.credit.analysis.mapper.AnalysisEntityMapper;
import com.client.credit.analysis.mapper.CreditAnalysisReponseMapper;
import com.client.credit.analysis.model.CreditAnalysis;
import com.client.credit.analysis.repository.CreditAnalysisRepository;
import com.client.credit.analysis.repository.entity.AnalysisEntity;
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
    static final BigDecimal AMOUNT_LIMIT = BigDecimal.valueOf(50000);
    static final BigDecimal WITHDRAWAL_LIMIT_PERCENTAGE = BigDecimal.valueOf(0.10);
    static final BigDecimal ANNUAL_INTEREST = BigDecimal.valueOf(15);
    static final BigDecimal APPROVAL_LIMIT_FIFTEEN_PERCENT_OF_INCOME = BigDecimal.valueOf(0.15);
    static final BigDecimal APPROVAL_LIMIT_THIRTY_PERCENT_OF_INCOME = BigDecimal.valueOf(0.30);
    static final BigDecimal CALCULATE_FIFTY_PERCENT_OF_INCOME = BigDecimal.valueOf(0.50);

    private static String formatCpf(String cpf) {
        return cpf.replaceAll("[-.]", "");
    }

    public CreditAnalysisResponse create(CreditAnalysisRequest creditAnalysisRequest) {
        final ApiClientDto apiClientDto = searchClientByID(creditAnalysisRequest.clientId());

        final CreditAnalysis creditAnalysis = analisar(creditAnalysisRequest);

        final AnalysisEntity analysisEntity = analysisEntityMapper.from(creditAnalysis);

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
            return CreditAnalysis.builder()
                    .clientId(request.clientId())
                    .approved(false)
                    .approvedLimit(BigDecimal.ZERO)
                    .withdraw(BigDecimal.ZERO)
                    .annualInterest(BigDecimal.ZERO)
                    .build();
        }
        BigDecimal monthlyIncomeLimitForCalculate = monthlyIncome;

        final int checkingMonthlyIncomeValue = monthlyIncome.compareTo(AMOUNT_LIMIT);

        if (checkingMonthlyIncomeValue > 0) {
            monthlyIncomeLimitForCalculate = AMOUNT_LIMIT;
        }
        final BigDecimal fiftyPercentOfIncome = monthlyIncomeLimitForCalculate.multiply(CALCULATE_FIFTY_PERCENT_OF_INCOME);

        final BigDecimal approvalLimitPercentage;

        if (requestedAmount.compareTo(fiftyPercentOfIncome) > 0) {
            approvalLimitPercentage = APPROVAL_LIMIT_FIFTEEN_PERCENT_OF_INCOME;
        } else {
            approvalLimitPercentage = APPROVAL_LIMIT_THIRTY_PERCENT_OF_INCOME;
        }

        final BigDecimal approvedLimit = monthlyIncomeLimitForCalculate.multiply(approvalLimitPercentage).setScale(2, RoundingMode.HALF_EVEN);

        final BigDecimal withdraw = approvedLimit.multiply(WITHDRAWAL_LIMIT_PERCENTAGE).setScale(2, RoundingMode.HALF_EVEN);
        LOGGER.info("Análise aprovada");
        return CreditAnalysis.builder()
                .clientId(request.clientId())
                .approved(true)
                .approvedLimit(approvedLimit)
                .withdraw(withdraw)
                .annualInterest(ANNUAL_INTEREST)
                .build();
    }

    public ApiClientDto searchClientByCpf(String cpf) {
        final String formattedCpf = formatCpf(cpf);
        final List<ApiClientDto> apiClientDtoList = apiClient.getClientByCpf(formattedCpf);
        if (apiClientDtoList.isEmpty()) {
            throw new ClientNotFoundException("Client not found by cpf %s".formatted(formattedCpf));
        }
        return apiClientDtoList.get(0);
    }

    public ApiClientDto searchClientByID(UUID idClient) {
        final ApiClientDto apiClientDto = apiClient.getClientById(idClient);
        System.out.println(apiClientDto);
        if (apiClientDto.id() == null) {
            throw new ClientNotFoundException("Client not found by id %s".formatted(idClient));
        }
        return apiClientDto;
    }

    public CreditAnalysisResponse getAnalysisById(UUID id) {
        final AnalysisEntity analysis =
                creditAnalysisRepository.findById(id).orElseThrow(() -> new AnalysisNotFoundException("Analysis not found by id %s".formatted(id)));

        return creditAnalysisReponseMapper.from(analysis);
    }

    public List<CreditAnalysisResponse> getAnalysisByClientId(UUID idClient) {
        final List<AnalysisEntity> analysis;
        if (idClient != null) {
            final ApiClientDto apiClientDto = searchClientByID(idClient);

            analysis = creditAnalysisRepository.findByClientId(apiClientDto.id());
        } else {
            analysis = creditAnalysisRepository.findAll();
        }

        return analysis
                .stream()
                .map(creditAnalysisReponseMapper::from)
                .collect(Collectors.toList());
    }

    public List<CreditAnalysisResponse> getAnalysisByClientCpf(String cpf) {
        final List<AnalysisEntity> analysis;

        final ApiClientDto apiClientDto = searchClientByCpf(cpf);

        analysis = creditAnalysisRepository.findByClientId(apiClientDto.id());

        return analysis
                .stream()
                .map(creditAnalysisReponseMapper::from)
                .collect(Collectors.toList());
    }
}
