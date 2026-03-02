package com.academiq.dto.note;

import com.academiq.entity.TypeEvaluation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationRequest {

    @NotBlank
    private String nom;

    @NotNull
    private TypeEvaluation type;

    private LocalDate dateEvaluation;

    @Builder.Default
    @Positive
    private double noteMaximale = 20.0;

    @Builder.Default
    @Positive
    private double coefficient = 1.0;

    private String description;

    @NotNull
    private Long moduleFormationId;

    @NotNull
    private Long promotionId;
}
