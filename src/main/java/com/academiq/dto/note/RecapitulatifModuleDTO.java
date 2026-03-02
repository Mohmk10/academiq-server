package com.academiq.dto.note;

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
public class RecapitulatifModuleDTO {

    private Long moduleId;
    private String moduleNom;
    private String moduleCode;
    private List<EvaluationRecapDTO> evaluations;
    private Double moyenneClasse;
}
