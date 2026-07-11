package com.bytebank.mapper;

import com.bytebank.dto.response.ContaResponse;
import com.bytebank.entity.Conta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContaMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioNome", source = "usuario.nome")
    ContaResponse toResponse(Conta conta);
}
