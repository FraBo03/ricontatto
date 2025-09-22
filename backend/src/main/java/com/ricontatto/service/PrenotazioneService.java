package com.ricontatto.service;

import com.ricontatto.model.FasciaOraria;
import com.ricontatto.model.Prenotazione;
import com.ricontatto.dto.DisponibilitaRequest;
import com.ricontatto.dto.PrenotazioneDto;
import com.ricontatto.dto.PrenotazioneLightDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PrenotazioneService {

    List<FasciaOraria> listaFasceOrarie();

    Map<Long, Integer> getDisponibilitaPerGiorno(LocalDate giorno);

    void setDisponibilitaPerGiorno(LocalDate giorno, List<DisponibilitaRequest> disponibilitaList);

    List<FasciaOraria> getFasceDisponibiliPerGiorno(LocalDate giorno);

    boolean isSlotDisponibile(LocalDate giorno, Long fasciaOrariaId);

    List<Prenotazione> listaPrenotazioni();

    List<Prenotazione> listaPrenotazioniByEmail(String email);

    List<String> getFasceOccupatePerGiorno(LocalDate giorno);

    List<String> generaFasceOrarieDisponibili();

    Prenotazione creaPrenotazione(PrenotazioneDto dto);

    Prenotazione creaPrenotazioneLight(PrenotazioneLightDto dto);

    void eliminaPrenotazione(Long id);
}
