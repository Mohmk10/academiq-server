package com.academiq.dto.alerte;

import com.academiq.entity.NiveauAlerte;
import com.academiq.entity.TypeAlerte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegleAlerteRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String description;

    @NotNull(message = "Le type est obligatoire")
    private TypeAlerte type;

    @NotNull(message = "Le niveau d'alerte est obligatoire")
    private NiveauAlerte niveauAlerte;

    private double seuil;

    private Double seuilCritique;

    private Integer nombreMaxAbsences;

    private Double pourcentageBaisse;
}
