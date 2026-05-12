package com.sistema.gestion.tickets.mapper;

import com.sistema.gestion.tickets.dto.TicketHistoryResponseDTO;
import com.sistema.gestion.tickets.dto.TicketRequestDTO;
import com.sistema.gestion.tickets.dto.TicketResponseDTO;
import com.sistema.gestion.tickets.entity.Ticket;
import com.sistema.gestion.tickets.entity.TicketHistory;
import com.sistema.gestion.tickets.entity.User;
import com.sistema.gestion.tickets.enums.TicketStatus;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    /**
     * Convierte la Entidad de la Base de Datos al DTO de salida para Angular.
     */
    public TicketResponseDTO toResponseDTO(Ticket ticket){
        if (ticket == null){
            return null;
        }

        // Manejo seguro de nulos: Un ticket recién creado no tiene agente asignado
        String creatorEmail = ticket.getCreator() != null ? ticket.getCreator().getEmail() : "Desconocido";
        String assigneeEmail = ticket.getAssignee() != null ? ticket.getAssignee().getEmail() : "Sin asignar";

        return new TicketResponseDTO(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus().name(), // Convertimos el Enum a String
                creatorEmail,
                assigneeEmail,
                ticket.getCreatedAt()
        );
    }

    /**
     * Convierte el DTO que viene del frontend (Angular) a una Entidad para guardar en BD.
     * Recibe también al Usuario creador, ya que el DTO solo trae texto.
     */
    public Ticket toEntity(TicketRequestDTO dto, User creator){
        if (dto == null){
            return null;
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(dto.title());
        ticket.setDescription(dto.description());
        ticket.setCreator(creator);
        ticket.setStatus(TicketStatus.OPEN); // Todo ticket nuevo nace como OPEN por regla de negocio

        // No seteamos el ID ni las fechas, de eso se encarga JPA (Hibernate)
        return ticket;
    }


    public TicketHistoryResponseDTO toHistoryResponseDTO(TicketHistory history) {
        if (history == null){
            return null;
        }
        // Manejamos el caso en que oldStatus es nulo (cuando el ticket recién se crea)
        String oldStatus = history.getOldStatus() != null ? history.getOldStatus().name() : "N/A";

        return new TicketHistoryResponseDTO(
                history.getActionDescription(),
                oldStatus,
                history.getNewStatus().name(),
                history.getChangedBy().getEmail(),
                history.getCreatedAt()
        );
    }
}
