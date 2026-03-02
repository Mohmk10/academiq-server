package com.academiq.dto.note;

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
public class RecapitulatifEtudiantDTO {

    private Long etudiantId;
    private String etudiantNom;
    private String etudiantMatricule;
    private String promotionNom;
    private List<ModuleNotesDTO> modules;
}
