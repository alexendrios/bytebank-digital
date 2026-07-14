package com.bytebank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Conta bancária vinculada a um {@link Usuario}.
 *
 * <p>Concorrência: toda operação que altera {@code saldo} deve buscar a
 * conta através de {@code ContaRepository#buscarParaAtualizacao} (lock
 * pessimista {@code SELECT ... FOR UPDATE}), que serializa escritores
 * concorrentes na mesma linha. O campo {@code version} funciona como
 * segunda camada de proteção (locking otimista): qualquer caminho de
 * escrita que porventura não passe pelo lock pessimista falha com
 * {@link jakarta.persistence.OptimisticLockException} em vez de perder
 * silenciosamente uma atualização concorrente.</p>
 */
@Entity
@Table(name = "contas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conta {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @Column(nullable = false, length = 10)
    private String agencia;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        if (saldo == null) {
            saldo = BigDecimal.ZERO;
        }
    }
}
