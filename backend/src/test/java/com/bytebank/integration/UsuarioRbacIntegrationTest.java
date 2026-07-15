package com.bytebank.integration;

import com.bytebank.entity.Perfil;
import com.bytebank.entity.Usuario;
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
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante que os endpoints /usuarios/** são acessíveis apenas por usuários
 * com perfil ADMIN, conforme definido na SecurityConfig.
 */
class UsuarioRbacIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String tokenAdmin;
    private String tokenCliente;

    private UUID adminId;
    private UUID clienteId;

    private boolean adminCriadoPeloTeste;
    private boolean clienteCriadoPeloTeste;
    

    @BeforeEach
    void setUp() {

        Usuario admin = usuarioRepository.findByEmail("admin@bytebank.com")
                .orElseGet(() -> {
                    adminCriadoPeloTeste = true;
                    Usuario novoAdmin = Usuario.builder()
                            .nome("Admin")
                            .email("admin@bytebank.com")
                            .senha(passwordEncoder.encode("senha1234"))
                            .perfil(Perfil.ADMIN)
                            .build();

                    return usuarioRepository.save(novoAdmin);
                });

        adminId = admin.getId();

        Usuario cliente = usuarioRepository.findByEmail("cliente@bytebank.com")
                .orElseGet(() -> {
                    clienteCriadoPeloTeste = true;
                    Usuario novoCliente = Usuario.builder()
                            .nome("Cliente")
                            .email("cliente@bytebank.com")
                            .senha(passwordEncoder.encode("senha1234"))
                            .perfil(Perfil.CLIENTE)
                            .build();

                    return usuarioRepository.save(novoCliente);
                });

        clienteId = cliente.getId();

        tokenAdmin = jwtService.generateAccessToken(admin);
        tokenCliente = jwtService.generateAccessToken(cliente);
    }

    @AfterEach
    void limpar() {


        if (clienteCriadoPeloTeste) {
            usuarioRepository.deleteById(clienteId);
        }

        if (adminCriadoPeloTeste) {
            usuarioRepository.deleteById(adminId);
        }
    }

    private HttpEntity<Void> entityCom(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    @Test
    @DisplayName("ADMIN deve conseguir listar usuários")
    void adminDeveListarUsuarios() {

        ResponseEntity<String> response = restTemplate.exchange(
                "/usuarios",
                HttpMethod.GET,
                entityCom(tokenAdmin),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("CLIENTE deve receber 403 ao tentar listar usuários")
    void clienteDeveReceberForbidden() {

        ResponseEntity<String> response = restTemplate.exchange(
                "/usuarios",
                HttpMethod.GET,
                entityCom(tokenCliente),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}