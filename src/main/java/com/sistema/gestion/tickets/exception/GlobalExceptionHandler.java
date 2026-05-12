package com.sistema.gestion.tickets.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Este método atrapa específicamente nuestra ResourceNotFoundException
    // 404 - Recurso no encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(), // 404
                HttpStatus.NOT_FOUND.getReasonPhrase(), // "Not Found"
                ex.getMessage(), // "Usuario no encontrado en el sistema"
                request.getRequestURI() // "/api/tickets"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Un atrapalotodo (Catch-all) para cualquier otro error no controlado (NullPointerException, caída de BD, etc.)
    // 500 - Error global inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
                HttpStatus.UNAUTHORIZED.getReasonPhrase(), // "Internal Server Error"
                "Ocurrió un error inesperado en el servidor. Contacte a soporte.", // Ocultamos el error real por seguridad
                request.getRequestURI()
        );

        // Opcional pero recomendado: Hacer un log.error(ex.getMessage(), ex) aquí para que quede en consola.

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 400 - Errores de validación DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        // Extraemos todos los campos que fallaron y sus mensajes (ej. "title": "El título es obligatorio")
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Este método atrapa cualquier IllegalStateException lanzada en la aplicación.
    // Se usa normalmente para errores de lógica de negocio,
    // por ejemplo: intentar asignar un ticket que no está en estado OPEN.
    // 400 - Error de lógica de negocio
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request) {

        // Construimos una respuesta de error personalizada
        ErrorResponse error = new ErrorResponse(
                // Fecha y hora exacta del error
                LocalDateTime.now(),
                // Código HTTP 400 (Bad Request)
                HttpStatus.BAD_REQUEST.value(),
                // Texto descriptivo del estado HTTP -> "Bad Request"
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                // Mensaje específico de la excepción
                // Ejemplo: "Solo se pueden asignar tickets OPEN"
                ex.getMessage(),
                // Endpoint donde ocurrió el error
                // Ejemplo: "/api/tickets/5/assign"
                request.getRequestURI()
        );
        // Retornamos la respuesta HTTP con estado 400
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
