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
@Table(name = "semestres", uniqueConstraints = @UniqueConstraint(columnNames = {"niveau_id", "semestre"}))
public class Semestre extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SemestreEnum semestre;

    @Column(nullable = false, length = 100)
    private String nom;

    @ManyToOne
    @JoinColumn(name = "niveau_id", nullable = false)
    private Niveau niveau;

    @Builder.Default
    @OneToMany(mappedBy = "semestre", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<UniteEnseignement> uniteEnseignements = new ArrayList<>();
}
