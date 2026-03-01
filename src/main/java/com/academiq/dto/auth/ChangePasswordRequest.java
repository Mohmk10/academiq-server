package com.academiq.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank
    private String ancienMotDePasse;

    @NotBlank
    @Size(min = 8)
    private String nouveauMotDePasse;

    @NotBlank
    private String confirmationMotDePasse;
}
