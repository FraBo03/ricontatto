package com.ricontatto.repository;

import com.ricontatto.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtenteRepository extends JpaRepository<Utente, String> {
    boolean existsByEmail(String email);
}
