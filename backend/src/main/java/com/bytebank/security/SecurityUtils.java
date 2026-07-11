package com.bytebank.security;

import com.bytebank.entity.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Acesso utilitário ao {@link Usuario} atualmente autenticado.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new IllegalStateException("Nenhum usuário autenticado no contexto atual");
        }
        return usuario;
    }

    public static boolean isAdmin() {
        return getUsuarioAutenticado().getPerfil() == com.bytebank.entity.Perfil.ADMIN;
    }
}
