package com.bytebank.strategy;

import com.bytebank.entity.Conta;
import com.bytebank.entity.Movimentacao;
import com.bytebank.entity.TipoMovimentacao;
import com.bytebank.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Estratégia de transferência entre duas contas distintas. Não implementa
 * {@link OperacaoStrategy} porque a operação afeta duas contas ao mesmo
 * tempo (débito na origem, crédito no destino), enquanto a interface
 * genérica foi desenhada para operações de conta única (depósito/saque).
 */
@Component
public class TransferenciaStrategy {

    /**
     * Aplica o débito na conta de origem e o crédito na conta de destino
     * (em memória, sem persistir) e retorna as duas movimentações geradas,
     * na ordem [movimentaçãoOrigem, movimentaçãoDestino].
     */
    public List<Movimentacao> executar(Conta origem, Conta destino, BigDecimal valor) {
        if (origem.getId().equals(destino.getId())) {
            throw new BusinessException("Conta de origem e destino não podem ser a mesma");
        }

        BigDecimal saldoAnteriorOrigem = origem.getSaldo();
        if (saldoAnteriorOrigem.compareTo(valor) < 0) {
            throw new BusinessException("Saldo insuficiente para realizar a transferência");
        }
        BigDecimal saldoAtualOrigem = saldoAnteriorOrigem.subtract(valor);
        origem.setSaldo(saldoAtualOrigem);

        BigDecimal saldoAnteriorDestino = destino.getSaldo();
        BigDecimal saldoAtualDestino = saldoAnteriorDestino.add(valor);
        destino.setSaldo(saldoAtualDestino);

        Movimentacao movimentacaoOrigem = Movimentacao.builder()
                .conta(origem)
                .tipo(TipoMovimentacao.TRANSFERENCIA_ENVIADA)
                .valor(valor)
                .saldoAnterior(saldoAnteriorOrigem)
                .saldoAtual(saldoAtualOrigem)
                .descricao("Transferência enviada para conta " + destino.getNumero())
                .build();

        Movimentacao movimentacaoDestino = Movimentacao.builder()
                .conta(destino)
                .tipo(TipoMovimentacao.TRANSFERENCIA_RECEBIDA)
                .valor(valor)
                .saldoAnterior(saldoAnteriorDestino)
                .saldoAtual(saldoAtualDestino)
                .descricao("Transferência recebida da conta " + origem.getNumero())
                .build();

        return List.of(movimentacaoOrigem, movimentacaoDestino);
    }
}
