package com.ricontatto.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "disponibilita_giorno", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"giorno", "fascia_oraria_id"})
})
public class DisponibilitaGiorno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate giorno;

    @ManyToOne
    @JoinColumn(name = "fascia_oraria_id", nullable = false)
    private FasciaOraria fasciaOraria;

    private Integer disponibilita; // es: 3 utenti prenotabili in quella fascia

    public DisponibilitaGiorno() {}

    public DisponibilitaGiorno(LocalDate giorno, FasciaOraria fasciaOraria, Integer disponibilita) {
        this.giorno = giorno;
        this.fasciaOraria = fasciaOraria;
        this.disponibilita = disponibilita;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getGiorno() {
        return giorno;
    }

    public void setGiorno(LocalDate giorno) {
        this.giorno = giorno;
    }

    public FasciaOraria getFasciaOraria() {
        return fasciaOraria;
    }

    public void setFasciaOraria(FasciaOraria fasciaOraria) {
        this.fasciaOraria = fasciaOraria;
    }

    public Integer getDisponibilita() {
        return disponibilita;
    }

    public void setDisponibilita(Integer disponibilita) {
        this.disponibilita = disponibilita;
    }
}
