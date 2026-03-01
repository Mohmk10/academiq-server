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
public class SemestreResponse {

    private Long id;
    private String semestre;
    private String nom;
    private Long niveauId;
    private int nombreUes;
}
