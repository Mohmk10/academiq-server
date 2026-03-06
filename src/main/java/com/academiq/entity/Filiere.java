package com.academiq.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "filieres")
public class Filiere extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, unique = true, length = 150)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Enseignant responsable;

    @Builder.Default
    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Niveau> niveaux = new ArrayList<>();
}
