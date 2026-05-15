package com.sistema.gestion.tickets.service;

import com.sistema.gestion.tickets.dto.AssignTicketRequestDTO;
import com.sistema.gestion.tickets.dto.TicketHistoryResponseDTO;
import com.sistema.gestion.tickets.dto.TicketRequestDTO;
import com.sistema.gestion.tickets.dto.TicketResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TicketService {

    // Recibimos el DTO de Angular y el ID de quién lo está creando.
    // TicketResponseDTO createTicket(TicketRequestDTO requestDTO, Long creatorId);
    // TicketResponseDTO assignTicket(Long ticketId, AssignTicketRequestDTO assignDTO);
    TicketResponseDTO createTicket(TicketRequestDTO requestDTO, String creatorEmail);

    TicketResponseDTO assignTicket(Long ticketId, AssignTicketRequestDTO assignDTO, String assignerEmail);

    TicketResponseDTO getTicketById(Long ticketId);

    TicketResponseDTO updateTicketStatus(Long ticketId, String newStatus);

    Page<TicketResponseDTO> getTicketsPaginated(int page, int size, String status);

    List<TicketHistoryResponseDTO> getTicketHistory(Long ticketId);


}
