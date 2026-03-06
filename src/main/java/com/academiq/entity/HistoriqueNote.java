package com.academiq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "historique_notes")
public class HistoriqueNote extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    private Double ancienneValeur;

    private Double nouvelleValeur;

    @Column(nullable = false)
    private boolean ancienAbsent;

    @Column(nullable = false)
    private boolean nouveauAbsent;

    @Column(length = 500)
    private String motifModification;

    @ManyToOne
    @JoinColumn(name = "modifie_par_id")
    private Utilisateur modifiePar;

    @Column(nullable = false)
    private LocalDateTime dateModification;
}
