package com.academiq.dto.calcul;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulletinEtudiantDTO {

    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantMatricule;
    private String promotionNom;
    private String anneeUniversitaire;
    private String filiereNom;
    private String niveauNom;
    private List<BulletinSemestreDTO> semestres;
    private Double moyenneAnnuelle;
    private int creditsValides;
    private int creditsTotaux;
    private String decision;
    private String mention;
}
