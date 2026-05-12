package com.sistema.gestion.tickets.repository;

import com.sistema.gestion.tickets.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA arma el "SELECT * FROM users WHERE email = ?" automaticamente
    Optional<User> findByEmail(String email);

    // Util para validar si un correo ya esta registrado antes de crear uno nuevo
    boolean existsByEmail(String email);
}
