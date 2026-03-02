package com.academiq.dto.note;

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
public class EvaluationRecapDTO {

    private Long evaluationId;
    private String evaluationNom;
    private String type;
    private double coefficient;
    private double noteMaximale;
    private Double moyenne;
    private Double noteMin;
    private Double noteMax;
    private long nombreNotes;
    private long nombreAbsents;
}
