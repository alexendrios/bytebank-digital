package com.bytebank.integration;

import com.bytebank.dto.request.TransferenciaRequest;
import com.bytebank.dto.request.ValorOperacaoRequest;
import com.bytebank.dto.response.ContaResponse;
import com.bytebank.dto.response.TransferenciaResponse;
import com.bytebank.entity.Conta;
import com.bytebank.entity.Perfil;
import com.bytebank.entity.Usuario;
import com.bytebank.repository.ContaRepository;
import com.bytebank.repository.MovimentacaoRepository;
import com.bytebank.repository.TransferenciaRepository;
import com.bytebank.repository.UsuarioRepository;
import com.bytebank.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração das operações bancárias (depósito, saque, transferência
 * e extrato) e da regra de propriedade de conta (CLIENTE só acessa a própria
 * conta), exercitando a stack completa contra um PostgreSQL real.
 */
class ContaOperacoesIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ContaRepository contaRepository;
    @Autowired
    private MovimentacaoRepository movimentacaoRepository;
    @Autowired
    private TransferenciaRepository transferenciaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    private Usuario clienteA;
    private Usuario clienteB;
    private Conta contaA;
    private Conta contaB;
    private String tokenClienteA;
    private String tokenClienteB;

    @BeforeEach
    void setUp() {
        clienteA = usuarioRepository.save(Usuario.builder()
                .nome("Cliente A").email("clienteA@bytebank.com")
                .senha(passwordEncoder.encode("senha1234")).perfil(Perfil.CLIENTE).build());

        clienteB = usuarioRepository.save(Usuario.builder()
                .nome("Cliente B").email("clienteB@bytebank.com")
                .senha(passwordEncoder.encode("senha1234")).perfil(Perfil.CLIENTE).build());

        contaA = contaRepository.save(Conta.builder()
                .numero("100000-1").agencia("0001").saldo(new BigDecimal("100.00")).usuario(clienteA).build());

        contaB = contaRepository.save(Conta.builder()
                .numero("200000-2").agencia("0001").saldo(new BigDecimal("10.00")).usuario(clienteB).build());

        tokenClienteA = jwtService.generateAccessToken(clienteA);
        tokenClienteB = jwtService.generateAccessToken(clienteB);
    }

    @AfterEach
    void limpar() {

        movimentacaoRepository.deleteAll();
        transferenciaRepository.deleteAll();

        if (contaA != null) {
            contaRepository.deleteById(contaA.getId());
        }

        if (contaB != null) {
            contaRepository.deleteById(contaB.getId());
        }

        if (clienteA != null) {
            usuarioRepository.deleteById(clienteA.getId());
        }

        if (clienteB != null) {
            usuarioRepository.deleteById(clienteB.getId());
        }
    }

    private HttpHeaders headersCom(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("POST /contas/{id}/deposito deve aumentar o saldo da própria conta")
    void deveDepositarNaPropriaConta() {
        ValorOperacaoRequest request = new ValorOperacaoRequest(new BigDecimal("50.00"));
        HttpEntity<ValorOperacaoRequest> entity = new HttpEntity<>(request, headersCom(tokenClienteA));

        ResponseEntity<ContaResponse> response = restTemplate.exchange(
                "/contas/" + contaA.getId() + "/deposito", HttpMethod.POST, entity, ContaResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().saldo()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("POST /contas/{id}/saque com saldo insuficiente deve retornar 422")
    void deveRetornarErroAoSacarSemSaldo() {
        ValorOperacaoRequest request = new ValorOperacaoRequest(new BigDecimal("1000.00"));
        HttpEntity<ValorOperacaoRequest> entity = new HttpEntity<>(request, headersCom(tokenClienteA));

        ResponseEntity<String> response = restTemplate.exchange(
                "/contas/" + contaA.getId() + "/saque", HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("Cliente não pode depositar em conta de outro cliente (403)")
    void naoDevePermitirDepositoEmContaDeOutroCliente() {
        ValorOperacaoRequest request = new ValorOperacaoRequest(new BigDecimal("50.00"));
        HttpEntity<ValorOperacaoRequest> entity = new HttpEntity<>(request, headersCom(tokenClienteB));

        ResponseEntity<String> response = restTemplate.exchange(
                "/contas/" + contaA.getId() + "/deposito", HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("POST /transferencias deve debitar origem e creditar destino")
    void deveTransferirEntreContas() {
        TransferenciaRequest request = new TransferenciaRequest(contaA.getId(), contaB.getId(), new BigDecimal("30.00"));
        HttpEntity<TransferenciaRequest> entity = new HttpEntity<>(request, headersCom(tokenClienteA));

        ResponseEntity<TransferenciaResponse> response = restTemplate.exchange(
                "/transferencias", HttpMethod.POST, entity, TransferenciaResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().valor()).isEqualByComparingTo("30.00");

        Conta origemAtualizada = contaRepository.findById(contaA.getId()).orElseThrow();
        Conta destinoAtualizada = contaRepository.findById(contaB.getId()).orElseThrow();
        assertThat(origemAtualizada.getSaldo()).isEqualByComparingTo("70.00");
        assertThat(destinoAtualizada.getSaldo()).isEqualByComparingTo("40.00");
    }

    @Test
    @DisplayName("Cliente não pode transferir a partir de conta que não é sua (403)")
    void naoDevePermitirTransferenciaDeContaAlheia() {
        TransferenciaRequest request = new TransferenciaRequest(contaA.getId(), contaB.getId(), new BigDecimal("10.00"));
        HttpEntity<TransferenciaRequest> entity = new HttpEntity<>(request, headersCom(tokenClienteB));

        ResponseEntity<String> response = restTemplate.exchange(
                "/transferencias", HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /contas/{id}/extrato deve listar as movimentações após um depósito")
    void deveListarExtratoAposDeposito() {
        ValorOperacaoRequest deposito = new ValorOperacaoRequest(new BigDecimal("25.00"));
        HttpEntity<ValorOperacaoRequest> depositoEntity = new HttpEntity<>(deposito, headersCom(tokenClienteA));
        restTemplate.exchange("/contas/" + contaA.getId() + "/deposito", HttpMethod.POST, depositoEntity, ContaResponse.class);

        HttpEntity<Void> extratoEntity = new HttpEntity<>(headersCom(tokenClienteA));
        ResponseEntity<String> response = restTemplate.exchange(
                "/contas/" + contaA.getId() + "/extrato", HttpMethod.GET, extratoEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("DEPOSITO");
    }
}
