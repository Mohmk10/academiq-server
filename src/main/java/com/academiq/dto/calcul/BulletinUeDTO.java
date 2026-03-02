package com.academiq.dto.calcul;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulletinUeDTO {

    private String ueNom;
    private String ueCode;
    private int credits;
    private double coefficient;
    private List<BulletinModuleDTO> modules;
    private Double moyenneUE;
    private boolean validee;
}
