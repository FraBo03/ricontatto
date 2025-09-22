package com.ricontatto.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/territorio")
@CrossOrigin(origins = "http://localhost:5173")
public class TerritorioController {

    @GetMapping("/regioni")
    public List<String> getRegioni() {
        return List.copyOf(REGIONI_E_PROVINCE.keySet());
    }

    @GetMapping("/province")
    public List<String> getProvince(@RequestParam String regione) {
        return REGIONI_E_PROVINCE.getOrDefault(regione, List.of());
    }

    private static final Map<String, List<String>> REGIONI_E_PROVINCE = Map.ofEntries(
    Map.entry("Abruzzo", List.of("AQ", "CH", "PE", "TE")),
    Map.entry("Basilicata", List.of("MT", "PZ")),
    Map.entry("Calabria", List.of("CS", "CZ", "KR", "RC", "VV")),
    Map.entry("Campania", List.of("AV", "BN", "CE", "NA", "SA")),
    Map.entry("Emilia-Romagna", List.of("BO", "FE", "FC", "MO", "PC", "PR", "RA", "RE", "RN")),
    Map.entry("Friuli-Venezia Giulia", List.of("GO", "PN", "TS", "UD")),
    Map.entry("Lazio", List.of("FR", "LT", "RI", "RM", "VT")),
    Map.entry("Liguria", List.of("GE", "IM", "SP", "SV")),
    Map.entry("Lombardia", List.of("BG", "BS", "CO", "CR", "LC", "LO", "MB", "MI", "MN", "PV", "SO", "VA")),
    Map.entry("Marche", List.of("AN", "AP", "FM", "MC", "PU")),
    Map.entry("Molise", List.of("CB", "IS")),
    Map.entry("Piemonte", List.of("AL", "AT", "BI", "CN", "NO", "TO", "VB", "VC")),
    Map.entry("Puglia", List.of("BA", "BT", "BR", "FG", "LE", "TA")),
    Map.entry("Sardegna", List.of("CA", "CI", "NU", "OG", "OR", "OT", "SS", "SU")),
    Map.entry("Sicilia", List.of("AG", "CL", "CT", "EN", "ME", "PA", "RG", "SR", "TP")),
    Map.entry("Toscana", List.of("AR", "FI", "GR", "LI", "LU", "MS", "PI", "PO", "PT", "SI")),
    Map.entry("Trentino-Alto Adige", List.of("BZ", "TN")),
    Map.entry("Umbria", List.of("PG", "TR")),
    Map.entry("Valle d'Aosta", List.of("AO")),
    Map.entry("Veneto", List.of("BL", "PD", "RO", "TV", "VE", "VR", "VI"))
);
}
