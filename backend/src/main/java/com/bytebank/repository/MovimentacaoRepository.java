package com.bytebank.repository;

import com.bytebank.entity.Movimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, UUID> {

    Page<Movimentacao> findByContaIdOrderByDataDesc(UUID contaId, Pageable pageable);
}
