package com.bytebank.security;

import com.bytebank.entity.Perfil;
import com.bytebank.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-with-at-least-256-bits-for-hmac-sha";

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(SECRET, 60_000L, 3_600_000L);
        jwtService = new JwtService(properties);

        usuario = Usuario.builder()
                .email("cliente@bytebank.com")
                .senha("hash")
                .perfil(Perfil.CLIENTE)
                .build();
    }

    @Test
    @DisplayName("Access token gerado deve conter o e-mail como subject e ser marcado como access")
    void deveGerarAccessTokenValido() {
        String token = jwtService.generateAccessToken(usuario);

        assertThat(jwtService.extractEmail(token)).isEqualTo(usuario.getEmail());
        assertThat(jwtService.isAccessToken(token)).isTrue();
        assertThat(jwtService.isRefreshToken(token)).isFalse();
        assertThat(jwtService.isValid(token, usuario)).isTrue();
    }

    @Test
    @DisplayName("Refresh token gerado deve ser distinguível do access token")
    void deveGerarRefreshTokenValido() {
        String token = jwtService.generateRefreshToken(usuario);

        assertThat(jwtService.isRefreshToken(token)).isTrue();
        assertThat(jwtService.isAccessToken(token)).isFalse();
        assertThat(jwtService.isValid(token, usuario)).isTrue();
    }

    @Test
    @DisplayName("Token não deve ser válido para um usuário diferente do subject")
    void naoDeveSerValidoParaOutroUsuario() {
        String token = jwtService.generateAccessToken(usuario);

        Usuario outroUsuario = Usuario.builder()
                .email("outro@bytebank.com")
                .senha("hash")
                .perfil(Perfil.CLIENTE)
                .build();

        assertThat(jwtService.isValid(token, outroUsuario)).isFalse();
    }

    @Test
    @DisplayName("Token expirado não deve ser válido")
    void tokenExpiradoNaoDeveSerValido() throws InterruptedException {
        JwtProperties propriedadesCurtas = new JwtProperties(SECRET, 1L, 1L);
        JwtService servicoComExpiracaoCurta = new JwtService(propriedadesCurtas);

        String token = servicoComExpiracaoCurta.generateAccessToken(usuario);
        Thread.sleep(50);

        assertThat(servicoComExpiracaoCurta.isValid(token, usuario)).isFalse();
    }
}
