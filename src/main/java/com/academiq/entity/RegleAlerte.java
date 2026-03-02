package com.academiq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "regles_alerte")
public class RegleAlerte extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAlerte type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NiveauAlerte niveauAlerte;

    @Column(nullable = false)
    private double seuil;

    private Double seuilCritique;

    private Integer nombreMaxAbsences;

    private Double pourcentageBaisse;

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;
}
