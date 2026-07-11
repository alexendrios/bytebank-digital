package com.bytebank.service;

import com.bytebank.dto.request.ContaRequest;
import com.bytebank.dto.response.ContaResponse;
import com.bytebank.entity.Conta;
import com.bytebank.entity.Perfil;
import com.bytebank.entity.Usuario;
import com.bytebank.exception.ResourceNotFoundException;
import com.bytebank.mapper.ContaMapper;
import com.bytebank.repository.ContaRepository;
import com.bytebank.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private ContaMapper contaMapper;

    private ContaService contaService;

    private Usuario cliente;
    private Usuario admin;
    private Conta contaDoCliente;

    @BeforeEach
    void setUp() {
        contaService = new ContaService(contaRepository, usuarioService, contaMapper);

        cliente = Usuario.builder().id(UUID.randomUUID()).email("cliente@bytebank.com")
                .senha("hash").perfil(Perfil.CLIENTE).build();
        admin = Usuario.builder().id(UUID.randomUUID()).email("admin@bytebank.com")
                .senha("hash").perfil(Perfil.ADMIN).build();

        contaDoCliente = Conta.builder()
                .id(UUID.randomUUID())
                .numero("100000-1")
                .agencia("0001")
                .saldo(BigDecimal.ZERO)
                .usuario(cliente)
                .build();
    }

    @Test
    @DisplayName("listar como ADMIN deve retornar todas as contas")
    void listarComoAdminRetornaTodasAsContas() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsuarioAutenticado).thenReturn(admin);
            when(contaRepository.findAll()).thenReturn(List.of(contaDoCliente));
            when(contaMapper.toResponse(contaDoCliente)).thenReturn(
                    new ContaResponse(contaDoCliente.getId(), "100000-1", "0001", BigDecimal.ZERO, cliente.getId(), null, null)
            );

            List<ContaResponse> resultado = contaService.listar();

            assertThat(resultado).hasSize(1);
            verify(contaRepository).findAll();
            verify(contaRepository, never()).findByUsuarioId(any());
        }
    }

    @Test
    @DisplayName("listar como CLIENTE deve retornar apenas as próprias contas")
    void listarComoClienteRetornaApenasProprias() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsuarioAutenticado).thenReturn(cliente);
            when(contaRepository.findByUsuarioId(cliente.getId())).thenReturn(List.of(contaDoCliente));
            when(contaMapper.toResponse(any())).thenReturn(
                    new ContaResponse(contaDoCliente.getId(), "100000-1", "0001", BigDecimal.ZERO, cliente.getId(), null, null)
            );

            contaService.listar();

            verify(contaRepository).findByUsuarioId(cliente.getId());
            verify(contaRepository, never()).findAll();
        }
    }

    @Test
    @DisplayName("buscarPorId deve lançar AccessDeniedException quando outro cliente tentar acessar")
    void deveNegarAcessoParaContaDeOutroCliente() {
        Usuario outroCliente = Usuario.builder().id(UUID.randomUUID()).email("outro@bytebank.com")
                .senha("hash").perfil(Perfil.CLIENTE).build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsuarioAutenticado).thenReturn(outroCliente);
            when(contaRepository.findById(contaDoCliente.getId())).thenReturn(Optional.of(contaDoCliente));

            assertThatThrownBy(() -> contaService.buscarPorId(contaDoCliente.getId()))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    @DisplayName("buscarPorId deve permitir que o próprio dono acesse a conta")
    void devePermitirAcessoDoProprioDono() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUsuarioAutenticado).thenReturn(cliente);
            when(contaRepository.findById(contaDoCliente.getId())).thenReturn(Optional.of(contaDoCliente));
            when(contaMapper.toResponse(contaDoCliente)).thenReturn(
                    new ContaResponse(contaDoCliente.getId(), "100000-1", "0001", BigDecimal.ZERO, cliente.getId(), null, null)
            );

            ContaResponse response = contaService.buscarPorId(contaDoCliente.getId());

            assertThat(response.id()).isEqualTo(contaDoCliente.getId());
        }
    }

    @Test
    @DisplayName("buscarEntidadePorId deve lançar ResourceNotFoundException quando a conta não existir")
    void deveLancarNotFoundQuandoContaNaoExiste() {
        UUID id = UUID.randomUUID();
        when(contaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.buscarEntidadePorId(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("criar deve gerar um número de conta único e vincular ao usuário informado")
    void deveCriarContaComNumeroUnico() {
        ContaRequest request = new ContaRequest("0001", cliente.getId());

        when(usuarioService.buscarEntidadePorId(cliente.getId())).thenReturn(cliente);
        when(contaRepository.existsByNumero(any())).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(contaMapper.toResponse(any())).thenReturn(
                new ContaResponse(UUID.randomUUID(), "123456-1", "0001", BigDecimal.ZERO, cliente.getId(), null, null)
        );

        ContaResponse response = contaService.criar(request);

        assertThat(response.agencia()).isEqualTo("0001");
        verify(contaRepository).existsByNumero(any());
    }
}
