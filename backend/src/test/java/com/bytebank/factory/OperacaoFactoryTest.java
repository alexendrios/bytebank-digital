package com.bytebank.factory;

import com.bytebank.entity.TipoMovimentacao;
import com.bytebank.exception.BusinessException;
import com.bytebank.strategy.DepositoStrategy;
import com.bytebank.strategy.OperacaoStrategy;
import com.bytebank.strategy.SaqueStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperacaoFactoryTest {

    private final OperacaoFactory factory = new OperacaoFactory(
            List.of(new DepositoStrategy(), new SaqueStrategy())
    );

    @Test
    @DisplayName("Deve resolver a DepositoStrategy para o tipo DEPOSITO")
    void deveResolverDeposito() {
        OperacaoStrategy strategy = factory.resolver(TipoMovimentacao.DEPOSITO);
        assertThat(strategy).isInstanceOf(DepositoStrategy.class);
    }

    @Test
    @DisplayName("Deve resolver a SaqueStrategy para o tipo SAQUE")
    void deveResolverSaque() {
        OperacaoStrategy strategy = factory.resolver(TipoMovimentacao.SAQUE);
        assertThat(strategy).isInstanceOf(SaqueStrategy.class);
    }

    @Test
    @DisplayName("Deve lançar BusinessException para um tipo sem estratégia registrada")
    void deveLancarExcecaoParaTipoNaoSuportado() {
        assertThatThrownBy(() -> factory.resolver(TipoMovimentacao.PIX))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não suportado");
    }
}
