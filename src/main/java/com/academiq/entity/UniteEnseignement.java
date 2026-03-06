package com.academiq.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "unites_enseignement", uniqueConstraints = @UniqueConstraint(columnNames = {"semestre_id", "code"}))
public class UniteEnseignement extends BaseEntity {

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private double coefficient;

    @ManyToOne
    @JoinColumn(name = "semestre_id", nullable = false)
    private Semestre semestre;

    @Builder.Default
    @OneToMany(mappedBy = "uniteEnseignement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ModuleFormation> modules = new ArrayList<>();
}
