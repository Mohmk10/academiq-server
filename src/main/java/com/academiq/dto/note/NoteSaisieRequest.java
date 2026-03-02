package com.academiq.dto.note;

import jakarta.validation.constraints.NotNull;
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
public class NoteSaisieRequest {

    @NotNull
    private Long evaluationId;

    @NotNull
    private Long etudiantId;

    private Double valeur;
    private boolean absent;
    private String commentaire;
}
