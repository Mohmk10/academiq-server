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
public class TauxReussiteDTO {
    private String contexte;
    private int totalInscrits;
    private int totalReussis;
    private int totalEchecs;
    private double tauxReussite;
    private Double moyenneGenerale;
}
