package com.academiq.dto.structure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class PromotionRequest {

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{4}")
    private String anneeUniversitaire;

    private String nom;

    @NotNull
    private LocalDate dateDebut;

    @NotNull
    private LocalDate dateFin;
}
