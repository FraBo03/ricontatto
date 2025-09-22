package com.ricontatto.controller;

import com.ricontatto.model.Utente;
import com.ricontatto.service.UtenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/utenti")
@CrossOrigin(origins = "http://localhost:5173")
public class UtenteController {

    private final UtenteService utenteService;

    public UtenteController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @GetMapping
    public List<Utente> getAllUtenti() {
        return utenteService.getAllUtenti();
    }

    @PostMapping
    public ResponseEntity<Utente> creaUtente(@RequestBody Utente utente) {
        Utente salvato = utenteService.salvaUtente(utente);
        return ResponseEntity.ok(salvato);
    }

    @GetMapping("/email-gia-presente")
    public ResponseEntity<Boolean> emailGiaPresente(@RequestParam String email) {
        boolean presente = utenteService.emailGiaPresente(email);
        return ResponseEntity.ok(presente);
    }

    @GetMapping("/{email}")
    public ResponseEntity<Utente> getUtenteByEmail(@PathVariable String email) {
        return utenteService.getUtenteByEmail(email)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{email}")
    public ResponseEntity<Utente> aggiornaUtente(@PathVariable String email, @RequestBody Utente utente) {
        try {
            Utente aggiornato = utenteService.aggiornaUtente(email, utente);
            return ResponseEntity.ok(aggiornato);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> eliminaUtente(@PathVariable String email) {
        utenteService.eliminaPerEmail(email); 
        return ResponseEntity.noContent().build();
    }
}
