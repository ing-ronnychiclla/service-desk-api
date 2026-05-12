package com.sistema.gestion.tickets.exception;

// Heredamos de RuntimeException para que Spring pueda hacer Rollback en la base de datos si ocurre un error
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
