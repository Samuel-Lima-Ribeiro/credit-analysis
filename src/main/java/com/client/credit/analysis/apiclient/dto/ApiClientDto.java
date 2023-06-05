package com.client.credit.analysis.apiclient.dto;

import java.util.UUID;
import lombok.Builder;

public record ApiClientDto(
        UUID id
) {
    @Builder
    public ApiClientDto(UUID id) {
        this.id = id;
    }
}
