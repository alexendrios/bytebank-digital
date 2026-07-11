package com.bytebank.dto.response;

import com.bytebank.entity.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MovimentacaoResponse(
        UUID id,
        TipoMovimentacao tipo,
        BigDecimal valor,
        BigDecimal saldoAnterior,
        BigDecimal saldoAtual,
        String descricao,
        LocalDateTime data
) {
}
