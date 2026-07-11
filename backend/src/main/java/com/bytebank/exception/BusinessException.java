package com.bytebank.exception;

/**
 * Exceção lançada quando uma regra de negócio é violada
 * (ex.: saldo insuficiente, limite de transferência excedido).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
