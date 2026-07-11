package com.bytebank.mapper;

import com.bytebank.dto.response.TransferenciaResponse;
import com.bytebank.entity.Transferencia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransferenciaMapper {

    @Mapping(target = "contaOrigemId", source = "contaOrigem.id")
    @Mapping(target = "contaOrigemNumero", source = "contaOrigem.numero")
    @Mapping(target = "contaDestinoId", source = "contaDestino.id")
    @Mapping(target = "contaDestinoNumero", source = "contaDestino.numero")
    TransferenciaResponse toResponse(Transferencia transferencia);
}
