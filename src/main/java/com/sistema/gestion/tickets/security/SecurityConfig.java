package com.sistema.gestion.tickets.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    // ==========================================
    // 1. REGLAS DE LAS RUTAS HTTP (El Cadenero)
    // ==========================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                // 1. ACTIVAMOS CORS AQUÍ
                .cors(withDefaults())

                // Desactivamos CSRF (Cross-Site Request Forgery)
                // Ya que usamos JWT, no dependemos de las Cookies del navegador, por lo que CSRF es innecesario.
                // 2. Desactivamos CSRF como ya lo teníamos
                .csrf(AbstractHttpConfigurer::disable)

                // ... (el resto del código se mantiene igual)
                // Configuramos los permisos de las rutas
                .authorizeHttpRequests(auth -> auth
                        // Dejamos las rutas de autenticación (Login/Registro) públicas para que cualquiera pueda entrar
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // TODAS las demás rutas exigen que el usuario esté autenticado
                        .anyRequest().authenticated()
                )

                // Política de Sesión SIN ESTADO (Stateless)
                // Le decimos a Spring: "No guardes la sesión en memoria. Cada petición debe validarse de cero usando el Token."
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Inyectamos el Proveedor de Autenticación
                .authenticationProvider(authenticationProvider())

                // ¡EL PASO FINAL! Colocamos nuestro Filtro JWT ANTES del filtro tradicional de Usuario/Contraseña
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ==========================================
    // 2. CONFIGURACIONES BASE DE SPRING SECURITY
    // ==========================================


    // El motor principal que verifica credenciales
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(userDetailsService);

        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    // El Administrador global de autenticación (lo usaremos luego en el Login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Algoritmo para encriptar contraseñas. ¡Nunca guardamos en texto plano!
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // Importaciones necesarias en la parte superior:
    // import org.springframework.web.cors.CorsConfiguration;
    // import org.springframework.web.cors.CorsConfigurationSource;
    // import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
    // import java.util.List;
    // import static org.springframework.security.config.Customizer.withDefaults;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // El origen de tu Frontend en Angular
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));

        // Los métodos HTTP que Angular tiene permitido usar
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Los headers permitidos (importante para enviar nuestro Bearer Token)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicamos esta regla a todas nuestras rutas de la API
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
