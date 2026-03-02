package com.academiq.dto.alerte;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TraiterAlerteRequest {

    @NotBlank(message = "Le commentaire est obligatoire")
    private String commentaire;
}
