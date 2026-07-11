package com.bytebank.strategy;

import com.bytebank.entity.Conta;
import com.bytebank.entity.Movimentacao;
import com.bytebank.entity.TipoMovimentacao;
import com.bytebank.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransferenciaStrategyTest {

    private final TransferenciaStrategy strategy = new TransferenciaStrategy();
    private Conta origem;
    private Conta destino;

    @BeforeEach
    void setUp() {
        origem = Conta.builder()
                .id(UUID.randomUUID())
                .numero("100000-1")
                .agencia("0001")
                .saldo(new BigDecimal("200.00"))
                .build();

        destino = Conta.builder()
                .id(UUID.randomUUID())
                .numero("200000-2")
                .agencia("0001")
                .saldo(new BigDecimal("50.00"))
                .build();
    }

    @Test
    @DisplayName("Deve debitar da origem e creditar no destino, gerando duas movimentações")
    void deveTransferirCorretamente() {
        List<Movimentacao> movimentacoes = strategy.executar(origem, destino, new BigDecimal("80.00"));

        assertThat(origem.getSaldo()).isEqualByComparingTo("120.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("130.00");

        assertThat(movimentacoes).hasSize(2);
        assertThat(movimentacoes.get(0).getTipo()).isEqualTo(TipoMovimentacao.TRANSFERENCIA_ENVIADA);
        assertThat(movimentacoes.get(0).getConta()).isEqualTo(origem);
        assertThat(movimentacoes.get(1).getTipo()).isEqualTo(TipoMovimentacao.TRANSFERENCIA_RECEBIDA);
        assertThat(movimentacoes.get(1).getConta()).isEqualTo(destino);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o saldo de origem for insuficiente")
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        assertThatThrownBy(() -> strategy.executar(origem, destino, new BigDecimal("500.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Saldo insuficiente");

        assertThat(origem.getSaldo()).isEqualByComparingTo("200.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando origem e destino forem a mesma conta")
    void deveLancarExcecaoQuandoContasIguais() {
        assertThatThrownBy(() -> strategy.executar(origem, origem, new BigDecimal("10.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não podem ser a mesma");
    }
}
