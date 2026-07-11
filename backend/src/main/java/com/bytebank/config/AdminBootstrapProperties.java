package com.bytebank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credenciais do usuário ADMIN inicial, criado automaticamente na subida
 * da aplicação caso ainda não exista nenhum ADMIN cadastrado. Ver
 * {@link AdminBootstrapRunner}.
 */
@ConfigurationProperties(prefix = "app.admin-bootstrap")
public record AdminBootstrapProperties(
        boolean enabled,
        String nome,
        String email,
        String senha
) {
}
