package com.bytebank.controller;

import com.bytebank.dto.request.TransferenciaRequest;
import com.bytebank.dto.response.TransferenciaResponse;
import com.bytebank.service.BancoFacade;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transferencias")
@RequiredArgsConstructor
@Tag(name = "Transferências")
public class TransferenciaController {

    private final BancoFacade bancoFacade;

    @PostMapping
    public ResponseEntity<TransferenciaResponse> transferir(@Valid @RequestBody TransferenciaRequest request) {
        TransferenciaResponse response = bancoFacade.transferir(
                request.contaOrigemId(),
                request.contaDestinoId(),
                request.valor()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
