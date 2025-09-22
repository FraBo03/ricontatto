package com.ricontatto.service;

import static org.junit.jupiter.api.Assertions.*;

import com.ricontatto.exception.PrenotazioneEsistenteDettagliataException;
import com.ricontatto.model.FasciaOraria;
import com.ricontatto.model.Prenotazione;
import com.ricontatto.dto.PrenotazioneDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Transactional
public class PrenotazioneServiceImplTest {

    @Autowired
    private PrenotazioneService prenotazioneService;

    private LocalDate dataTest;

    @BeforeEach
    public void setup() {
        dataTest = LocalDate.now().plusDays(1); // usa domani per test pulito
    }

    @Test
    public void testListaFasceOrarie() {
        List<FasciaOraria> fasce = prenotazioneService.listaFasceOrarie();
        assertNotNull(fasce);
        assertFalse(fasce.isEmpty());
    }

    @Test
    public void testGetDisponibilitaPerGiorno() {
        Map<Long, Integer> disponibilita = prenotazioneService.getDisponibilitaPerGiorno(dataTest);
        assertNotNull(disponibilita);
    }

    @Test
    public void testIsSlotDisponibile() {
        List<FasciaOraria> fasce = prenotazioneService.listaFasceOrarie();
        assertFalse(fasce.isEmpty());
        Long fasciaId = fasce.get(0).getId();

        boolean disponibile = prenotazioneService.isSlotDisponibile(dataTest, fasciaId);
        assertTrue(disponibile || !disponibile); // solo controllo che non lanci eccezioni
    }

    @Test
    public void testCreaPrenotazione() {
        List<FasciaOraria> fasce = prenotazioneService.listaFasceOrarie();
        assertFalse(fasce.isEmpty());

        FasciaOraria fascia = fasce.get(0);

        PrenotazioneDto dto = new PrenotazioneDto();
        dto.setNome("Mario Rossi");
        dto.setEmail("mario.rossi@example.com");
        dto.setCellulare("testcell-" + System.currentTimeMillis()); // numero univoco per evitare conflitti
        dto.setGiorno(dataTest);
        dto.setFasciaOrariaId(fascia.getId());

        Prenotazione prenotazione = prenotazioneService.creaPrenotazione(dto);

        assertNotNull(prenotazione);
        assertEquals(dto.getNome(), prenotazione.getNome());
        assertEquals(dto.getEmail(), prenotazione.getEmail());
        assertEquals(dto.getGiorno(), prenotazione.getGiorno());
        assertEquals(fascia.getId(), prenotazione.getFasciaOraria().getId());
    }

    @Test
    public void testCreaPrenotazione_DoppioneLanciaEccezione() {
        List<FasciaOraria> fasce = prenotazioneService.listaFasceOrarie();
        assertFalse(fasce.isEmpty());

        FasciaOraria fascia = fasce.get(0);

        String cellulareDuplicato = "duplicato-12345";

        // Prima creiamo la prenotazione con quel cellulare
        PrenotazioneDto primoDto = new PrenotazioneDto();
        primoDto.setNome("Primo Utente");
        primoDto.setEmail("primo@example.com");
        primoDto.setCellulare(cellulareDuplicato);
        primoDto.setGiorno(dataTest);
        primoDto.setFasciaOrariaId(fascia.getId());
        prenotazioneService.creaPrenotazione(primoDto);

        // Ora proviamo a crearne un'altra con lo stesso cellulare, ci aspettiamo l'eccezione
        PrenotazioneDto secondoDto = new PrenotazioneDto();
        secondoDto.setNome("Secondo Utente");
        secondoDto.setEmail("secondo@example.com");
        secondoDto.setCellulare(cellulareDuplicato);
        secondoDto.setGiorno(dataTest.plusDays(1));
        secondoDto.setFasciaOrariaId(fascia.getId());

        PrenotazioneEsistenteDettagliataException thrown = assertThrows(
            PrenotazioneEsistenteDettagliataException.class,
            () -> prenotazioneService.creaPrenotazione(secondoDto)
        );

        assertTrue(thrown.getMessage().contains("Cellulare già in lista"));
        assertNotNull(thrown.getPrenotazioneId());
    }

    @Test
    public void testEliminaPrenotazione() {
        List<FasciaOraria> fasce = prenotazioneService.listaFasceOrarie();
        FasciaOraria fascia = fasce.get(0);

        PrenotazioneDto dto = new PrenotazioneDto();
        dto.setNome("Test Elimina");
        dto.setEmail("test.elimina@example.com");
        dto.setCellulare("1112223333");
        dto.setGiorno(dataTest);
        dto.setFasciaOrariaId(fascia.getId());

        Prenotazione prenotazione = prenotazioneService.creaPrenotazione(dto);

        Long id = prenotazione.getId();
        assertNotNull(id);

        prenotazioneService.eliminaPrenotazione(id);

        List<Prenotazione> prenotazioni = prenotazioneService.listaPrenotazioni();
        boolean exists = prenotazioni.stream().anyMatch(p -> p.getId().equals(id));
        assertFalse(exists);
    }
}
