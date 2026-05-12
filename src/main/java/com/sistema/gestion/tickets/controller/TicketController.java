package com.sistema.gestion.tickets.controller;

import com.sistema.gestion.tickets.dto.AssignTicketRequestDTO;
import com.sistema.gestion.tickets.dto.TicketHistoryResponseDTO;
import com.sistema.gestion.tickets.dto.TicketRequestDTO;
import com.sistema.gestion.tickets.dto.TicketResponseDTO;
import com.sistema.gestion.tickets.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(
            @Valid @RequestBody TicketRequestDTO requestDTO,
            // Simulamos que obtenemos el ID del usuario desde un Header por ahora.
            // Cuando implementemos Spring Security, esto se extraerá automáticamente del Token JWT.
            // @RequestHeader("X-User-Id") Long creatorId)
            // ¡Adiós al Header inseguro! Hola a la seguridad de Spring
            Authentication authentication) {

        // authentication.getName() nos devuelve el 'subject' del JWT (que configuramos como el email)
        String userEmail = authentication.getName();

        TicketResponseDTO response = ticketService.createTicket(requestDTO, userEmail);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponseDTO> assignTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignTicketRequestDTO assignDTO,
            Authentication authentication) {

        String assignerEmail = authentication.getName();
        TicketResponseDTO response = ticketService.assignTicket(ticketId, assignDTO, assignerEmail);

        return ResponseEntity.ok(response); // Retornamos un HTTP 200 OK
    }

    @GetMapping
    public ResponseEntity<Page<TicketResponseDTO>> getTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            // required = false significa que este parámetro es opcional
            @RequestParam(required = false) String status) {

        Page<TicketResponseDTO> response = ticketService.getTicketsPaginated(page, size, status);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ticketId}/history")
    public ResponseEntity<List<TicketHistoryResponseDTO>> getTicketHistory(
            @PathVariable Long ticketId) {

        List<TicketHistoryResponseDTO> response = ticketService.getTicketHistory(ticketId);

        return ResponseEntity.ok(response);
    }

}
