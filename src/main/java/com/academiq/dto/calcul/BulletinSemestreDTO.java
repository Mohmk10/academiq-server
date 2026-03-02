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
public class BulletinSemestreDTO {

    private String semestreNom;
    private List<BulletinUeDTO> ues;
    private Double moyenneSemestre;
    private int creditsValidesSemestre;
}
