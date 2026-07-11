package com.bytebank.strategy;

import com.bytebank.entity.Conta;
import com.bytebank.entity.Movimentacao;
import com.bytebank.entity.TipoMovimentacao;

import java.math.BigDecimal;

/**
 * Estratégia (GoF Strategy) para uma operação bancária que afeta uma única
 * conta. Cada implementação sabe validar e aplicar sua própria regra de
 * negócio (ex.: saque valida saldo suficiente, depósito não valida).
 * A {@link com.bytebank.factory.OperacaoFactory} resolve a implementação
 * correta em tempo de execução a partir do {@link TipoMovimentacao}.
 */
public interface OperacaoStrategy {

    /**
     * Aplica a operação sobre o saldo da conta (em memória, sem persistir)
     * e retorna o registro de {@link Movimentacao} correspondente, também
     * ainda não persistido.
     */
    Movimentacao executar(Conta conta, BigDecimal valor);

    TipoMovimentacao getTipo();
}
