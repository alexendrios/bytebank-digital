package com.bytebank.exception;

/**
 * Exceção lançada quando um recurso (usuário, conta, etc.) não é encontrado.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
