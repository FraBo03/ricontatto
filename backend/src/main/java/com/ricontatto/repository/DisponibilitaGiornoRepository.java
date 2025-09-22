package com.ricontatto.repository;

import com.ricontatto.model.DisponibilitaGiorno;
import com.ricontatto.model.FasciaOraria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibilitaGiornoRepository extends JpaRepository<DisponibilitaGiorno, Long> {
    List<DisponibilitaGiorno> findAllByGiorno(LocalDate giorno);
    Optional<DisponibilitaGiorno> findByGiornoAndFasciaOraria(LocalDate giorno, FasciaOraria fasciaOraria);

    @Query("SELECT DISTINCT d.giorno FROM DisponibilitaGiorno d WHERE d.disponibilita > 0")
    List<LocalDate> findGiorniConAlmenoUnaDisponibilita();
}
