package com.bytebank.config;

import com.bytebank.entity.Perfil;
import com.bytebank.entity.Usuario;
import com.bytebank.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolve o problema de "ovo e galinha" do RBAC: como {@code /usuarios} só
 * é acessível por ADMIN, precisa existir um jeito de criar o primeiro ADMIN
 * sem depender de outro ADMIN já existente. Este runner cria esse usuário
 * automaticamente na subida da aplicação, apenas se ainda não houver nenhum
 * ADMIN cadastrado — depois disso, ele nunca mais recria ou altera nada.
 *
 * Credenciais configuráveis via {@code app.admin-bootstrap.*}
 * (ver application.yml). Pode ser desligado com
 * {@code app.admin-bootstrap.enabled=false}.
 */
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminBootstrapProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }

        boolean existeAdmin = usuarioRepository.findAll().stream()
                .anyMatch(usuario -> usuario.getPerfil() == Perfil.ADMIN);

        if (existeAdmin) {
            return;
        }

        if (usuarioRepository.existsByEmail(properties.email())) {
            log.warn("Bootstrap de ADMIN ignorado: já existe um usuário CLIENTE com o e-mail {}. "
                    + "Promova-o manualmente ou ajuste app.admin-bootstrap.email.", properties.email());
            return;
        }

        Usuario admin = Usuario.builder()
                .nome(properties.nome())
                .email(properties.email())
                .senha(passwordEncoder.encode(properties.senha()))
                .perfil(Perfil.ADMIN)
                .build();

        usuarioRepository.save(admin);

        log.info("Usuário ADMIN inicial criado automaticamente: {}. "
                + "Altere a senha padrão assim que possível.", properties.email());
    }
}
