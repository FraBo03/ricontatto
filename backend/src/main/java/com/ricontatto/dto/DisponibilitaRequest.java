package com.ricontatto.dto;

import java.time.LocalDate;

public class DisponibilitaRequest {
    private LocalDate giorno;
    private Long fasciaOrariaId;
    private Integer disponibilita;

    // Getter e Setter
    public LocalDate getGiorno() {
        return giorno;
    }
    public void setGiorno(LocalDate giorno) {
        this.giorno = giorno;
    }
    public Long getFasciaOrariaId() {
        return fasciaOrariaId;
    }
    public void setFasciaOrariaId(Long fasciaOrariaId) {
        this.fasciaOrariaId = fasciaOrariaId;
    }
    public Integer getDisponibilita() {
        return disponibilita;
    }
    public void setDisponibilita(Integer disponibilita) {
        this.disponibilita = disponibilita;
    }
}

