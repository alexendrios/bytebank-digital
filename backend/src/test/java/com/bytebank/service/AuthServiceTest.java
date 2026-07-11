package com.bytebank.service;

import com.bytebank.dto.request.LoginRequest;
import com.bytebank.dto.request.RefreshTokenRequest;
import com.bytebank.dto.request.RegisterRequest;
import com.bytebank.dto.response.AuthResponse;
import com.bytebank.entity.Perfil;
import com.bytebank.entity.Usuario;
import com.bytebank.exception.ConflictException;
import com.bytebank.repository.UsuarioRepository;
import com.bytebank.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    @DisplayName("register deve criar usuário CLIENTE com senha criptografada e retornar tokens")
    void deveRegistrarNovoUsuario() {
        RegisterRequest request = new RegisterRequest("Maria Silva", "maria@bytebank.com", "senha1234");

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.senha())).thenReturn("senha-encriptada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario salvo = captor.getValue();

        assertThat(salvo.getEmail()).isEqualTo("maria@bytebank.com");
        assertThat(salvo.getSenha()).isEqualTo("senha-encriptada");
        assertThat(salvo.getPerfil()).isEqualTo(Perfil.CLIENTE);
    }

    @Test
    @DisplayName("register deve lançar ConflictException quando o e-mail já existir")
    void deveLancarConflictQuandoEmailJaExiste() {
        RegisterRequest request = new RegisterRequest("Maria Silva", "maria@bytebank.com", "senha1234");
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("login deve autenticar via AuthenticationManager e retornar tokens")
    void deveAutenticarUsuarioExistente() {
        LoginRequest request = new LoginRequest("maria@bytebank.com", "senha1234");
        Usuario usuario = Usuario.builder().email("maria@bytebank.com").senha("hash").perfil(Perfil.CLIENTE).build();

        when(usuarioRepository.findByEmail("maria@bytebank.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(any());
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("refresh deve emitir novos tokens quando o refresh token for válido")
    void deveRenovarTokensComRefreshTokenValido() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-valido");
        Usuario usuario = Usuario.builder().email("maria@bytebank.com").senha("hash").perfil(Perfil.CLIENTE).build();

        when(jwtService.isRefreshToken("refresh-valido")).thenReturn(true);
        when(jwtService.extractEmail("refresh-valido")).thenReturn("maria@bytebank.com");
        when(usuarioRepository.findByEmail("maria@bytebank.com")).thenReturn(Optional.of(usuario));
        when(jwtService.isValid("refresh-valido", usuario)).thenReturn(true);
        when(jwtService.generateAccessToken(usuario)).thenReturn("novo-access-token");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("novo-refresh-token");

        AuthResponse response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("novo-access-token");
        assertThat(response.refreshToken()).isEqualTo("novo-refresh-token");
    }

    @Test
    @DisplayName("refresh deve lançar JwtException quando receber um access token no lugar de refresh")
    void deveLancarExcecaoQuandoTokenNaoForDeRefresh() {
        RefreshTokenRequest request = new RefreshTokenRequest("access-token-invalido");
        when(jwtService.isRefreshToken("access-token-invalido")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(JwtException.class);

        verify(usuarioRepository, never()).findByEmail(anyString());
    }
}
