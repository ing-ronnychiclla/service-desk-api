package com.sistema.gestion.tickets.entity;

import com.sistema.gestion.tickets.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // ==========================================================
    // MÉTODOS DE LA INTERFAZ UserDetails (Contrato de Seguridad)
    // ==========================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Transformamos nuestro Enum Role en un formato que Spring Security entienda
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        // En nuestro sistema, el nombre de usuario es el correo
        return  this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // No manejamos expiración de cuenta por ahora
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // No manejamos bloqueo por intentos fallidos por ahora
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // No manejamos expiración de credenciales por ahora
    }

    @Override
    public boolean isEnabled() {
        // Conectamos el estado de Spring con nuestro campo de base de datos
        return this.isActive;
    }
}
