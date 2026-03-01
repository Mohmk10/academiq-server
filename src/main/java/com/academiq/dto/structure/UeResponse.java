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
public class UeResponse {

    private Long id;
    private String code;
    private String nom;
    private String description;
    private int credits;
    private double coefficient;
    private Long semestreId;
    private String semestreNom;
    private int nombreModules;
}
