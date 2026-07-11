package com.bytebank.service;

import com.bytebank.dto.response.ContaResponse;
import com.bytebank.dto.response.MovimentacaoResponse;
import com.bytebank.dto.response.TransferenciaResponse;
import com.bytebank.entity.Conta;
import com.bytebank.entity.Movimentacao;
import com.bytebank.entity.TipoMovimentacao;
import com.bytebank.entity.Transferencia;
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
        Conta conta = contaService.buscarEntidadePorId(contaId);
        contaService.validarPropriedade(conta);

        OperacaoStrategy strategy = operacaoFactory.resolver(tipo);
        Movimentacao movimentacao = strategy.executar(conta, valor);

        contaRepository.save(conta);
        movimentacaoRepository.save(movimentacao);

        return contaMapper.toResponse(conta);
    }

    @Transactional
    public TransferenciaResponse transferir(UUID contaOrigemId, UUID contaDestinoId, BigDecimal valor) {
        Conta origem = contaService.buscarEntidadePorId(contaOrigemId);
        contaService.validarPropriedade(origem);

        Conta destino = contaService.buscarEntidadePorId(contaDestinoId);
        // Nota: a conta de destino não precisa pertencer ao usuário logado —
        // faz parte do propósito de uma transferência enviar saldo a terceiros.

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
