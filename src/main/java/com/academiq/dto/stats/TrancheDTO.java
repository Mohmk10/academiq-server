package com.academiq.dto.stats;

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
public class TrancheDTO {
    private String label;
    private double borneInf;
    private double borneSup;
    private int nombre;
    private double pourcentage;
}
