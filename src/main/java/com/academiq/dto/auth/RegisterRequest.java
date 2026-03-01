package com.academiq.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String nom;

    @NotBlank
    @Size(min = 2, max = 100)
    private String prenom;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String motDePasse;

    private String telephone;

    private LocalDate dateNaissance;

    private String adresse;
}
