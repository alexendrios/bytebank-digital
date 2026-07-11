package com.bytebank.exception;

/**
 * Exceção lançada quando há conflito com um recurso já existente
 * (ex.: e-mail já cadastrado).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
