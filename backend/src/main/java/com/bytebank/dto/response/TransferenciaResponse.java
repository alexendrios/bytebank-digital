package com.bytebank.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferenciaResponse(
        UUID id,
        UUID contaOrigemId,
        String contaOrigemNumero,
        UUID contaDestinoId,
        String contaDestinoNumero,
        BigDecimal valor,
        LocalDateTime data
) {
}
