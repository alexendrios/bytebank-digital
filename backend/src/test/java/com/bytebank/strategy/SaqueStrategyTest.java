package com.bytebank.strategy;

import com.bytebank.entity.Conta;
import com.bytebank.entity.Movimentacao;
import com.bytebank.entity.TipoMovimentacao;
import com.bytebank.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaqueStrategyTest {

    private final SaqueStrategy strategy = new SaqueStrategy();
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
    @DisplayName("Deve subtrair o valor do saldo quando houver saldo suficiente")
    void deveDecrementarSaldoQuandoSaldoSuficiente() {
        Movimentacao movimentacao = strategy.executar(conta, new BigDecimal("40.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("60.00");
        assertThat(movimentacao.getTipo()).isEqualTo(TipoMovimentacao.SAQUE);
        assertThat(movimentacao.getSaldoAnterior()).isEqualByComparingTo("100.00");
        assertThat(movimentacao.getSaldoAtual()).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o saldo for insuficiente")
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        assertThatThrownBy(() -> strategy.executar(conta, new BigDecimal("200.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Saldo insuficiente");

        // Garante que o saldo não foi alterado em caso de falha
        assertThat(conta.getSaldo()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Deve permitir saque exato do saldo disponível (saldo final zero)")
    void devePermitirSaqueDoValorExatoDoSaldo() {
        Movimentacao movimentacao = strategy.executar(conta, new BigDecimal("100.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("0.00");
        assertThat(movimentacao.getSaldoAtual()).isEqualByComparingTo("0.00");
    }
}
