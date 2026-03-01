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
public class EnseignantInfo {

    private Long id;
    private String matricule;
    private String specialite;
    private String grade;
    private String departement;
    private String bureau;
    private LocalDate dateRecrutement;
    private String statut;
}
