package com.ricontatto.service;

import com.ricontatto.model.Utente;
import java.util.List;
import java.util.Optional;

public interface UtenteService {
    List<Utente> getAllUtenti();
    Utente salvaUtente(Utente utente);
    boolean emailGiaPresente(String email);
    Optional<Utente> getUtenteByEmail(String email);
    Utente aggiornaUtente(String email, Utente nuovoUtente);
    void eliminaPerEmail(String email);
}
