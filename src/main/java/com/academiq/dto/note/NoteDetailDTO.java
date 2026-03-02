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
public class NoteDetailDTO {

    private Long noteId;
    private String evaluationNom;
    private String type;
    private Double valeur;
    private boolean absent;
    private double noteMaximale;
    private double coefficient;
}
