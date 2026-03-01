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
public class AffectationResponse {

    private Long id;
    private Long enseignantId;
    private String enseignantNom;
    private Long moduleId;
    private String moduleNom;
    private Long promotionId;
    private String promotionNom;
    private String anneeUniversitaire;
    private boolean actif;
}
