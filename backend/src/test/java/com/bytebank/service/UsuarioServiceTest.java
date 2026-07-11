package com.bytebank.service;

import com.bytebank.dto.request.UsuarioRequest;
import com.bytebank.dto.response.UsuarioResponse;
import com.bytebank.entity.Perfil;
import com.bytebank.entity.Usuario;
import com.bytebank.exception.ConflictException;
import com.bytebank.exception.ResourceNotFoundException;
import com.bytebank.mapper.UsuarioMapper;
import com.bytebank.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioMapper usuarioMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, usuarioMapper, passwordEncoder);
    }

    @Test
    @DisplayName("criar deve criptografar a senha e salvar o usuário com o perfil informado")
    void deveCriarUsuarioComSucesso() {
        UsuarioRequest request = new UsuarioRequest("João", "joao@bytebank.com", "senha1234", Perfil.ADMIN);
        Usuario salvo = Usuario.builder().id(UUID.randomUUID()).nome("João").email("joao@bytebank.com")
                .senha("hash").perfil(Perfil.ADMIN).dataCadastro(LocalDateTime.now()).build();

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.senha())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(salvo);
        when(usuarioMapper.toResponse(salvo)).thenReturn(
                new UsuarioResponse(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getPerfil(), salvo.getDataCadastro())
        );

        UsuarioResponse response = usuarioService.criar(request);

        assertThat(response.email()).isEqualTo("joao@bytebank.com");
        assertThat(response.perfil()).isEqualTo(Perfil.ADMIN);
    }

    @Test
    @DisplayName("criar deve lançar ConflictException quando o e-mail já estiver em uso")
    void deveLancarConflictAoCriarComEmailDuplicado() {
        UsuarioRequest request = new UsuarioRequest("João", "joao@bytebank.com", "senha1234", Perfil.CLIENTE);
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(request))
                .isInstanceOf(ConflictException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar deve lançar IllegalArgumentException quando a senha não for informada")
    void deveLancarExcecaoQuandoSenhaAusente() {
        UsuarioRequest request = new UsuarioRequest("João", "joao@bytebank.com", "  ", Perfil.CLIENTE);
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.criar(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("buscarEntidadePorId deve lançar ResourceNotFoundException quando o usuário não existir")
    void deveLancarNotFoundQuandoUsuarioNaoExiste() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarEntidadePorId(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("atualizar deve manter a senha atual quando a nova senha não for informada")
    void deveManterSenhaQuandoNaoInformada() {
        UUID id = UUID.randomUUID();
        Usuario existente = Usuario.builder().id(id).nome("João").email("joao@bytebank.com")
                .senha("hash-antigo").perfil(Perfil.CLIENTE).build();

        UsuarioRequest request = new UsuarioRequest("João Atualizado", "joao@bytebank.com", null, Perfil.CLIENTE);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioMapper.toResponse(any())).thenReturn(
                new UsuarioResponse(id, "João Atualizado", "joao@bytebank.com", Perfil.CLIENTE, null)
        );

        usuarioService.atualizar(id, request);

        assertThat(existente.getSenha()).isEqualTo("hash-antigo");
        assertThat(existente.getNome()).isEqualTo("João Atualizado");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("remover deve excluir o usuário existente")
    void deveRemoverUsuario() {
        UUID id = UUID.randomUUID();
        Usuario existente = Usuario.builder().id(id).email("joao@bytebank.com").senha("hash").perfil(Perfil.CLIENTE).build();
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existente));

        usuarioService.remover(id);

        verify(usuarioRepository).delete(existente);
    }
}
