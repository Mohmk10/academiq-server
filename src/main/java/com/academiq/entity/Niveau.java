package com.academiq.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "niveaux", uniqueConstraints = @UniqueConstraint(columnNames = {"filiere_id", "niveau"}))
public class Niveau extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NiveauEnum niveau;

    @Builder.Default
    @Column(nullable = false)
    private int nombreSemestres = 2;

    private int creditsRequis;

    @ManyToOne
    @JoinColumn(name = "filiere_id", nullable = false)
    private Filiere filiere;

    @Builder.Default
    @OneToMany(mappedBy = "niveau", fetch = FetchType.EAGER)
    private List<Promotion> promotions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "niveau", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Semestre> semestres = new ArrayList<>();
}
