package com.academiq.dto.structure;

import jakarta.validation.constraints.NotBlank;
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
public class AffectationRequest {

    @NotNull
    private Long enseignantId;

    @NotNull
    private Long moduleFormationId;

    @NotNull
    private Long promotionId;

    @NotBlank
    private String anneeUniversitaire;
}
