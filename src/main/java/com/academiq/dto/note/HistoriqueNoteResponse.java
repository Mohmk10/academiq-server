package com.academiq.dto.note;

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
public class HistoriqueNoteResponse {

    private Long id;
    private Double ancienneValeur;
    private Double nouvelleValeur;
    private boolean ancienAbsent;
    private boolean nouveauAbsent;
    private String motifModification;
    private String modifieParNom;
    private LocalDateTime dateModification;
}
