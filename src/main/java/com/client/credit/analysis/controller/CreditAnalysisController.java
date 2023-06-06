package com.client.credit.analysis.controller;

import com.client.credit.analysis.controller.request.CreditAnalysisRequest;
import com.client.credit.analysis.controller.response.CreditAnalysisResponse;
import com.client.credit.analysis.service.CreditAnalysisService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "v1.0/credits/analysis")
@RequiredArgsConstructor
@Validated
public class CreditAnalysisController {

    private final CreditAnalysisService creditAnalysisService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CreditAnalysisResponse create(@RequestBody CreditAnalysisRequest creditAnalysisRequest) {
        return creditAnalysisService.create(creditAnalysisRequest);
    }

    @GetMapping(path = "/{id}")
    public CreditAnalysisResponse getAnalysis(@PathVariable(value = "id") UUID id) {
        return creditAnalysisService.getAnalysisById(id);
    }

    @GetMapping
    public List<CreditAnalysisResponse> getAnalysisByIdClient(
            @RequestParam(value = "idClient", required = false) UUID idClient,
            @RequestParam(value = "cpfClient", required = false) @Valid @CPF(message = "cpf invalid") String cpfClient) {
        if (cpfClient != null) {
            return creditAnalysisService.getAnalysisByClientCpf(cpfClient);
        }
        return creditAnalysisService.getAnalysisByClientId(idClient);
    }
}
