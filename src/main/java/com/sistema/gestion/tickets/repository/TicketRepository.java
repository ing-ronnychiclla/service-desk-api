package com.sistema.gestion.tickets.repository;

import com.sistema.gestion.tickets.entity.Ticket;
import com.sistema.gestion.tickets.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Devuelve todos los tickets asignados a un agente específico
    List<Ticket> findByAssigneeId(Long assigneeId);

    // Devuelve todos los tickets que tienen un estado específico (ej. OPEN)
    List<Ticket> findByStatus(TicketStatus status);

    // Consulta personalizada usando JPQL para evitar el problema N+1
    @Query("SELECT t FROM Ticket t JOIN FETCH t.creator LEFT JOIN FETCH t.assignee WHERE t.id = :id")
    Optional<Ticket> findByIdWithUsers(@Param("id") Long id);

    // Este método devuelve una "Página" de tickets en lugar de una simple "Lista"
    Page<Ticket> findAll(Pageable pageable);

    // Devuelve una página de tickets filtrados por su estado
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
}
