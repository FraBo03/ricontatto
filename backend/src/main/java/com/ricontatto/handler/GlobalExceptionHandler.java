package com.ricontatto.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ricontatto.exception.PrenotazioneEsistenteDettagliataException;
import com.ricontatto.exception.PrenotazioneEsistenteException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PrenotazioneEsistenteException.class)
    public ResponseEntity<Map<String, Object>> handlePrenotazioneEsistente(PrenotazioneEsistenteException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("errore", ex.getMessage());

        if (ex instanceof PrenotazioneEsistenteDettagliataException dettagliata) {
            error.put("id", dettagliata.getPrenotazioneId());
            error.put("giorno", dettagliata.getGiorno());
            error.put("oraInizio", dettagliata.getOraInizio().toString()); // es. "10:00"
            error.put("oraFine", dettagliata.getOraFine().toString());     // es. "11:00"
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}