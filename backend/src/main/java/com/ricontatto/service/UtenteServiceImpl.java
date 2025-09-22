package com.ricontatto.service;

import com.ricontatto.model.Utente;
import com.ricontatto.repository.UtenteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UtenteServiceImpl implements UtenteService {

    private final UtenteRepository utenteRepository;

    public UtenteServiceImpl(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    @Override
    public List<Utente> getAllUtenti() {
        return utenteRepository.findAll();
    }

    @Override
    public Utente salvaUtente(Utente utente) {
        return utenteRepository.save(utente);
    }

    @Override
    public boolean emailGiaPresente(String email) {
        return utenteRepository.existsByEmail(email);
    }

    @Override
    public Optional<Utente> getUtenteByEmail(String email) {
        return utenteRepository.findById(email);
    }

    @Override
    public Utente aggiornaUtente(String email, Utente nuovoUtente) {
        return utenteRepository.findById(email)
            .map(utenteEsistente -> {
                utenteEsistente.setNome(nuovoUtente.getNome());
                utenteEsistente.setCognome(nuovoUtente.getCognome());
                utenteEsistente.setRegione(nuovoUtente.getRegione());
                utenteEsistente.setProvincia(nuovoUtente.getProvincia());
                utenteEsistente.setCitta(nuovoUtente.getCitta());
                utenteEsistente.setIndirizzo(nuovoUtente.getIndirizzo());
                utenteEsistente.setCap(nuovoUtente.getCap());
                return utenteRepository.save(utenteEsistente);
            })
            .orElseThrow(() -> new RuntimeException("Utente non trovato con email: " + email));
    }

    @Override
    public void eliminaPerEmail(String email) {
        utenteRepository.deleteById(email);
    }
}
