package com.ricontatto.repository;

import com.ricontatto.model.Motivazione;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MotivazioneRepository extends JpaRepository<Motivazione, Long> {
    List<Motivazione> findByDescrizioneIn(List<String> descrizioni);
}
