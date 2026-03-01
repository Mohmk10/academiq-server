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
public class ModuleResponse {

    private Long id;
    private String code;
    private String nom;
    private String description;
    private int credits;
    private double coefficient;
    private int volumeHoraireCM;
    private int volumeHoraireTD;
    private int volumeHoraireTP;
    private int volumeHoraireTotal;
    private double ponderationCC;
    private double ponderationExamen;
    private Long ueId;
    private String ueNom;
    private Long enseignantId;
    private String enseignantNom;
}
