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
public class PeriodePerformanceDTO {
    private String promotionNom;
    private String anneeUniversitaire;
    private String niveauNom;
    private Double moyenneAnnuelle;
    private int creditsValides;
    private int creditsTotaux;
    private String decision;
}
