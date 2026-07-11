package com.bytebank.dto.request;

import com.bytebank.entity.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String senha,

        @NotNull(message = "Perfil é obrigatório")
        Perfil perfil
) {
}
