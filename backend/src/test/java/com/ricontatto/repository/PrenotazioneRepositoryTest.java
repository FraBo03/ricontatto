package com.ricontatto.repository;

import com.ricontatto.model.FasciaOraria;
import com.ricontatto.model.Prenotazione;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PrenotazioneRepositoryTest {

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @Autowired
    private FasciaOrariaRepository fasciaOrariaRepository;

    private LocalDate giorno;
    private FasciaOraria fascia;

    @BeforeEach
    public void setup() {
        giorno = LocalDate.now().plusDays(1);

        // Salvo una fascia oraria di test
        fascia = new FasciaOraria();
        fascia.setOraInizio(LocalTime.of(9, 0));
        fascia.setOraFine(LocalTime.of(10, 0));
        fasciaOrariaRepository.save(fascia);

        // Salvo una prenotazione associata
        Prenotazione p = new Prenotazione();
        p.setNome("Test");
        p.setEmail("test@mail.com");
        p.setCellulare("1234567890");
        p.setGiorno(giorno);
        p.setFasciaOraria(fascia);
        p.setOraInizio(fascia.getOraInizio());
        p.setOraFine(fascia.getOraFine());
        prenotazioneRepository.save(p);
    }

    @Test
    public void testFindByGiorno() {
        List<Prenotazione> result = prenotazioneRepository.findByGiorno(giorno);
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getNome());
    }

    @Test
    public void testCountByGiornoAndOraInizioAndOraFine() {
        long count = prenotazioneRepository.countByGiornoAndOraInizioAndOraFine(
            giorno,
            fascia.getOraInizio(),
            fascia.getOraFine()
        );
        assertEquals(1, count);
    }

    @Test
    public void testFindByGiornoAndOraInizioLessThanAndOraFineGreaterThan() {
        List<Prenotazione> result = prenotazioneRepository
            .findByGiornoAndOraInizioLessThanAndOraFineGreaterThan(
                giorno,
                LocalTime.of(10, 0),
                LocalTime.of(9, 0)
            );
        assertEquals(1, result.size());
    }
}
