package com.bytebank.dto.response;

import com.bytebank.entity.Perfil;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        Perfil perfil,
        LocalDateTime dataCadastro
) {
}
