package com.ricontatto.repository;

import com.ricontatto.model.Prenotazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrenotazioneRepository extends JpaRepository<Prenotazione, Long> {
    List<Prenotazione> findByGiorno(LocalDate giorno);
    List<Prenotazione> findByEmail(String email);

    boolean existsByCellulare(String cellulare);
    boolean existsByCellulareAndGiornoGreaterThanEqual(String cellulare, LocalDate giorno);

    Optional<Prenotazione> findFirstByCellulareAndGiornoGreaterThanEqual(String cellulare, LocalDate giorno);

    List<Prenotazione> findByGiornoAndOraInizioLessThanAndOraFineGreaterThan(
        LocalDate giorno,
        LocalTime oraFine,
        LocalTime oraInizio
    );

    long countByGiornoAndOraInizioAndOraFine(LocalDate giorno, LocalTime oraInizio, LocalTime oraFine);
}
