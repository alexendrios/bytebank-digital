package com.bytebank.service;

import com.bytebank.dto.response.ContaResponse;
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
import com.bytebank.strategy.DepositoStrategy;
import com.bytebank.strategy.OperacaoStrategy;
import com.bytebank.strategy.TransferenciaStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bytebank.exception.BusinessException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BancoFacadeTest {

    @Mock
    private ContaService contaService;
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private MovimentacaoRepository movimentacaoRepository;
    @Mock
    private TransferenciaRepository transferenciaRepository;
    @Mock
    private OperacaoFactory operacaoFactory;
    @Mock
    private TransferenciaStrategy transferenciaStrategy;
    @Mock
    private ContaMapper contaMapper;
    @Mock
    private MovimentacaoMapper movimentacaoMapper;
    @Mock
    private TransferenciaMapper transferenciaMapper;

    private BancoFacade bancoFacade;

    private Conta conta;

    @BeforeEach
    void setUp() {
        bancoFacade = new BancoFacade(
                contaService, contaRepository, movimentacaoRepository, transferenciaRepository,
                operacaoFactory, transferenciaStrategy, contaMapper, movimentacaoMapper, transferenciaMapper
        );

        conta = Conta.builder().id(UUID.randomUUID()).numero("100000-1").agencia("0001")
                .saldo(new BigDecimal("100.00")).build();
    }

    @Test
    @DisplayName("depositar deve resolver a DepositoStrategy via factory, salvar conta e movimentação")
    void deveDepositarComSucesso() {
        OperacaoStrategy strategy = new DepositoStrategy();
        Movimentacao movimentacao = Movimentacao.builder().conta(conta).tipo(TipoMovimentacao.DEPOSITO)
                .valor(new BigDecimal("50.00")).saldoAnterior(new BigDecimal("100.00"))
                .saldoAtual(new BigDecimal("150.00")).build();

        when(contaService.buscarEntidadePorId(conta.getId())).thenReturn(conta);
        when(contaService.buscarEntidadeParaAtualizacao(conta.getId())).thenReturn(conta);
        when(operacaoFactory.resolver(TipoMovimentacao.DEPOSITO)).thenReturn(strategy);
        when(contaMapper.toResponse(conta)).thenReturn(
                new ContaResponse(conta.getId(), conta.getNumero(), conta.getAgencia(), new BigDecimal("150.00"), null, null, null)
        );

        ContaResponse response = bancoFacade.depositar(conta.getId(), new BigDecimal("50.00"));

        assertThat(response.saldo()).isEqualByComparingTo("150.00");
        verify(contaService).validarPropriedade(conta);
        verify(contaRepository).save(conta);
        verify(movimentacaoRepository).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("transferir deve debitar origem, creditar destino e persistir a Transferencia")
    void deveTransferirComSucesso() {
        Conta destino = Conta.builder().id(UUID.randomUUID()).numero("200000-2").agencia("0001")
                .saldo(new BigDecimal("10.00")).build();

        Movimentacao movOrigem = Movimentacao.builder().conta(conta).tipo(TipoMovimentacao.TRANSFERENCIA_ENVIADA).build();
        Movimentacao movDestino = Movimentacao.builder().conta(destino).tipo(TipoMovimentacao.TRANSFERENCIA_RECEBIDA).build();

        when(contaService.buscarEntidadePorId(conta.getId())).thenReturn(conta);
        when(contaService.buscarEntidadeParaAtualizacao(conta.getId())).thenReturn(conta);
        when(contaService.buscarEntidadeParaAtualizacao(destino.getId())).thenReturn(destino);
        when(transferenciaStrategy.executar(conta, destino, new BigDecimal("30.00")))
                .thenReturn(List.of(movOrigem, movDestino));
        when(transferenciaRepository.save(any(Transferencia.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transferenciaMapper.toResponse(any())).thenReturn(
                new TransferenciaResponse(UUID.randomUUID(), conta.getId(), conta.getNumero(),
                        destino.getId(), destino.getNumero(), new BigDecimal("30.00"), null)
        );

        TransferenciaResponse response = bancoFacade.transferir(conta.getId(), destino.getId(), new BigDecimal("30.00"));

        assertThat(response.valor()).isEqualByComparingTo("30.00");
        verify(contaService).validarPropriedade(conta);
        verify(contaRepository).save(conta);
        verify(contaRepository).save(destino);
        verify(movimentacaoRepository).saveAll(List.of(movOrigem, movDestino));
    }

    @Test
    @DisplayName("transferir deve rejeitar imediatamente quando origem e destino forem a mesma conta, sem tentar travar")
    void deveRejeitarTransferenciaParaMesmaConta() {
        assertThatThrownBy(() -> bancoFacade.transferir(conta.getId(), conta.getId(), new BigDecimal("10.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não podem ser a mesma");

        verifyNoInteractions(contaRepository, transferenciaStrategy, movimentacaoRepository);
    }

    @Test
    @DisplayName("transferir deve funcionar corretamente mesmo quando o UUID de origem é maior que o de destino " +
            "(a ordem de aquisição de lock é invertida internamente, mas o resultado de negócio não muda)")
    void deveTransferirComSucessoIndependenteDaOrdemDosUuids() {
        // Força contaOrigemId > contaDestinoId, cenário em que a ordenação
        // canônica de lock inverte a ordem de busca interna.
        Conta origem = Conta.builder().id(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"))
                .numero("999999-9").agencia("0001").saldo(new BigDecimal("100.00")).build();
        Conta destino = Conta.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .numero("111111-1").agencia("0001").saldo(new BigDecimal("10.00")).build();

        Movimentacao movOrigem = Movimentacao.builder().conta(origem).tipo(TipoMovimentacao.TRANSFERENCIA_ENVIADA).build();
        Movimentacao movDestino = Movimentacao.builder().conta(destino).tipo(TipoMovimentacao.TRANSFERENCIA_RECEBIDA).build();

        when(contaService.buscarEntidadePorId(origem.getId())).thenReturn(origem);
        when(contaService.buscarEntidadeParaAtualizacao(origem.getId())).thenReturn(origem);
        when(contaService.buscarEntidadeParaAtualizacao(destino.getId())).thenReturn(destino);
        when(transferenciaStrategy.executar(origem, destino, new BigDecimal("30.00")))
                .thenReturn(List.of(movOrigem, movDestino));
        when(transferenciaRepository.save(any(Transferencia.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transferenciaMapper.toResponse(any())).thenReturn(
                new TransferenciaResponse(UUID.randomUUID(), origem.getId(), origem.getNumero(),
                        destino.getId(), destino.getNumero(), new BigDecimal("30.00"), null)
        );

        TransferenciaResponse response = bancoFacade.transferir(origem.getId(), destino.getId(), new BigDecimal("30.00"));

        assertThat(response.valor()).isEqualByComparingTo("30.00");
        verify(transferenciaStrategy).executar(origem, destino, new BigDecimal("30.00"));
    }
}
