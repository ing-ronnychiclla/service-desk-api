package com.sistema.gestion.tickets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 2. Lo que nos envía Angular para Registrar un nuevo agente
public record RegisterRequestDTO(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Debe ser un formato de correo valido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password,

        @NotBlank(message = "El rol es obligatorio (ADMIN, AGENT, EMPLOYEE)")
        String role
) {
}
