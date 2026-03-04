package com.academiq.mapper;

import com.academiq.dto.alerte.AlerteResponse;
import com.academiq.dto.alerte.RegleAlerteRequest;
import com.academiq.dto.alerte.RegleAlerteResponse;
import com.academiq.dto.alerte.StatistiquesAlertesDTO;
import com.academiq.entity.Alerte;
import com.academiq.entity.RegleAlerte;
import com.academiq.entity.Utilisateur;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AlerteMapper {

    public AlerteResponse toAlerteResponse(Alerte alerte) {
        var etudiant = alerte.getEtudiant();
        Utilisateur etudiantUser = etudiant != null ? etudiant.getUtilisateur() : null;

        AlerteResponse.AlerteResponseBuilder builder = AlerteResponse.builder()
                .id(alerte.getId())
                .type(alerte.getType() != null ? alerte.getType().name() : null)
                .niveau(alerte.getNiveau() != null ? alerte.getNiveau().name() : null)
                .statut(alerte.getStatut() != null ? alerte.getStatut().name() : null)
                .titre(alerte.getTitre())
                .message(alerte.getMessage())
                .valeurDetectee(alerte.getValeurDetectee())
                .seuilAlerte(alerte.getSeuilAlerte())
                .etudiantId(etudiant != null ? etudiant.getId() : null)
                .etudiantNom(etudiantUser != null ? etudiantUser.getPrenom() + " " + etudiantUser.getNom() : null)
                .etudiantMatricule(etudiant != null ? etudiant.getMatricule() : null)
                .createdAt(alerte.getCreatedAt());

        if (alerte.getPromotion() != null) {
            builder.promotionId(alerte.getPromotion().getId())
                    .promotionNom(alerte.getPromotion().getNom());
        }

        if (alerte.getModuleFormation() != null) {
            builder.moduleId(alerte.getModuleFormation().getId())
                    .moduleNom(alerte.getModuleFormation().getNom());
        }

        if (alerte.getTraiteePar() != null) {
            builder.traiteParNom(alerte.getTraiteePar().getPrenom() + " " + alerte.getTraiteePar().getNom());
        }

        builder.dateTraitement(alerte.getDateTraitement())
                .commentaireTraitement(alerte.getCommentaireTraitement());

        return builder.build();
    }

    public List<AlerteResponse> toAlerteResponseList(List<Alerte> alertes) {
        return alertes.stream().map(this::toAlerteResponse).toList();
    }

    public RegleAlerteResponse toRegleAlerteResponse(RegleAlerte regle) {
        return RegleAlerteResponse.builder()
                .id(regle.getId())
                .nom(regle.getNom())
                .description(regle.getDescription())
                .type(regle.getType().name())
                .niveauAlerte(regle.getNiveauAlerte().name())
                .seuil(regle.getSeuil())
                .seuilCritique(regle.getSeuilCritique())
                .nombreMaxAbsences(regle.getNombreMaxAbsences())
                .pourcentageBaisse(regle.getPourcentageBaisse())
                .actif(regle.isActif())
                .build();
    }

    public List<RegleAlerteResponse> toRegleAlerteResponseList(List<RegleAlerte> regles) {
        return regles.stream().map(this::toRegleAlerteResponse).toList();
    }

    public RegleAlerte toRegleAlerte(RegleAlerteRequest request) {
        return RegleAlerte.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .type(request.getType())
                .niveauAlerte(request.getNiveauAlerte())
                .seuil(request.getSeuil())
                .seuilCritique(request.getSeuilCritique())
                .nombreMaxAbsences(request.getNombreMaxAbsences())
                .pourcentageBaisse(request.getPourcentageBaisse())
                .build();
    }

    public StatistiquesAlertesDTO toStatistiquesDTO(Map<String, Long> stats) {
        return StatistiquesAlertesDTO.builder()
                .totalActives(stats.getOrDefault("totalActives", 0L))
                .totalCritiques(stats.getOrDefault("totalCritiques", 0L))
                .totalAttention(stats.getOrDefault("totalAttention", 0L))
                .totalTraitees(stats.getOrDefault("totalTraitees", 0L))
                .totalResolues(stats.getOrDefault("totalResolues", 0L))
                .build();
    }
}
