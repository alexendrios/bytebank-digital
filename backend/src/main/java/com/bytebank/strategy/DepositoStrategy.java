package com.bytebank.strategy;

import com.bytebank.entity.Conta;
import com.bytebank.entity.Movimentacao;
import com.bytebank.entity.TipoMovimentacao;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DepositoStrategy implements OperacaoStrategy {

    @Override
    public Movimentacao executar(Conta conta, BigDecimal valor) {
        BigDecimal saldoAnterior = conta.getSaldo();
        BigDecimal saldoAtual = saldoAnterior.add(valor);
        conta.setSaldo(saldoAtual);

        return Movimentacao.builder()
                .conta(conta)
                .tipo(getTipo())
                .valor(valor)
                .saldoAnterior(saldoAnterior)
                .saldoAtual(saldoAtual)
                .descricao("Depósito em conta")
                .build();
    }

    @Override
    public TipoMovimentacao getTipo() {
        return TipoMovimentacao.DEPOSITO;
    }
}
