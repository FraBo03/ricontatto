package com.ricontatto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "fascia_oraria")
public class FasciaOraria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime oraInizio;
    private LocalTime oraFine;

    @Column(name = "slot_max")
    private Integer slotMax;

    @JsonIgnore
    @OneToMany(mappedBy = "fasciaOraria", cascade = CascadeType.ALL)
    private List<Prenotazione> prenotazioni;

    // Costruttori
    public FasciaOraria() {}

    public FasciaOraria(LocalTime oraInizio, LocalTime oraFine) {
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
    }

    // Getter e Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalTime getOraInizio() {
        return oraInizio;
    }

    public void setOraInizio(LocalTime oraInizio) {
        this.oraInizio = oraInizio;
    }

    public LocalTime getOraFine() {
        return oraFine;
    }

    public void setOraFine(LocalTime oraFine) {
        this.oraFine = oraFine;
    }

    public List<Prenotazione> getPrenotazioni() {
        return prenotazioni;
    }

    public void setPrenotazioni(List<Prenotazione> prenotazioni) {
        this.prenotazioni = prenotazioni;
    }

    public Integer getSlotMax() {
        return slotMax;
    }

    public void setSlotMax(Integer slotMax) {
        this.slotMax = slotMax;
    }

    
}
