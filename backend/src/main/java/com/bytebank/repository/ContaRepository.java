package com.bytebank.repository;

import com.bytebank.entity.Conta;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaRepository extends JpaRepository<Conta, UUID> {

    Optional<Conta> findByNumero(String numero);

    boolean existsByNumero(String numero);

    List<Conta> findByUsuarioId(UUID usuarioId);

    /**
     * Busca a conta bloqueando a linha correspondente no banco
     * ({@code SELECT ... FOR UPDATE}) até o fim da transação corrente.
     * Deve ser usada por toda operação que lê o saldo para em seguida
     * modificá-lo (depósito, saque, transferência), evitando que duas
     * transações concorrentes leiam o mesmo saldo "antigo" e uma
     * atualização se perca (lost update).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("select c from Conta c where c.id = :id")
    Optional<Conta> buscarParaAtualizacao(@Param("id") UUID id);
}
