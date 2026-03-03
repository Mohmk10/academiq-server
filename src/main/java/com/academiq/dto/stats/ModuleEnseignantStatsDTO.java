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
public class ModuleEnseignantStatsDTO {
    private Long moduleId;
    private String moduleNom;
    private String moduleCode;
    private String promotionNom;
    private int nombreInscrits;
    private int nombreNotesSaisies;
    private Double moyenneClasse;
    private double tauxReussite;
    private int nombreAlertes;
}
