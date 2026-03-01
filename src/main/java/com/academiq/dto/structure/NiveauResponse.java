package com.academiq.dto.structure;

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
public class NiveauResponse {

    private Long id;
    private String niveau;
    private int nombreSemestres;
    private int creditsRequis;
    private Long filiereId;
    private String filiereNom;
    private int nombrePromotions;
}
