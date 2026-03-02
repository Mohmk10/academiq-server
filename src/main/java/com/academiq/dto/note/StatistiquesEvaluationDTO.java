package com.academiq.dto.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatistiquesEvaluationDTO {

    private Long evaluationId;
    private String evaluationNom;
    private String type;
    private Double moyenne;
    private Double mediane;
    private Double ecartType;
    private Double noteMin;
    private Double noteMax;
    private long nombreNotes;
    private long nombreAbsents;
    private long nombreInscrits;
    private Double tauxReussite;
    private Map<String, Long> distribution;
}
