package com.bytebank.integration;

import com.bytebank.dto.request.LoginRequest;
import com.bytebank.dto.request.RefreshTokenRequest;
import com.bytebank.dto.request.RegisterRequest;
import com.bytebank.dto.response.AuthResponse;
import com.bytebank.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração ponta a ponta do fluxo de autenticação, exercitando
 * Controller → Service → Security → Repository contra um PostgreSQL real.
 */
class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @AfterEach
    void limpar() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /auth/register deve criar usuário e retornar tokens")
    void deveRegistrarNovoUsuario() {
        RegisterRequest request = new RegisterRequest("Maria Silva", "maria@bytebank.com", "senha1234");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/auth/register", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
        assertThat(usuarioRepository.existsByEmail("maria@bytebank.com")).isTrue();
    }

    @Test
    @DisplayName("POST /auth/register com e-mail duplicado deve retornar 409")
    void deveRetornarConflitoParaEmailDuplicado() {
        RegisterRequest request = new RegisterRequest("Maria Silva", "maria@bytebank.com", "senha1234");
        restTemplate.postForEntity("/auth/register", request, AuthResponse.class);

        ResponseEntity<String> response = restTemplate.postForEntity("/auth/register", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("POST /auth/login com credenciais válidas deve retornar tokens")
    void deveLogarComCredenciaisValidas() {
        RegisterRequest registerRequest = new RegisterRequest("Maria Silva", "maria@bytebank.com", "senha1234");
        restTemplate.postForEntity("/auth/register", registerRequest, AuthResponse.class);

        LoginRequest loginRequest = new LoginRequest("maria@bytebank.com", "senha1234");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/auth/login", loginRequest, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("POST /auth/login com senha incorreta deve retornar 401")
    void deveRetornarUnauthorizedParaSenhaIncorreta() {
        RegisterRequest registerRequest = new RegisterRequest("Maria Silva", "maria@bytebank.com", "senha1234");
        restTemplate.postForEntity("/auth/register", registerRequest, AuthResponse.class);

        LoginRequest loginRequest = new LoginRequest("maria@bytebank.com", "senha-errada");
        ResponseEntity<String> response = restTemplate.postForEntity("/auth/login", loginRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /auth/refresh-token deve emitir novos tokens a partir de um refresh token válido")
    void deveRenovarTokenComRefreshTokenValido() {
        RegisterRequest registerRequest = new RegisterRequest("Maria Silva", "maria@bytebank.com", "senha1234");
        AuthResponse registerResponse = restTemplate.postForEntity("/auth/register", registerRequest, AuthResponse.class).getBody();

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(registerResponse.refreshToken());
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/auth/refresh-token", refreshRequest, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("Acessar endpoint protegido sem token deve retornar 401")
    void deveRetornarUnauthorizedSemToken() {
        ResponseEntity<String> response = restTemplate.getForEntity("/contas", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
