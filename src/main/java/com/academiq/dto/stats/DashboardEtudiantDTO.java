package com.academiq.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardEtudiantDTO {
    private String etudiantNom;
    private String etudiantMatricule;
    private String promotionNom;
    private String filiereNom;
    private String niveauNom;
    private Double moyenneActuelle;
    private int creditsValides;
    private int creditsTotaux;
    private int rang;
    private int totalEtudiants;
    private int nombreAlertes;
    private List<String> alertes;
    private List<NoteRecenteDTO> notesRecentes;
    private Map<String, Double> moyennesParModule;
}
