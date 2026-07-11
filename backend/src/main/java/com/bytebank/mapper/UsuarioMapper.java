package com.bytebank.mapper;

import com.bytebank.dto.response.UsuarioResponse;
import com.bytebank.entity.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario usuario);
}
