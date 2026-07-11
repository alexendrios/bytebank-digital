package com.bytebank.controller;

import com.bytebank.dto.request.ContaRequest;
import com.bytebank.dto.request.ValorOperacaoRequest;
import com.bytebank.dto.response.ContaResponse;
import com.bytebank.dto.response.MovimentacaoResponse;
import com.bytebank.service.BancoFacade;
import com.bytebank.service.ContaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints de contas e operações bancárias (depósito, saque, extrato).
 * Regras de propriedade (CLIENTE só acessa a própria conta) são
 * reforçadas em {@link ContaService} e {@link BancoFacade}.
 */
@RestController
@RequestMapping("/contas")
@RequiredArgsConstructor
@Tag(name = "Contas")
public class ContaController {

    private final ContaService contaService;
    private final BancoFacade bancoFacade;

    @GetMapping
    public ResponseEntity<List<ContaResponse>> listar() {
        return ResponseEntity.ok(contaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contaService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody ContaRequest request) {
        return ResponseEntity.ok(contaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        contaService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deposito")
    public ResponseEntity<ContaResponse> depositar(@PathVariable UUID id, @Valid @RequestBody ValorOperacaoRequest request) {
        return ResponseEntity.ok(bancoFacade.depositar(id, request.valor()));
    }

    @PostMapping("/{id}/saque")
    public ResponseEntity<ContaResponse> sacar(@PathVariable UUID id, @Valid @RequestBody ValorOperacaoRequest request) {
        return ResponseEntity.ok(bancoFacade.sacar(id, request.valor()));
    }

    @GetMapping("/{id}/extrato")
    public ResponseEntity<Page<MovimentacaoResponse>> extrato(
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(bancoFacade.extrato(id, pageable));
    }
}
