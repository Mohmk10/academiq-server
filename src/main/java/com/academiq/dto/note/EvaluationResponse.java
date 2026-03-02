package com.academiq.dto.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResponse {

    private Long id;
    private String nom;
    private String type;
    private String statut;
    private LocalDate dateEvaluation;
    private double noteMaximale;
    private double coefficient;
    private String description;
    private Long moduleId;
    private String moduleNom;
    private Long promotionId;
    private String promotionNom;
    private long nombreNotesSaisies;
    private long nombreInscrits;
    private Double moyenne;
    private Double noteMin;
    private Double noteMax;
}
