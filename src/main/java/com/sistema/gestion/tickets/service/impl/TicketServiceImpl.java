package com.sistema.gestion.tickets.service.impl;

import com.sistema.gestion.tickets.dto.AssignTicketRequestDTO;
import com.sistema.gestion.tickets.dto.TicketHistoryResponseDTO;
import com.sistema.gestion.tickets.dto.TicketRequestDTO;
import com.sistema.gestion.tickets.dto.TicketResponseDTO;
import com.sistema.gestion.tickets.entity.Ticket;
import com.sistema.gestion.tickets.entity.TicketHistory;
import com.sistema.gestion.tickets.entity.User;
import com.sistema.gestion.tickets.enums.TicketStatus;
import com.sistema.gestion.tickets.exception.ResourceNotFoundException;
import com.sistema.gestion.tickets.mapper.TicketMapper;
import com.sistema.gestion.tickets.repository.TicketHistoryRepository;
import com.sistema.gestion.tickets.repository.TicketRepository;
import com.sistema.gestion.tickets.repository.UserRepository;
import com.sistema.gestion.tickets.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;

    private final TicketHistoryRepository historyRepository;

    @Override
    @Transactional
    public TicketResponseDTO createTicket(TicketRequestDTO requestDTO, String creatorEmail) {

        // 1. Validar Reglas de Negocio: ¿Existe el usuario que intenta crear el ticket?
            // User creator = userRepository.findById(creatorId)
        // 1. Buscamos por el email extraído del token seguro
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario autenticado no existe en el sistema."));

        // 2. Transformar: Convertimos el JSON (DTO) a un objeto de Base de Datos (Entity)
        Ticket ticket = ticketMapper.toEntity(requestDTO, creator);

        // 3. Persistir: Guardamos en PostgreSQL
        Ticket savedTicket = ticketRepository.save(ticket);

        // 5. REGISTRAMOS EL HISTORIAL DE CREACIÓN(paso ultimo despues de crear createTicket)
        TicketHistory history = TicketHistory.builder()
                .ticket(savedTicket)
                .changedBy(creator) // El creador real respaldado por el token
                .actionDescription("Ticket reportado y abierto.")
                .oldStatus(null) // No había estado previo
                .newStatus(savedTicket.getStatus()) // OPEN
                .build();
        historyRepository.save(history);

        // 4. Retornar: Transformamos la Entity guardada de vuelta a JSON (DTO) para Angular
        return ticketMapper.toResponseDTO(savedTicket);
    }

    @Override
    @Transactional
    public TicketResponseDTO assignTicket(Long ticketId, AssignTicketRequestDTO assignDTO, String assignerEmail) {

        // 1. Buscamos el ticket. Fíjate que usamos nuestro método con JOIN FETCH para evitar el N+1
        Ticket ticket = ticketRepository.findByIdWithUsers(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("El ticket con ID "  + ticketId + " no existe"));

        // 2. Regla de Negocio Critica: Solo podemos asignar tickets que estén ABIERTOS
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new IllegalStateException("Solo se pueden asignar tickets en estado OPEN. Estado actual: " + ticket.getStatus());
        }

        // 3. Buscamos al agente que se hará cargo
        User agent = userRepository.findById(assignDTO.assigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("El agente con ID " + assignDTO.assigneeId() + " no existe"));

        User assigner = userRepository.findByEmail(assignerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario autenticado no existe en el sistema."));

        // Guardamos el estado viejo antes de cambiarlo
        TicketStatus previousStatus = ticket.getStatus();

        // REGISTRAMOS EL HISTORIAL DE ASIGNACIÓN(paso último después de crear el metodo assignTicket)
        // Nota: Asumimos por ahora que el "changedBy" es el mismo agente tomando el ticket.
        // Más adelante, con Spring Security, el changedBy será quien esté logueado.

        // 4. Modificamos el estado y asignamos al agente
        ticket.setAssignee(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        // 5. Guardamos
        Ticket updatedTicket = ticketRepository.save(ticket);

        TicketHistory history = TicketHistory.builder()
                .ticket(updatedTicket)
                .changedBy(assigner) // ¡AHORA SÍ! Queda registrado QUIÉN ordenó la asignación
                // .actionDescription("El agente " + agent.getEmail() + " tomo el ticket.")
                .actionDescription("El usuario " + assigner.getEmail() + " asignó el ticket al agente " + agent.getEmail())
                .oldStatus(previousStatus) // OPEN
                .newStatus(updatedTicket.getStatus()) // IN_PROGRESS
                .build();
        historyRepository.save(history);

        return ticketMapper.toResponseDTO(updatedTicket);
    }

    @Override
    public TicketResponseDTO getTicketById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        // ¡Aquí está la magia! No devuelvas 'ticket' directo, conviértelo a tu DTO
        return ticketMapper.toResponseDTO(ticket);
    }

    @Override
    public TicketResponseDTO updateTicketStatus(Long ticketId, String newStatus) {
        // 1. Buscamos el ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        // 2. Actualizamos el estado (Asumiendo que tu estado es un String.
        // Si usas un Enum, sería algo como TicketStatus.valueOf(newStatus))
        TicketStatus.valueOf(newStatus);

        // 3. Guardamos y convertimos a DTO para devolver a Angular
        ticketRepository.save(ticket);
        return ticketMapper.toResponseDTO(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponseDTO> getTicketsPaginated(int page, int size, String status) {

        // 1. Configuramos la paginación: Qué página, de qué tamaño, y ordenados por fecha descendente (los más nuevos primero)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Ticket> ticketPage;

        // Evaluamos si el cliente envió un filtro de estado
        if (status != null && !status.isBlank()) {
            try {
                // Intentamos convertir el texto (ej. "open") a nuestro Enum exacto ("OPEN")
                TicketStatus enumStatus = TicketStatus.valueOf(status.toUpperCase());

                // Si es válido, disparamos la consulta filtrada
                ticketPage = ticketRepository.findByStatus(enumStatus, pageable);
            } catch (IllegalArgumentException e) {
                // Si el cliente envía "?status=HOLA", atrapamos el error para no colapsar
                throw new IllegalArgumentException("Estado de ticket no valido: " +  status);
            }
        } else {
            // Si el cliente no envió nada, mantenemos el comportamiento original (traer todos)
            // 2. Consultamos a la base de datos. JPA hará un "SELECT ... LIMIT ? OFFSET ?"
            ticketPage = ticketRepository.findAll(pageable);
        }

        // 3. Transformamos la Página de Entidades a una Página de DTOs
        // El método .map() itera internamente y aplica nuestro ticketMapper a cada elemento
        return ticketPage.map(ticketMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketHistoryResponseDTO> getTicketHistory(Long ticketId) {

        // 1. Validamos que el ticket exista antes de buscar su historia
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket con ID " + ticketId + " no existe");
        }

        // 2. Buscamos el historial ordenado cronológicamente (el método que creamos en el repo)
        List<TicketHistory> historyList = historyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);

        // 3. Transformamos la lista de Entidades a lista de DTOs usando la API de Streams de Java
        return historyList.stream()
                .map(ticketMapper::toHistoryResponseDTO)
                .collect(Collectors.toList());
    }
}
