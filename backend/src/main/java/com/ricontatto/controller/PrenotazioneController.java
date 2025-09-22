package com.ricontatto.controller;

import com.ricontatto.dto.DisponibilitaRequest;
import com.ricontatto.dto.PrenotazioneDto;
import com.ricontatto.dto.PrenotazioneLightDto;
import com.ricontatto.model.DisponibilitaGiorno;
import com.ricontatto.model.FasciaOraria;
import com.ricontatto.model.Motivazione;
import com.ricontatto.model.Prenotazione;
import com.ricontatto.repository.DisponibilitaGiornoRepository;
import com.ricontatto.repository.FasciaOrariaRepository;
import com.ricontatto.repository.MotivazioneRepository;
import com.ricontatto.repository.PrenotazioneRepository;
import com.ricontatto.service.PrenotazioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // Cambia secondo frontend
public class PrenotazioneController {

    @Autowired
    private PrenotazioneService prenotazioneService;

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @Autowired
    private DisponibilitaGiornoRepository disponibilitaGiornoRepository;

    @Autowired
    private FasciaOrariaRepository fasciaOrariaRepository;

    @Autowired
    private MotivazioneRepository motivazioneRepository;

    @GetMapping("/motivazioni")
    public List<Motivazione> getMotivazioni() {
        return motivazioneRepository.findAll();
    }

    // 1) Recupera tutte le fasce orarie predefinite
    @GetMapping("/fasce-orarie")
    public ResponseEntity<List<FasciaOraria>> getFasceOrarie() {
        List<FasciaOraria> fasce = prenotazioneService.listaFasceOrarie();
        return ResponseEntity.ok(fasce);
    }

    @GetMapping("/fasce-orarie-default")
    public ResponseEntity<List<FasciaOraria>> getFasceOrarieDefault() {
        List<FasciaOraria> fasce = fasciaOrariaRepository.findAll().stream()
            .filter(f -> {
                // esclude fasce anomale che attraversano la mezzanotte
                if (f.getOraFine().isBefore(f.getOraInizio())) {
                    return false;
                }
                return !f.getOraInizio().isBefore(LocalTime.of(9, 0)) &&
                    !f.getOraFine().isAfter(LocalTime.of(18, 0));
            })
            .toList();

        return ResponseEntity.ok(fasce);
    }

    // 2) Recupera la disponibilità di slot per ogni fascia di un giorno specifico
    @GetMapping("/disponibilita/{giorno}")
    public ResponseEntity<Map<Long, Integer>> getDisponibilitaPerGiorno(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate giorno) {

        Map<Long, Integer> disponibilita = prenotazioneService.getDisponibilitaPerGiorno(giorno);
        return ResponseEntity.ok(disponibilita);
    }

    // 3) Imposta o aggiorna la disponibilità slot per fasce di un giorno (bulk update)
    @PostMapping("/disponibilita/{giorno}")
    public ResponseEntity<?> setDisponibilitaPerGiorno(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate giorno,
            @RequestBody List<DisponibilitaRequest> slotPerFasciaList) {

        System.out.println("Ricevuto: " + slotPerFasciaList);

        prenotazioneService.setDisponibilitaPerGiorno(giorno, slotPerFasciaList);
        return ResponseEntity.ok().build();
    }

    // 4) Crea una prenotazione se ci sono slot disponibili
    @PostMapping("/prenotazioni")
    public ResponseEntity<Prenotazione> creaPrenotazioneL(@RequestBody PrenotazioneDto dto) {
        Prenotazione prenotazione = prenotazioneService.creaPrenotazione(dto);
        return ResponseEntity.ok(prenotazione);
    }

    @PostMapping("/prenotazioni/light")
    public ResponseEntity<Prenotazione> creaPrenotazioneLight(@RequestBody PrenotazioneLightDto dto) {
        Prenotazione prenotazione = prenotazioneService.creaPrenotazioneLight(dto);
        return ResponseEntity.ok(prenotazione);
    }


    // 5) Lista tutte le prenotazioni
    @GetMapping("/prenotazioni")
public ResponseEntity<List<Prenotazione>> listaPrenotazioni(@RequestParam(required = false) String email) {
    List<Prenotazione> prenotazioni = (email != null)
        ? prenotazioneService.listaPrenotazioniByEmail(email)
        : prenotazioneService.listaPrenotazioni();

    return ResponseEntity.ok(prenotazioni);
}

