package com.academiq.dto.structure;

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
public class InscriptionResponse {

    private Long id;
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantMatricule;
    private Long promotionId;
    private String promotionNom;
    private LocalDate dateInscription;
    private String statut;
    private boolean redoublant;
}
