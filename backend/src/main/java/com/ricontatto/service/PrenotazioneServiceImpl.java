package com.ricontatto.service;

import com.ricontatto.model.DisponibilitaGiorno;
import com.ricontatto.model.FasciaOraria;
import com.ricontatto.model.Motivazione;
import com.ricontatto.model.Prenotazione;
import com.ricontatto.repository.DisponibilitaGiornoRepository;
import com.ricontatto.repository.FasciaOrariaRepository;
import com.ricontatto.repository.MotivazioneRepository;
import com.ricontatto.repository.PrenotazioneRepository;
import com.ricontatto.dto.PrenotazioneDto;
import com.ricontatto.dto.PrenotazioneLightDto;
import com.ricontatto.exception.PrenotazioneEsistenteDettagliataException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ricontatto.dto.DisponibilitaRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PrenotazioneServiceImpl implements PrenotazioneService {

    @Autowired
    private FasciaOrariaRepository fasciaOrariaRepository;

    @Autowired
    private DisponibilitaGiornoRepository disponibilitaRepository;

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;
    
    @Autowired
    private MotivazioneRepository motivazioneRepository;
    
    @Override
    public List<FasciaOraria> listaFasceOrarie() {
        return fasciaOrariaRepository.findAll();
    }

    @Override
    public Map<Long, Integer> getDisponibilitaPerGiorno(LocalDate giorno) {
        List<DisponibilitaGiorno> disponibilitaList = disponibilitaRepository.findAllByGiorno(giorno);
        Map<Long, Integer> disponibilitaMap = new HashMap<>();

        for (DisponibilitaGiorno d : disponibilitaList) {
            Long fasciaId = d.getFasciaOraria().getId();
            int slotTotali = d.getDisponibilita();

            LocalTime oraInizio = d.getFasciaOraria().getOraInizio();
            LocalTime oraFine = d.getFasciaOraria().getOraFine();

            long prenotazioniEffettuate = prenotazioneRepository
                .countByGiornoAndOraInizioAndOraFine(giorno, oraInizio, oraFine);

            int slotResidui = slotTotali - (int) prenotazioniEffettuate;
            disponibilitaMap.put(fasciaId, slotResidui);
        }

        return disponibilitaMap;
    }

    @Override
    public void setDisponibilitaPerGiorno(LocalDate giorno, List<DisponibilitaRequest> disponibilitaList) {
        for (DisponibilitaRequest dto : disponibilitaList) {
            FasciaOraria fascia = fasciaOrariaRepository.findById(dto.getFasciaOrariaId())
                    .orElseThrow(() -> new RuntimeException("Fascia oraria non trovata: " + dto.getFasciaOrariaId()));

            DisponibilitaGiorno disponibilita = disponibilitaRepository
                    .findByGiornoAndFasciaOraria(giorno, fascia)
                    .orElse(new DisponibilitaGiorno(giorno, fascia, 0));

            disponibilita.setDisponibilita(dto.getDisponibilita());

            disponibilitaRepository.save(disponibilita);
        }
    }
    @Override
    public List<FasciaOraria> getFasceDisponibiliPerGiorno(LocalDate giorno) {
        Map<Long, Integer> disponibilita = getDisponibilitaPerGiorno(giorno);
        // Rimuovo le fasce con slot <= 0
        return listaFasceOrarie().stream()
                .filter(f -> disponibilita.getOrDefault(f.getId(), 0) > 0)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isSlotDisponibile(LocalDate giorno, Long fasciaOrariaId) {
        Map<Long, Integer> disponibilita = getDisponibilitaPerGiorno(giorno);
        int slotDisponibili = disponibilita.getOrDefault(fasciaOrariaId, 0);

        // Recupera la fascia oraria per avere oraInizio e oraFine
        FasciaOraria fascia = fasciaOrariaRepository.findById(fasciaOrariaId)
                .orElseThrow(() -> new RuntimeException("Fascia oraria non trovata"));

        long prenotazioniCount = prenotazioneRepository.countByGiornoAndOraInizioAndOraFine(
                giorno, fascia.getOraInizio(), fascia.getOraFine());

        return (slotDisponibili - prenotazioniCount) > 0;
    }

    @Override
    public Prenotazione creaPrenotazione(PrenotazioneDto dto) {
        FasciaOraria fascia;

        if (dto.getFasciaOrariaId() == null) {
            // Usa fascia oraria di default (id 1L, cambia se vuoi)
            fascia = fasciaOrariaRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Fascia oraria di default non trovata"));
        } else {
            fascia = fasciaOrariaRepository.findById(dto.getFasciaOrariaId())
                .orElseThrow(() -> new RuntimeException("Fascia oraria non trovata"));
        }

        Optional<Prenotazione> prenotazioneEsistente = prenotazioneRepository
            .findFirstByCellulareAndGiornoGreaterThanEqual(dto.getCellulare(), LocalDate.now());

        if (prenotazioneEsistente.isPresent()) {
            Prenotazione esistente = prenotazioneEsistente.get();

            String giorno = esistente.getGiorno().toString();
            String fasciaText = esistente.getOraInizio().toString().substring(0, 5) + " - " +
                                esistente.getOraFine().toString().substring(0, 5);

            String messaggio = "Cellulare già in lista. Appuntamento il giorno " + giorno +
                            " nella fascia oraria " + fasciaText;

            throw new PrenotazioneEsistenteDettagliataException(
                messaggio,
                esistente.getId(),
                esistente.getGiorno(),
                esistente.getOraInizio(),
                esistente.getOraFine()
            );
        }

        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setNome(dto.getNome());
        prenotazione.setEmail(dto.getEmail());
        prenotazione.setGiorno(dto.getGiorno());
        prenotazione.setFasciaOraria(fascia);
        prenotazione.setCellulare(dto.getCellulare());

        // copia oraInizio e oraFine dalla fascia
        prenotazione.setOraInizio(fascia.getOraInizio());
        prenotazione.setOraFine(fascia.getOraFine());

        return prenotazioneRepository.save(prenotazione);
    }

    @Override
    public Prenotazione creaPrenotazioneLight(PrenotazioneLightDto dto) {
        // 1. Determina la fascia oraria
        FasciaOraria fasciaOraria;

        if (dto.getFasciaOrariaId() != null) {
            fasciaOraria = fasciaOrariaRepository.findById(dto.getFasciaOrariaId())
                .orElseThrow(() -> new RuntimeException("Fascia oraria non trovata"));
        } else if (dto.getFasciaOraria() != null && dto.getFasciaOraria().contains(" - ")) {
            try {
                String[] parti = dto.getFasciaOraria().split(" - ");
                String oraInizio = parti[0].trim();
                String oraFine = parti[1].trim();

                int idFascia;
                if (oraInizio.equals("23:00") && oraFine.equals("00:00")) {
                    idFascia = 24;
                } else {
                    idFascia = Integer.parseInt(oraFine.substring(0, 2));
                }

                fasciaOraria = fasciaOrariaRepository.findById((long) idFascia)
                    .orElseThrow(() -> new RuntimeException("Fascia oraria con id " + idFascia + " non trovata"));
            } catch (Exception e) {
                throw new RuntimeException("Formato fascia oraria non valido: " + dto.getFasciaOraria(), e);
            }
        } else {
            // Fascia oraria di default
            fasciaOraria = fasciaOrariaRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Fascia oraria di default non trovata"));
        }

        // 2. Verifica se già esiste una prenotazione futura per lo stesso cellulare
        Optional<Prenotazione> prenotazioneEsistente = prenotazioneRepository
            .findFirstByCellulareAndGiornoGreaterThanEqual(dto.getCellulare(), LocalDate.now());

        if (prenotazioneEsistente.isPresent()) {
            Prenotazione esistente = prenotazioneEsistente.get();

            String giorno = esistente.getGiorno().toString();
            String fasciaText = esistente.getOraInizio().toString().substring(0, 5) + " - " +
                                esistente.getOraFine().toString().substring(0, 5);

            String messaggio = "Cellulare già in lista. Appuntamento il giorno " + giorno +
                            " nella fascia oraria " + fasciaText;

            throw new PrenotazioneEsistenteDettagliataException(
                messaggio,
                esistente.getId(),
                esistente.getGiorno(),
                esistente.getOraInizio(),
                esistente.getOraFine()
            );
        }

        // 3. Crea la prenotazione
        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setNome(dto.getNome());
        prenotazione.setEmail(dto.getEmail());
        prenotazione.setGiorno(dto.getGiorno());
        prenotazione.setCellulare(dto.getCellulare());
        prenotazione.setFasciaOraria(fasciaOraria);
        prenotazione.setOraInizio(fasciaOraria.getOraInizio());
        prenotazione.setOraFine(fasciaOraria.getOraFine());
        prenotazione.setNote(dto.getNote());

        // 4. Associa motivazioni in base alla descrizione
        if (dto.getMotivazioniDescrizioni() != null && !dto.getMotivazioniDescrizioni().isEmpty()) {
            List<Motivazione> motivazioniTrovate = motivazioneRepository
                .findByDescrizioneIn(dto.getMotivazioniDescrizioni());

            if (motivazioniTrovate.size() != dto.getMotivazioniDescrizioni().size()) {
                throw new RuntimeException("Alcune descrizioni di motivazioni non sono valide");
            }

            prenotazione.setMotivazioni(new HashSet<>(motivazioniTrovate));
        }

        return prenotazioneRepository.save(prenotazione);
    }

    @Override
    public List<Prenotazione> listaPrenotazioni() {
        return prenotazioneRepository.findAll();
    }

    @Override
    public List<Prenotazione> listaPrenotazioniByEmail(String email) {
        return prenotazioneRepository.findByEmail(email);
    }

    @Override
    public void eliminaPrenotazione(Long id) {
        prenotazioneRepository.deleteById(id);
    }

    @Override
    public List<String> getFasceOccupatePerGiorno(LocalDate giorno) {
        Map<Long, Integer> disponibilita = getDisponibilitaPerGiorno(giorno);
        List<FasciaOraria> fasce = listaFasceOrarie();

        List<String> fasceOccupate = new ArrayList<>();

        for (FasciaOraria f : fasce) {
            int slotDisponibili = disponibilita.getOrDefault(f.getId(), 0);
            long prenotazioniCount = prenotazioneRepository.countByGiornoAndOraInizioAndOraFine(giorno, f.getOraInizio(), f.getOraFine());
            if (prenotazioniCount >= slotDisponibili) {
                fasceOccupate.add(f.getOraInizio() + "-" + f.getOraFine());
            }
        }
        return fasceOccupate;
    }

    @Override
    public List<String> generaFasceOrarieDisponibili() {
        List<FasciaOraria> fasce = listaFasceOrarie();
        return fasce.stream()
                .map(f -> f.getOraInizio() + "-" + f.getOraFine())
                .collect(Collectors.toList());
    }
}
