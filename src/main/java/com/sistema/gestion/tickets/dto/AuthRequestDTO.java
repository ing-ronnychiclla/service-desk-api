package com.sistema.gestion.tickets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 1. Lo que nos envía Angular para Loguearse
public record AuthRequestDTO(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Debe ser un formato de correo valido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
