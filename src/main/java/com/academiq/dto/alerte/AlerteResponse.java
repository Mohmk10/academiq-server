package com.academiq.dto.alerte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlerteResponse {
    private Long id;
    private String type;
    private String niveau;
    private String statut;
    private String titre;
    private String message;
    private Double valeurDetectee;
    private Double seuilAlerte;
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantMatricule;
    private Long promotionId;
    private String promotionNom;
    private Long moduleId;
    private String moduleNom;
    private String traiteParNom;
    private LocalDateTime dateTraitement;
    private String commentaireTraitement;
    private LocalDateTime createdAt;
}
