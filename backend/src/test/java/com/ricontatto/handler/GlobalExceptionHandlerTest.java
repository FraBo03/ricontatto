package com.ricontatto.handler;

import com.ricontatto.controller.PrenotazioneController;
import com.ricontatto.exception.PrenotazioneEsistenteDettagliataException;
import com.ricontatto.repository.DisponibilitaGiornoRepository;
import com.ricontatto.repository.FasciaOrariaRepository;
import com.ricontatto.repository.PrenotazioneRepository;
import com.ricontatto.service.PrenotazioneService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PrenotazioneController.class)
public class GlobalExceptionHandlerTest {

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

    @Test
    void testPrenotazioneEsistenteDettagliataException() throws Exception {
        Mockito.when(prenotazioneService.creaPrenotazione(any()))
                .thenThrow(new PrenotazioneEsistenteDettagliataException(
                        "Prenotazione già esistente",
                        123L,
                        LocalDate.of(2025, 7, 15),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0)
                ));

        String payload = """
            {
              "nome": "Mario",
              "email": "mario@example.com",
              "cellulare": "3331234567",
              "giorno": "2025-07-15",
              "fasciaOrariaId": 1
            }
        """;

        mockMvc.perform(post("/api/prenotazioni")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errore").value("Prenotazione già esistente"))
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.giorno").value("2025-07-15"))
                .andExpect(jsonPath("$.oraInizio").value("10:00"))
                .andExpect(jsonPath("$.oraFine").value("11:00"));
    }
}
