package com.academiq.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodeModuleDTO {
    private String anneeUniversitaire;
    private String promotionNom;
    private Double moyenneClasse;
    private double tauxReussite;
    private int nombreInscrits;
}
