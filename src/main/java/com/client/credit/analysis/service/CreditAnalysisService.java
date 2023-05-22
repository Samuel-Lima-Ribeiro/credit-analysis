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

        final ApiClientDto apiClientDto = searchClient(creditAnalysis.clientId());

        final CreditAnalysis creditAnalysisUpdateAnalysis = creditAnalysis.updateFromAnalysis(analisar(creditAnalysisRequest));

        final CreditAnalysis creditAnalysisUpdateClient = creditAnalysisUpdateAnalysis.updateFromClient(apiClientDto);

        final AnalysisEntity analysisEntity = analysisEntityMapper.from(creditAnalysisUpdateClient);
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

        //Talvez esse daq seja da linha 27, ou colocar isso para validar dps do MaiorQue
        BigDecimal monthlyIncomeLimitForCalculate = monthlyIncome;
        final BigDecimal amountLimit = BigDecimal.valueOf(50000);
        final int checkingMonthlyIncomeValue = monthlyIncome.compareTo(amountLimit);

        if (checkingMonthlyIncomeValue > 0) {
            monthlyIncomeLimitForCalculate = amountLimit;
        }

        final int checkingRequestAmountGreaterThanMonthlyIncome = requestedAmount.compareTo(monthlyIncome);

        if (checkingRequestAmountGreaterThanMonthlyIncome > 0) {
            return CreditAnalysis.builder()
                    .approved(false)
                    .approvedLimit(BigDecimal.ZERO)
                    .withdraw(BigDecimal.ZERO)
                    .annualInterest(BigDecimal.ZERO)
                    .build();
        }

        final BigDecimal fiftyPercentOfIncome = monthlyIncomeLimitForCalculate.multiply(BigDecimal.valueOf(0.50));
        final BigDecimal approvalLimitPercentage;

        if (requestedAmount.compareTo(fiftyPercentOfIncome) > 0) {
            final BigDecimal percentageToCalculateLimit = BigDecimal.valueOf(0.15);
            approvalLimitPercentage = percentageToCalculateLimit;
        } else {
            final BigDecimal percentageToCalculateLimit = BigDecimal.valueOf(0.30);
            approvalLimitPercentage = percentageToCalculateLimit;
        }

        final BigDecimal approvedLimit = monthlyIncomeLimitForCalculate.multiply(approvalLimitPercentage).setScale(2, RoundingMode.HALF_EVEN);

        final BigDecimal withdrawalLimitPercentage = BigDecimal.valueOf(0.10);

        final BigDecimal annualInterest = BigDecimal.valueOf(15);

        final BigDecimal withdraw = approvedLimit.multiply(withdrawalLimitPercentage).setScale(2, RoundingMode.HALF_EVEN);
        return CreditAnalysis.builder()
                .approved(true)
                .approvedLimit(approvedLimit)
                .withdraw(withdraw)
                .annualInterest(annualInterest)
                .build();
    }

    public ApiClientDto searchClient(UUID id) {
        try {
            return apiClient.getClientById(id);
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
        final int LengthMaxCpf = 15;
        final List<AnalysisEntity> analysis;
        if (id.length() < LengthMaxCpf) {
            id = formatCpf(id);
            final ApiClientDto client = apiClient.getClientByCpf(id);
            System.out.println(client.id() + " ID DO CARA");
            analysis = creditAnalysisRepository.findByClientId(client.id());
        } else {
            analysis = creditAnalysisRepository.findByClientId(UUID.fromString(id));
        }
        return analysis.stream()
                .map(creditAnalysisReponseMapper::from)
                .collect(Collectors.toList());
    }
}
