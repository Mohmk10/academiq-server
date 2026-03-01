package com.academiq.dto.structure;

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
public class InscriptionRequest {

    @NotNull
    private Long etudiantId;

    @NotNull
    private Long promotionId;

    private boolean redoublant;
}
