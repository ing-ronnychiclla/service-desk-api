package com.sistema.gestion.tickets.repository;

import com.sistema.gestion.tickets.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    // Este método nos servirá después para mostrar el "timeline" en Angular
    // Usamos OrderBy para que vengan ordenados cronológicamente
    List<TicketHistory> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
