package com.bytebank.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ContaResponse(
        UUID id,
        String numero,
        String agencia,
        BigDecimal saldo,
        UUID usuarioId,
        String usuarioNome,
        LocalDateTime dataCriacao
) {
}