    // 6) Lista fasce orarie disponibili per un giorno (usata lato frontend per selezione)
    @GetMapping("/fasce-disponibili")
    public ResponseEntity<List<FasciaOraria>> getFasceDisponibili(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate giorno) {
        List<FasciaOraria> fasceDisponibili = prenotazioneService.getFasceDisponibiliPerGiorno(giorno);
        return ResponseEntity.ok(fasceDisponibili);
    }

    @GetMapping("/fasce-disponibili/light")
public ResponseEntity<List<String>> getFasceDisponibiliLight(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate giorno) {

    List<FasciaOraria> fasceDisponibili = prenotazioneService.getFasceDisponibiliPerGiorno(giorno);

    List<String> fasceFormattate = fasceDisponibili.stream()
            .map(f -> f.getOraInizio().toString().substring(0, 5) + " - " + f.getOraFine().toString().substring(0, 5))
            .collect(Collectors.toList());

    return ResponseEntity.ok(fasceFormattate);
}

    @DeleteMapping("/prenotazioni/{id}")
    public ResponseEntity<Void> eliminaPrenotazione(@PathVariable Long id) {
        prenotazioneService.eliminaPrenotazione(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/prenotazioni/{id}")
    public ResponseEntity<?> aggiornaPrenotazione(
        @PathVariable Long id,
        @RequestBody PrenotazioneLightDto aggiornataDto) {

        Optional<Prenotazione> esistente = prenotazioneRepository.findById(id);
        if (esistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Prenotazione prenotazione = esistente.get();
        prenotazione.setNome(aggiornataDto.getNome());
        prenotazione.setEmail(aggiornataDto.getEmail());
        prenotazione.setGiorno(aggiornataDto.getGiorno());
        prenotazione.setCellulare(aggiornataDto.getCellulare());
        prenotazione.setNote(aggiornataDto.getNote());
        List<String> descrizioniRichieste = Optional.ofNullable(aggiornataDto.getMotivazioniDescrizioni())
            .orElse(Collections.emptyList());

        List<Motivazione> motivazioniTrovate = motivazioneRepository.findByDescrizioneIn(descrizioniRichieste);

        // (opzionale) Validazione: tutte le motivazioni devono essere presenti
        if (motivazioniTrovate.size() != descrizioniRichieste.size()) {
            throw new IllegalArgumentException("Alcune motivazioni non sono state trovate nel database");
        }

        prenotazione.setMotivazioni(new HashSet<>(motivazioniTrovate));

        if (aggiornataDto.getFasciaOrariaId() != null) {
            Optional<FasciaOraria> fascia = fasciaOrariaRepository.findById(aggiornataDto.getFasciaOrariaId());
            if (fascia.isEmpty()) {
                return ResponseEntity.badRequest().body("Fascia oraria non trovata");
            }
            prenotazione.setFasciaOraria(fascia.get());
        }

        prenotazioneRepository.save(prenotazione);
        return ResponseEntity.ok(prenotazione);
    }

    @GetMapping("/prenotazioni/count-giorno/{giorno}")
    public ResponseEntity<Map<Long, Long>> countPrenotazioniPerOgniFascia(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate giorno
    ) {
        List<Prenotazione> prenotazioni = prenotazioneRepository.findByGiorno(giorno);

        Map<Long, Long> counts = prenotazioni.stream()
            .filter(p -> p.getFasciaOraria() != null)
            .collect(Collectors.groupingBy(
                p -> p.getFasciaOraria().getId(),
                Collectors.counting()
            ));

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/giorni-disponibili")
    public List<LocalDate> getGiorniDisponibili() {
        return disponibilitaGiornoRepository
            .findGiorniConAlmenoUnaDisponibilita();
    }

    @GetMapping("/giorni-disponibili/light")
    public List<LocalDate> getGiorniDisponibiliFuturi() {
        LocalDate oggi = LocalDate.now();

        return disponibilitaGiornoRepository
            .findGiorniConAlmenoUnaDisponibilita()
            .stream()
            .filter(giorno -> !giorno.isBefore(oggi))
            .limit(5)
            .toList();
    }
}
