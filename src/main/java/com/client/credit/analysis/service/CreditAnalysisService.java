package com.client.credit.analysis.service;

import com.client.credit.analysis.apiclient.ApiClient;
import com.client.credit.analysis.apiclient.dto.ApiClientDto;
import com.client.credit.analysis.controller.request.CreditAnalysisRequest;
import com.client.credit.analysis.controller.response.CreditAnalysisResponse;
import com.client.credit.analysis.exception.ClientNotFoundException;
import com.client.credit.analysis.exception.NumberNotNegative;
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

    public CreditAnalysisResponse create(CreditAnalysisRequest creditAnalysisRequest) {
        // Entity salva cpf e id, salvo tudo no model, model salvo pra entity, entiy pra response tiro oq n me interessa

        //        // primeiro crio
        //        final CreditAnalysis creditAnalysis = creditAnalysisMapper.from(creditAnalysisRequest);

        // segundo vejo se tem o client ou n
        final ApiClientDto apiClientDto = searchClient(creditAnalysisRequest.clientId());

        // Terceiro faço analise e já buildo
        final CreditAnalysis creditAnalysis = analisar(creditAnalysisRequest);

        System.out.println("Depois da analise foi isso que ocorreu: " + creditAnalysis);

        // por ultimo atualizo meu client com o cpf se tudo tiver ok
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
        final int verificandoValorAmount = requestedAmount.compareTo(BigDecimal.ZERO);
        final int verificandoValorMonthly = monthlyIncome.compareTo(BigDecimal.ZERO);

        //Verifico se os números são negativos
        if (verificandoValorAmount <= 0) {
            throw new NumberNotNegative("AmountRequest não pode ser negativo ou zero");
        } else if (verificandoValorMonthly <= 0) {
            throw new NumberNotNegative("MonthlyIncome não pode ser negativo ou zero");
        }

        //Talvez esse daq seja da linha 27, ou colocar isso para validar dps do MaiorQue
        //Verifico se o limite pasosu de 50, entao vira 50
        BigDecimal calAmountRequest = request.monthlyIncome();
        final BigDecimal AMOUNT_LIMIT = BigDecimal.valueOf(50000);
        final int verificandoValorMaximoRenda = monthlyIncome.compareTo(AMOUNT_LIMIT);
        if (verificandoValorMaximoRenda > 0) {
            calAmountRequest = AMOUNT_LIMIT;
            System.out.println("Reatribuindo valor do amountRequest");
        }

        final int verificandorequestAmountMaiorQueMonthlyIncome = requestedAmount.compareTo(monthlyIncome);

        // verifico se pedido é maior que renda
        if (verificandorequestAmountMaiorQueMonthlyIncome > 0) {
            System.out.println("Request foi maior que o salario");

            final CreditAnalysis creditAnalysis = CreditAnalysis.builder()
                    .approved(false)
                    .build();
            return creditAnalysis;
        }

        // Verificando se o pedido é maior que 50 porcento da renda ou n
        final BigDecimal cinquentaPorcentoRenda = calAmountRequest.multiply(BigDecimal.valueOf(0.50));
        final BigDecimal porcentagemAprovacaoLimite;

        if (requestedAmount.compareTo(cinquentaPorcentoRenda) > 0) {
            final BigDecimal porcentagemRenda50 = BigDecimal.valueOf(0.15);
            porcentagemAprovacaoLimite = porcentagemRenda50;
            System.out.println("Caiu no maior que 50, retorna 15");
        } else {
            final BigDecimal porcentagemRenda50 = BigDecimal.valueOf(0.30);
            porcentagemAprovacaoLimite = porcentagemRenda50;
            System.out.println("Caiu no maior que 30, retorna 30");
        }

        final BigDecimal approvedLimit = calAmountRequest.multiply(porcentagemAprovacaoLimite).setScale(2, RoundingMode.HALF_EVEN);

        final BigDecimal porcentagemLimitSaque = BigDecimal.valueOf(0.10);

        final BigDecimal annualInterest = BigDecimal.valueOf(0.15);

        final BigDecimal withdraw = approvedLimit.multiply(porcentagemLimitSaque).setScale(2, RoundingMode.HALF_EVEN);
        System.out.printf("Limite aprovado do quantia mensal %.2f do pedido %.2f foi de %.2f limite do saque 10%% %.2f %n", monthlyIncome,
                requestedAmount, approvedLimit, withdraw);

        final CreditAnalysis creditAnalysis = CreditAnalysis.builder()
                .approved(true)
                .approvedLimit(approvedLimit)
                .withdraw(withdraw)
                .annualInterest(annualInterest)
                .build();
        return creditAnalysis;
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

    public List<AnalysisEntity> findAllClients() {
        return creditAnalysisRepository.findAll();
    }
}
