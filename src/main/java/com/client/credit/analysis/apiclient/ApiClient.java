package com.client.credit.analysis.apiclient;

import com.client.credit.analysis.apiclient.dto.ApiClientDto;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "apiClient", url = "${url.apiclient.host}")
public interface ApiClient {

    @GetMapping(path = "/{id}")
    ApiClientDto getClientById(@PathVariable(value = "id") UUID id);

    @GetMapping
    List<ApiClientDto> getClientByCpf(@RequestParam String cpf);
}
