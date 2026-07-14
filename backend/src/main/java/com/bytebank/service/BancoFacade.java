package com.bytebank.service;

import com.bytebank.dto.response.ContaResponse;
import com.bytebank.dto.response.MovimentacaoResponse;
import com.bytebank.dto.response.TransferenciaResponse;
import com.bytebank.entity.Conta;
import com.bytebank.entity.Movimentacao;
import com.bytebank.entity.TipoMovimentacao;
import com.bytebank.entity.Transferencia;
import com.bytebank.exception.BusinessException;
import com.bytebank.factory.OperacaoFactory;
import com.bytebank.mapper.ContaMapper;
import com.bytebank.mapper.MovimentacaoMapper;
import com.bytebank.mapper.TransferenciaMapper;
import com.bytebank.repository.ContaRepository;
import com.bytebank.repository.MovimentacaoRepository;
import com.bytebank.repository.TransferenciaRepository;
import com.bytebank.strategy.OperacaoStrategy;
import com.bytebank.strategy.TransferenciaStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Fachada (GoF Facade) que centraliza as interações entre {@link ContaService},
 * {@link OperacaoFactory}/{@link OperacaoStrategy} e os repositórios de
 * movimentação/transferência, expondo uma API simples e transacional para
 * as operações bancárias do dia a dia.
 */
@Service
@RequiredArgsConstructor
public class BancoFacade {

    private final ContaService contaService;
    private final ContaRepository contaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final OperacaoFactory operacaoFactory;
    private final TransferenciaStrategy transferenciaStrategy;
    private final ContaMapper contaMapper;
    private final MovimentacaoMapper movimentacaoMapper;
    private final TransferenciaMapper transferenciaMapper;

    @Transactional
    public ContaResponse depositar(UUID contaId, BigDecimal valor) {
        return aplicarOperacaoSimples(contaId, valor, TipoMovimentacao.DEPOSITO);
    }

    @Transactional
    public ContaResponse sacar(UUID contaId, BigDecimal valor) {
        return aplicarOperacaoSimples(contaId, valor, TipoMovimentacao.SAQUE);
    }

    private ContaResponse aplicarOperacaoSimples(UUID contaId, BigDecimal valor, TipoMovimentacao tipo) {
        // Valida propriedade primeiro com uma leitura simples (sem lock);
        // a leitura que efetivamente sustenta a alteração de saldo é
        // sempre a com lock pessimista, feita a seguir.
        contaService.validarPropriedade(contaService.buscarEntidadePorId(contaId));
        Conta conta = contaService.buscarEntidadeParaAtualizacao(contaId);

        OperacaoStrategy strategy = operacaoFactory.resolver(tipo);
        Movimentacao movimentacao = strategy.executar(conta, valor);

        contaRepository.save(conta);
        movimentacaoRepository.save(movimentacao);

        return contaMapper.toResponse(conta);
    }

    @Transactional
    public TransferenciaResponse transferir(UUID contaOrigemId, UUID contaDestinoId, BigDecimal valor) {
        if (contaOrigemId.equals(contaDestinoId)) {
            throw new BusinessException("Conta de origem e destino não podem ser a mesma");
        }

        // Valida propriedade da conta de origem com uma leitura simples,
        // sem lock (a conta de destino não precisa pertencer ao usuário
        // logado — faz parte do propósito de uma transferência enviar
        // saldo a terceiros).
        contaService.validarPropriedade(contaService.buscarEntidadePorId(contaOrigemId));

        // Trava as duas contas SEMPRE na mesma ordem (menor UUID primeiro),
        // independentemente de quem é origem/destino. Se duas transferências
        // concorrentes ocorrerem em direções opostas (A→B e B→A ao mesmo
        // tempo) e cada uma travasse "origem antes de destino", teríamos
        // deadlock (uma trava A e espera B, a outra trava B e espera A).
        // Com ordem canônica, ambas disputam a mesma primeira trava e uma
        // delas simplesmente espera a outra terminar.
        boolean origemPrimeiro = contaOrigemId.compareTo(contaDestinoId) < 0;
        UUID primeiroId = origemPrimeiro ? contaOrigemId : contaDestinoId;
        UUID segundoId = origemPrimeiro ? contaDestinoId : contaOrigemId;

        Conta primeira = contaService.buscarEntidadeParaAtualizacao(primeiroId);
        Conta segunda = contaService.buscarEntidadeParaAtualizacao(segundoId);

        Conta origem = origemPrimeiro ? primeira : segunda;
        Conta destino = origemPrimeiro ? segunda : primeira;

        List<Movimentacao> movimentacoes = transferenciaStrategy.executar(origem, destino, valor);

        contaRepository.save(origem);
        contaRepository.save(destino);
        movimentacaoRepository.saveAll(movimentacoes);

        Transferencia transferencia = Transferencia.builder()
                .contaOrigem(origem)
                .contaDestino(destino)
                .valor(valor)
                .build();

        return transferenciaMapper.toResponse(transferenciaRepository.save(transferencia));
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponse> extrato(UUID contaId, Pageable pageable) {
        Conta conta = contaService.buscarEntidadePorId(contaId);
        contaService.validarPropriedade(conta);

        return movimentacaoRepository.findByContaIdOrderByDataDesc(contaId, pageable)
                .map(movimentacaoMapper::toResponse);
    }
}
