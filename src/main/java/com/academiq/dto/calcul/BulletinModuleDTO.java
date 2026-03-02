package com.academiq.dto.calcul;

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
public class BulletinModuleDTO {

    private String moduleNom;
    private String moduleCode;
    private double coefficient;
    private int credits;
    private Double moyenneCC;
    private Double noteExamen;
    private Double moyenneModule;
    private String enseignantNom;
}
