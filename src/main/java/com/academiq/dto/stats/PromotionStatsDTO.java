package com.academiq.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionStatsDTO {
    private Long promotionId;
    private String promotionNom;
    private String anneeUniversitaire;
    private String filiereNom;
    private String niveauNom;
    private int nombreInscrits;
    private Double moyenneGenerale;
    private double tauxReussite;
    private Map<String, Integer> mentions;
    private int nombreAdmis;
    private int nombreAjournes;
    private int nombreRattrapage;
}
