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
public class FiliereResponse {

    private Long id;
    private String code;
    private String nom;
    private String description;
    private boolean actif;
    private String responsableNom;
    private int nombreNiveaux;
}
