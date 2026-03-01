package com.academiq.dto.structure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {

    private Long id;
    private String anneeUniversitaire;
    private String nom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean actif;
    private Long niveauId;
    private String niveauNom;
    private String filiereNom;
    private long nombreInscrits;
}
