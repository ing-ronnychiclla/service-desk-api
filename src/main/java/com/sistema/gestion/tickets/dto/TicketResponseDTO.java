package com.sistema.gestion.tickets.dto;

import java.time.LocalDateTime;

public record TicketResponseDTO(

        // EL DTO DE SALIDA(Lo que le enviamos a Angular)

        Long id,
        String title,
        String description,
        String status,

        // Aplanamos la información. En lugar de mandar un objeto User completo,
        // solo mandamos el email para que Angular lo muestre en pantalla.
        String creatorEmail,
        String assigneeEmail,

        LocalDateTime createdAt
) {}
