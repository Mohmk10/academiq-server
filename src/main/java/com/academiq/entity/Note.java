package com.academiq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Entity
@Table(name = "notes", uniqueConstraints = @UniqueConstraint(columnNames = {"etudiant_id", "evaluation_id"}))
public class Note extends BaseEntity {

    private Double valeur;

    @Builder.Default
    @Column(nullable = false)
    private boolean absent = false;

    @Column(length = 500)
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private Evaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saisie_par_id")
    private Utilisateur saisiePar;

    private LocalDateTime dateSaisie;

    private LocalDateTime dateModification;

    private Double ancienneValeur;
}
