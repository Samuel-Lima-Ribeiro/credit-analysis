package com.client.credit.analysis.apiclient.dto;

import java.util.UUID;

public record ApiClientDto(
        UUID id,
        String cpf,
        Integer status
) {
}
