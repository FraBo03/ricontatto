package com.ricontatto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricontatto.dto.PrenotazioneDto;
import com.ricontatto.model.FasciaOraria;
import com.ricontatto.model.Prenotazione;
import com.ricontatto.repository.DisponibilitaGiornoRepository;
import com.ricontatto.repository.FasciaOrariaRepository;
import com.ricontatto.repository.PrenotazioneRepository;
import com.ricontatto.service.PrenotazioneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrenotazioneController.class)
class PrenotazioneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrenotazioneService prenotazioneService;

    @MockBean
    private PrenotazioneRepository prenotazioneRepository;

    @MockBean
    private DisponibilitaGiornoRepository disponibilitaGiornoRepository;

    @MockBean
    private FasciaOrariaRepository fasciaOrariaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Test 1: POST /api/prenotazioni
    @Test
    void creaPrenotazione_DeveRestituire200EJson() throws Exception {
        PrenotazioneDto dto = new PrenotazioneDto();
        dto.setNome("Mario");
        dto.setEmail("mario@example.com");
        dto.setCellulare("3331234567");
        dto.setGiorno(LocalDate.of(2025, 7, 2));
        dto.setFasciaOrariaId(1L);

        Prenotazione mockPrenotazione = new Prenotazione();
        mockPrenotazione.setId(10L);
        mockPrenotazione.setNome("Mario");

        when(prenotazioneService.creaPrenotazione(any(PrenotazioneDto.class)))
                .thenReturn(mockPrenotazione);

        mockMvc.perform(post("/api/prenotazioni")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Mario"));
    }

    // Test 2: DELETE /api/prenotazioni/{id}
    @Test
    void eliminaPrenotazione_DeveRestituire204() throws Exception {
        doNothing().when(prenotazioneService).eliminaPrenotazione(10L);

        mockMvc.perform(delete("/api/prenotazioni/10"))
                .andExpect(status().isNoContent());
    }

    // Test 3: GET /api/fasce-disponibili?giorno=...
    @Test
    void getFasceDisponibili_DeveRestituire200() throws Exception {
        FasciaOraria f = new FasciaOraria();
        f.setId(1L);
        f.setOraInizio(LocalTime.of(9, 0));
        f.setOraFine(LocalTime.of(10, 0));

        when(prenotazioneService.getFasceDisponibiliPerGiorno(LocalDate.of(2025, 7, 2)))
                .thenReturn(java.util.List.of(f));

        mockMvc.perform(get("/api/fasce-disponibili")
                .param("giorno", "2025-07-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].oraInizio").value("09:00:00"));
    }

    // Test PUT /api/prenotazioni/{id}
    @Test
    void aggiornaPrenotazione_Successo() throws Exception {
        Long id = 1L;

        PrenotazioneDto dto = new PrenotazioneDto();
        dto.setNome("Luca");
        dto.setEmail("luca@example.com");
        dto.setCellulare("3339876543");
        dto.setGiorno(LocalDate.of(2025, 7, 3));
        dto.setFasciaOrariaId(2L);

        // Mock prenotazione esistente
        Prenotazione esistente = new Prenotazione();
        esistente.setId(id);
        esistente.setNome("Mario");
        esistente.setEmail("mario@old.com");

        // Mock fascia oraria
        FasciaOraria fascia = new FasciaOraria();
        fascia.setId(2L);
        fascia.setOraInizio(LocalTime.of(10, 0));
        fascia.setOraFine(LocalTime.of(11, 0));

        when(prenotazioneRepository.findById(id)).thenReturn(Optional.of(esistente));
        when(fasciaOrariaRepository.findById(2L)).thenReturn(Optional.of(fascia));
        when(prenotazioneRepository.save(any(Prenotazione.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/api/prenotazioni/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Luca"))
                .andExpect(jsonPath("$.email").value("luca@example.com"))
                .andExpect(jsonPath("$.fasciaOraria.id").value(2));
    }

    @Test
    void aggiornaPrenotazione_FasciaNonTrovata_BadRequest() throws Exception {
        Long id = 1L;

        PrenotazioneDto dto = new PrenotazioneDto();
        dto.setFasciaOrariaId(999L);

        Prenotazione esistente = new Prenotazione();
        esistente.setId(id);

        when(prenotazioneRepository.findById(id)).thenReturn(Optional.of(esistente));
        when(fasciaOrariaRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/prenotazioni/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Fascia oraria non trovata"));
    }

    @Test
    void aggiornaPrenotazione_NotFound() throws Exception {
        Long id = 1L;

        PrenotazioneDto dto = new PrenotazioneDto();
        dto.setNome("Luca");

        when(prenotazioneRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/prenotazioni/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // Test GET /api/prenotazioni/count-giorno/{giorno}
    @Test
    void countPrenotazioniPerOgniFascia_ReturnsMap() throws Exception {
        LocalDate giorno = LocalDate.of(2025, 7, 4);

        FasciaOraria fascia1 = new FasciaOraria();
        fascia1.setId(1L);
        FasciaOraria fascia2 = new FasciaOraria();
        fascia2.setId(2L);

        Prenotazione p1 = new Prenotazione();
        p1.setFasciaOraria(fascia1);

        Prenotazione p2 = new Prenotazione();
        p2.setFasciaOraria(fascia1);

        Prenotazione p3 = new Prenotazione();
        p3.setFasciaOraria(fascia2);

        when(prenotazioneRepository.findByGiorno(giorno)).thenReturn(java.util.List.of(p1, p2, p3));

        mockMvc.perform(get("/api/prenotazioni/count-giorno/{giorno}", giorno.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['1']").value(2))
                .andExpect(jsonPath("$.['2']").value(1));
    }
}
