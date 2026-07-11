package com.bytebank.mapper;

import com.bytebank.dto.response.MovimentacaoResponse;
import com.bytebank.entity.Movimentacao;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovimentacaoMapper {

    MovimentacaoResponse toResponse(Movimentacao movimentacao);
}
