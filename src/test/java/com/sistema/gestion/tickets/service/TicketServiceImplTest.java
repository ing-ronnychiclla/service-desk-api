package com.sistema.gestion.tickets.service;

import com.sistema.gestion.tickets.dto.TicketRequestDTO;
import com.sistema.gestion.tickets.dto.TicketResponseDTO;
import com.sistema.gestion.tickets.entity.Ticket;
import com.sistema.gestion.tickets.entity.TicketHistory;
import com.sistema.gestion.tickets.entity.User;
import com.sistema.gestion.tickets.enums.Role;
import com.sistema.gestion.tickets.enums.TicketStatus;
import com.sistema.gestion.tickets.mapper.TicketMapper;
import com.sistema.gestion.tickets.repository.TicketHistoryRepository;
import com.sistema.gestion.tickets.repository.TicketRepository;
import com.sistema.gestion.tickets.repository.UserRepository;
import com.sistema.gestion.tickets.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Le decimos a JUnit que usaremos Mockito para simular objetos
@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {

    // ==========================================
    // 1. LOS MOCKS (Los dobles de acción)
    // ==========================================
    // No queremos tocar la base de datos real, así que simulamos los repositorios
    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketHistoryRepository historyRepository;

    @Mock
    private TicketMapper ticketMapper;

    // ==========================================
    // 2. EL SUJETO DE PRUEBA
    // ==========================================
    // Inyectamos los "Mocks" falsos dentro de nuestro servicio real
    @InjectMocks
    private TicketServiceImpl ticketService;

    // ==========================================
    // 3. LA PRUEBA UNITARIA
    // ==========================================
    @Test
    void createTicket_Successful_ReturnsTicketResponseDTO() {

        // --- GIVEN (Escenario) ---
        String userEmail = "empleado@empresa.com";
        TicketRequestDTO requestDTO = new TicketRequestDTO("Falla en teclado", "No funciona la tecla enter");

        User mockUser = User.builder().id(1L).email(userEmail).role(Role.EMPLOYEE).build();

        Ticket mockTicket = Ticket.builder()
                .id(100L)
                .title(requestDTO.title())
                .description(requestDTO.description())
                .status(TicketStatus.OPEN)
                .creator(mockUser)
                .build();

        TicketResponseDTO expectedResponse = new TicketResponseDTO(
                100L, "Falla en teclado", "No funciona la tecla enter", "OPEN", userEmail, null, LocalDateTime.now()
        );

        // Le enseñamos a los Mocks cómo deben comportarse cuando el servicio los llame
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));
        when(ticketMapper.toEntity(requestDTO, mockUser)).thenReturn(mockTicket);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(mockTicket);
        when(ticketMapper.toResponseDTO(mockTicket)).thenReturn(expectedResponse);

        // --- WHEN (Acción) ---
        // Ejecutamos nuestro código de negocio
        TicketResponseDTO actualResponse = ticketService.createTicket(requestDTO, userEmail);

        // --- THEN (Verificación) ---
        // 1. Verificamos que la respuesta no sea nula
        assertNotNull(actualResponse);

        // 2. Verificamos que el título coincida
        assertEquals("Falla en teclado", actualResponse.title());

        // 3. Verificamos que el estado sea el correcto
        assertEquals("OPEN", actualResponse.status());

        // 4. Verificamos que los repositorios fueron llamados exactamente 1 vez
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(ticketRepository, times(1)).save(mockTicket);

        // 5. Verificamos que el registro de auditoría también se haya guardado
        verify(historyRepository, times(1)).save(any(TicketHistory.class));
    }
}
