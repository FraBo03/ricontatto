package com.ricontatto.repository;

import com.ricontatto.model.DisponibilitaGiorno;
import com.ricontatto.model.FasciaOraria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class DisponibilitaGiornoRepositoryTest {

    @Autowired
    private DisponibilitaGiornoRepository disponibilitaGiornoRepository;

    @Autowired
    private FasciaOrariaRepository fasciaOrariaRepository;

    @Test
    void testFindAllByGiorno() {
        LocalDate oggi = LocalDate.now();

        List<DisponibilitaGiorno> list = disponibilitaGiornoRepository.findAllByGiorno(oggi);

        // Assert qualcosa (esempio: lista vuota o non vuota, a seconda dati di test)
        assertThat(list).isNotNull();
    }

    @Test
    void testFindByGiornoAndFasciaOraria() {
        LocalDate oggi = LocalDate.now();

        // Creo e salvo una fascia oraria di test
        FasciaOraria fascia = new FasciaOraria();
        fascia.setOraInizio(LocalTime.of(9, 0));
        fascia.setOraFine(LocalTime.of(10, 0));
        fasciaOrariaRepository.save(fascia);

        // Creo e salvo una disponibilità legata a quella fascia
        DisponibilitaGiorno disponibilita = new DisponibilitaGiorno();
        disponibilita.setGiorno(oggi);
        disponibilita.setFasciaOraria(fascia);
        disponibilita.setDisponibilita(5);
        disponibilitaGiornoRepository.save(disponibilita);

        Optional<DisponibilitaGiorno> opt = disponibilitaGiornoRepository.findByGiornoAndFasciaOraria(oggi, fascia);

        assertThat(opt).isPresent();
    }

    @Test
    void testFindGiorniConAlmenoUnaDisponibilita() {
        List<LocalDate> giorni = disponibilitaGiornoRepository.findGiorniConAlmenoUnaDisponibilita();

        assertThat(giorni).isNotNull();
        // Puoi aggiungere altri assert a seconda dei dati nel DB di test
    }
}
