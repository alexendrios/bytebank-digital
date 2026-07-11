package com.bytebank.repository;

import com.bytebank.entity.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaRepository extends JpaRepository<Conta, UUID> {

    Optional<Conta> findByNumero(String numero);

    boolean existsByNumero(String numero);

    List<Conta> findByUsuarioId(UUID usuarioId);
}
