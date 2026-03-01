package com.academiq.dto.utilisateur;

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
public class EtudiantInfo {

    private Long id;
    private String matricule;
    private String niveauActuel;
    private String filiereActuelle;
    private String numeroTuteur;
    private String nomTuteur;
    private LocalDate dateInscription;
    private String statut;
}
