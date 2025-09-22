package com.ricontatto.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class PrenotazioneEsistenteDettagliataException extends PrenotazioneEsistenteException {
    private final Long prenotazioneId;
    private final LocalDate giorno;
    private final LocalTime oraInizio;
    private final LocalTime oraFine;

    public PrenotazioneEsistenteDettagliataException(String message, Long prenotazioneId, LocalDate giorno, LocalTime oraInizio, LocalTime oraFine) {
        super(message);
        this.prenotazioneId = prenotazioneId;
        this.giorno = giorno;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
    }

    public Long getPrenotazioneId() {
        return prenotazioneId;
    }

    public LocalDate getGiorno() {
        return giorno;
    }

    public LocalTime getOraInizio() {
        return oraInizio;
    }

    public LocalTime getOraFine() {
        return oraFine;
    }
}
