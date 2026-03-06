package com.academiq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "etudiants")
public class Etudiant extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @Column(nullable = false, unique = true, length = 50)
    private String matricule;

    @Column(nullable = false)
    private LocalDate dateInscription;

    @Column(length = 10)
    private String niveauActuel;

    @Column(length = 100)
    private String filiereActuelle;

    @Column(length = 20)
    private String numeroTuteur;

    @Column(length = 200)
    private String nomTuteur;

    @Enumerated(EnumType.STRING)
    private StatutEtudiant statut;
}
