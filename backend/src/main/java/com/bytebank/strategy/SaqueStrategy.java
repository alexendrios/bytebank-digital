package com.bytebank.strategy;

import com.bytebank.entity.Conta;
import com.bytebank.entity.Movimentacao;
import com.bytebank.entity.TipoMovimentacao;
import com.bytebank.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SaqueStrategy implements OperacaoStrategy {

    @Override
    public Movimentacao executar(Conta conta, BigDecimal valor) {
        BigDecimal saldoAnterior = conta.getSaldo();

        if (saldoAnterior.compareTo(valor) < 0) {
            throw new BusinessException("Saldo insuficiente para realizar o saque");
        }

        BigDecimal saldoAtual = saldoAnterior.subtract(valor);
        conta.setSaldo(saldoAtual);

        return Movimentacao.builder()
                .conta(conta)
                .tipo(getTipo())
                .valor(valor)
                .saldoAnterior(saldoAnterior)
                .saldoAtual(saldoAtual)
                .descricao("Saque em conta")
                .build();
    }

    @Override
    public TipoMovimentacao getTipo() {
        return TipoMovimentacao.SAQUE;
    }
}
