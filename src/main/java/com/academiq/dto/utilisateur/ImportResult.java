package com.academiq.dto.utilisateur;

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
public class ImportResult {

    private int totalLignes;
    private int importes;
    private int echecs;

    @Builder.Default
    private List<String> erreurs = new ArrayList<>();

    public void addErreur(String erreur) {
        erreurs.add(erreur);
    }
}
