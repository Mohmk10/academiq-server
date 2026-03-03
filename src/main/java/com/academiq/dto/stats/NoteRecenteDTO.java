package com.academiq.dto.stats;

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
public class NoteRecenteDTO {
    private String evaluationNom;
    private String moduleNom;
    private String type;
    private Double valeur;
    private double noteMaximale;
    private LocalDate date;
}
