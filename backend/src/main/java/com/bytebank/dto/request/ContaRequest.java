package com.bytebank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ContaRequest(

        @NotBlank(message = "Agência é obrigatória")
        String agencia,

        @NotNull(message = "Usuário titular é obrigatório")
        UUID usuarioId
) {
}
