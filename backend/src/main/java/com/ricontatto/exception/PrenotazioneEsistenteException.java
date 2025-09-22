package com.ricontatto.exception;

public class PrenotazioneEsistenteException extends RuntimeException {
    public PrenotazioneEsistenteException(String message) {
        super(message);
    }
}