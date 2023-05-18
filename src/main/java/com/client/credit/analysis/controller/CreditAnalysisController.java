package com.client.credit.analysis.controller;

import com.client.credit.analysis.controller.request.CreditAnalysisRequest;
import com.client.credit.analysis.controller.response.CreditAnalysisResponse;
import com.client.credit.analysis.repository.entity.AnalysisEntity;
import com.client.credit.analysis.service.CreditAnalysisService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "v1.0/credit")
@RequiredArgsConstructor
public class CreditAnalysisController {

    private final CreditAnalysisService creditAnalysisService;

    @PostMapping(path = "/analysis")
    public CreditAnalysisResponse create(@RequestBody CreditAnalysisRequest creditAnalysisRequest) {
        return creditAnalysisService.create(creditAnalysisRequest);
    }

    @GetMapping(path = "/{id}")
    public void buscarClient(@PathVariable(value = "id") UUID id) {
        creditAnalysisService.searchClient(id);
    }

    @GetMapping
    public List<AnalysisEntity> searchAllClients() {
        return creditAnalysisService.findAllClients();
    }
}
