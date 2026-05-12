package com.sistema.gestion.tickets.dto;

import jakarta.validation.constraints.NotNull;

public record AssignTicketRequestDTO (
    @NotNull(message = "El ID del agente es obligatorio")
    Long assigneeId
)
{}
