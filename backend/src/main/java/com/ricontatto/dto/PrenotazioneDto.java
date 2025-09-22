// PrenotazioneDto.java
package com.ricontatto.dto;

import java.time.LocalDate;

public class PrenotazioneDto {
    private String nome;
    private LocalDate giorno;
    private Long fasciaOrariaId;
    private String email;
    private String cellulare;

    // Getter e Setter
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

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
}
