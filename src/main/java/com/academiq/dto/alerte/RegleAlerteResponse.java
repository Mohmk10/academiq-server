package com.academiq.dto.alerte;

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
public class RegleAlerteResponse {
    private Long id;
    private String nom;
    private String description;
    private String type;
    private String niveauAlerte;
    private double seuil;
    private Double seuilCritique;
    private Integer nombreMaxAbsences;
    private Double pourcentageBaisse;
    private boolean actif;
}
