package com.academiq.dto.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteResponse {

    private Long id;
    private Double valeur;
    private boolean absent;
    private String commentaire;
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantMatricule;
    private Long evaluationId;
    private String evaluationNom;
    private String saisiePar;
    private LocalDateTime dateSaisie;
    private LocalDateTime dateModification;
    private Double ancienneValeur;
}
