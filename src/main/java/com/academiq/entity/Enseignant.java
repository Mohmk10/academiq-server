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
@Table(name = "enseignants")
public class Enseignant extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @Column(nullable = false, unique = true, length = 50)
    private String matricule;

    @Column(nullable = false, length = 150)
    private String specialite;

    @Column(length = 100)
    private String grade;

    @Column(length = 100)
    private String departement;

    private LocalDate dateRecrutement;

    @Column(length = 50)
    private String bureau;

    @Enumerated(EnumType.STRING)
    private StatutEnseignant statut;
}
