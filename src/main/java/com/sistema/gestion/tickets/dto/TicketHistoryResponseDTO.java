package com.sistema.gestion.tickets.dto;

import java.time.LocalDateTime;

public record TicketHistoryResponseDTO (
        String actionDescription,
        String oldStatus,
        String newStatus,
        String changedByEmail,
        LocalDateTime createdAt
){}