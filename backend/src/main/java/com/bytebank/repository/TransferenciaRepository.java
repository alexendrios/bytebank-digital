package com.bytebank.repository;

import com.bytebank.entity.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferenciaRepository extends JpaRepository<Transferencia, UUID> {
}
