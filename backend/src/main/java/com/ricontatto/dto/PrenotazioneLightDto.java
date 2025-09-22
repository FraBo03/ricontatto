package com.ricontatto.dto;

import java.time.LocalDate;
import java.util.List;

public class PrenotazioneLightDto {
    private String nome;
    private String email;
    private String cellulare;
    
    private LocalDate giorno;
    private String fasciaOraria;
    private Long fasciaOrariaId;
    private List<String> motivazioniDescrizioni;
    private String note;

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getCellulare() {
        return cellulare;
    }
    public void setCellulare(String cellulare) {
        this.cellulare = cellulare;
    }
    public LocalDate getGiorno() {
        return giorno;
    }
    public void setGiorno(LocalDate giorno) {
        this.giorno = giorno;
    }
    public String getFasciaOraria() {
        return fasciaOraria;
    }
    public void setFasciaOraria(String fasciaOraria) {
        this.fasciaOraria = fasciaOraria;
    }
    public Long getFasciaOrariaId() {
        return fasciaOrariaId;
    }
    public void setFasciaOrariaId(Long fasciaOrariaId) {
        this.fasciaOrariaId = fasciaOrariaId;
    }
    public List<String> getMotivazioniDescrizioni() {
        return motivazioniDescrizioni;
    }
    public void setMotivazioniDescrizioni(List<String> motivazioniDescrizioni) {
        this.motivazioniDescrizioni = motivazioniDescrizioni;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
}