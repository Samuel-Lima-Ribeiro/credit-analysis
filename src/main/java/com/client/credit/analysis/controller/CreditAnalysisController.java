package com.client.credit.analysis.controller;

import com.client.credit.analysis.controller.request.CreditAnalysisRequest;
import com.client.credit.analysis.controller.response.CreditAnalysisResponse;
import com.client.credit.analysis.service.CreditAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping(path = "sla")
    public void sla() {
        System.out.println("aaaaaa");
    }
}
