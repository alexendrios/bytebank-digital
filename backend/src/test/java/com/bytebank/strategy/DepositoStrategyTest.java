package com.bytebank.strategy;

import com.bytebank.entity.Conta;
import com.bytebank.entity.Movimentacao;
import com.bytebank.entity.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DepositoStrategyTest {

    private final DepositoStrategy strategy = new DepositoStrategy();
    private Conta conta;

    @BeforeEach
    void setUp() {
        conta = Conta.builder()
                .numero("100000-1")
                .agencia("0001")
                .saldo(new BigDecimal("100.00"))
                .build();
    }

    @Test
    @DisplayName("Deve somar o valor ao saldo da conta")
    void deveIncrementarSaldo() {
        Movimentacao movimentacao = strategy.executar(conta, new BigDecimal("50.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("150.00");
        assertThat(movimentacao.getSaldoAnterior()).isEqualByComparingTo("100.00");
        assertThat(movimentacao.getSaldoAtual()).isEqualByComparingTo("150.00");
        assertThat(movimentacao.getTipo()).isEqualTo(TipoMovimentacao.DEPOSITO);
        assertThat(movimentacao.getConta()).isEqualTo(conta);
    }

    @Test
    @DisplayName("O tipo da estratégia deve ser DEPOSITO")
    void deveRetornarTipoCorreto() {
        assertThat(strategy.getTipo()).isEqualTo(TipoMovimentacao.DEPOSITO);
    }
}
