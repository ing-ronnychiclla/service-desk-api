package com.sistema.gestion.tickets.security;

import com.sistema.gestion.tickets.repository.UserRepository;
import com.sistema.gestion.tickets.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extraer el Header de Autorización
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Si no hay header o no empieza con "Bearer ", lo dejamos pasar.
        // (Spring Security lo bloqueará más adelante si la ruta era privada).
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraemos el token (cortamos los primeros 7 caracteres: "Bearer ")
        jwt = authHeader.substring(7);

        // 4. Extraemos el email del token usando nuestro JwtService
        userEmail = jwtService.extractUsername(jwt);

        // 5. Si hay un email y el usuario aún no está autenticado en el contexto actual
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Buscamos al usuario en la base de datos
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // 6. Validamos matemáticamente el token
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 7. Si es válido, creamos el "Pase VIP" de Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // No pasamos credenciales (contraseña) por seguridad
                        userDetails.getAuthorities() // Los roles (ej. ROLE_ADMIN)
                );

                // Le agregamos detalles extra de la petición web (IP, sesión, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 8. Registramos oficialmente al usuario en el Contexto de Seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 9. Pasamos al siguiente filtro de la cadena
        filterChain.doFilter(request, response);
    }
}
