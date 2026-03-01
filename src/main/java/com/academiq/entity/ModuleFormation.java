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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "modules_formation", uniqueConstraints = @UniqueConstraint(columnNames = {"unite_enseignement_id", "code"}))
public class ModuleFormation extends BaseEntity {

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

    private int volumeHoraireCM;

    private int volumeHoraireTD;

    private int volumeHoraireTP;

    @Column(nullable = false)
    private double ponderationCC;

    @Column(nullable = false)
    private double ponderationExamen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unite_enseignement_id", nullable = false)
    private UniteEnseignement uniteEnseignement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;

    public int getVolumeHoraireTotal() {
        return volumeHoraireCM + volumeHoraireTD + volumeHoraireTP;
    }
}
