package com.academiq.dto.structure;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class ModuleRequest {

    @NotBlank
    @Size(max = 20)
    private String code;

    @NotBlank
    @Size(max = 200)
    private String nom;

    private String description;

    @Min(1)
    private int credits;

    @Positive
    private double coefficient;

    private int volumeHoraireCM;
    private int volumeHoraireTD;
    private int volumeHoraireTP;

    @DecimalMin("0")
    @DecimalMax("1")
    private double ponderationCC;

    @DecimalMin("0")
    @DecimalMax("1")
    private double ponderationExamen;

    private Long enseignantId;
}
