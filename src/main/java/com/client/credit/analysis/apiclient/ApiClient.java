package com.client.credit.analysis.apiclient;

import com.client.credit.analysis.apiclient.dto.ApiClientDto;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "apiClient", url = "localhost:8080/v1.0/clients")
public interface ApiClient {

    @GetMapping(path = "/{id}")
    ApiClientDto getClient(@PathVariable(value = "id") UUID id);
}
