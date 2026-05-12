package com.sistema.gestion.tickets.service;

import com.sistema.gestion.tickets.dto.AuthRequestDTO;
import com.sistema.gestion.tickets.dto.AuthResponseDTO;
import com.sistema.gestion.tickets.dto.RegisterRequestDTO;
import com.sistema.gestion.tickets.entity.User;
import com.sistema.gestion.tickets.enums.Role;
import com.sistema.gestion.tickets.exception.ResourceNotFoundException;
import com.sistema.gestion.tickets.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // Metodo para crear un usuario con la contraseña correctamente encriptada
    public AuthResponseDTO register(RegisterRequestDTO request) {

        // Verificamos si el correo ya existe
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El correo ya esta en uso.");
        }

        User user = User.builder()
                .email(request.email())
                // !AQUÍ ESTÁ LA MAGIA! Encriptamos la contraseña antes de guardarla
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.valueOf(request.role().toUpperCase()))
                .isActive(true)
                .build();

        userRepository.save(user);

        // Generamos el token inmediatamente después de registrarse
        String jwtToken = jwtService.generateToken(user);

        return new AuthResponseDTO(jwtToken);
    }

    // Método para iniciar sesión
    public AuthResponseDTO login(AuthRequestDTO request) {

        // 1. Delegamos a Spring Security la validación de la contraseña.
        // Si la contraseña es incorrecta, esto lanzará una excepción automáticamente.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // 2. Si paso la línea anterior, significa que el usuario y clave son correctos.
        // Lo buscamos en la BD para tener su objeto User completo.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 3. Le fabricamos su token
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponseDTO(jwtToken);
    }
}
