package com.sistema.gestion.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketRequestDTO(

        // EL DTO DE ENTRADA(Lo que nos envia Angular)

        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 150, message = "El titulo no puede exceder los 150 caracteres")
        String title,

        @NotBlank(message = "La descripcion es obligatoria")
        String description

        // No pedimos ni el estado (siempre nace OPEN),
        // ni la fecha, ni el usuario creador (eso lo sacamos del token JWT de seguridad).
) {}
