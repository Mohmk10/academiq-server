package com.academiq.service;

import com.academiq.entity.NiveauAlerte;
import com.academiq.entity.RegleAlerte;
import com.academiq.entity.TypeAlerte;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.RegleAlerteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegleAlerteService {

    private static final Logger log = LoggerFactory.getLogger(RegleAlerteService.class);

    private final RegleAlerteRepository regleAlerteRepository;

    @Transactional(readOnly = true)
    public List<RegleAlerte> getAllRegles() {
        return regleAlerteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RegleAlerte> getReglesActives() {
        return regleAlerteRepository.findByActifTrue();
    }

    @Transactional(readOnly = true)
    public List<RegleAlerte> getReglesByType(TypeAlerte type) {
        return regleAlerteRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public RegleAlerte getRegleById(Long id) {
        return regleAlerteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Règle d'alerte", "id", id));
    }

    @Transactional
    public RegleAlerte creerRegle(RegleAlerte regle) {
        return regleAlerteRepository.save(regle);
    }

    @Transactional
    public RegleAlerte modifierRegle(Long id, RegleAlerte data) {
        RegleAlerte regle = getRegleById(id);
        regle.setNom(data.getNom());
        regle.setDescription(data.getDescription());
        regle.setType(data.getType());
        regle.setNiveauAlerte(data.getNiveauAlerte());
        regle.setSeuil(data.getSeuil());
        regle.setSeuilCritique(data.getSeuilCritique());
        regle.setNombreMaxAbsences(data.getNombreMaxAbsences());
        regle.setPourcentageBaisse(data.getPourcentageBaisse());
        return regleAlerteRepository.save(regle);
    }

    @Transactional
    public RegleAlerte toggleRegle(Long id) {
        RegleAlerte regle = getRegleById(id);
        regle.setActif(!regle.isActif());
        return regleAlerteRepository.save(regle);
    }

    @Transactional
    public void deleteRegle(Long id) {
        RegleAlerte regle = getRegleById(id);
        regleAlerteRepository.delete(regle);
        log.info("Règle d'alerte {} supprimée", id);
    }

    @Transactional
    public void initialiserReglesParDefaut() {
        creerSiAbsent("Moyenne module faible",
                "Alerte lorsque la moyenne d'un module est inférieure à 10/20",
                TypeAlerte.MOYENNE_FAIBLE, NiveauAlerte.ATTENTION, 10.0,
                null, null, null);

        creerSiAbsent("Moyenne module critique",
                "Alerte lorsque la moyenne d'un module est inférieure à 8/20",
                TypeAlerte.MOYENNE_FAIBLE, NiveauAlerte.CRITIQUE, 8.0,
                null, null, null);

        creerSiAbsent("Absences répétées",
                "Alerte lorsqu'un étudiant cumule trop d'absences sur un module",
                TypeAlerte.ABSENCES_REPETEES, NiveauAlerte.ATTENTION, 0,
                null, 3, null);

        creerSiAbsent("Risque d'exclusion",
                "Alerte lorsque les crédits validés sont insuffisants",
                TypeAlerte.RISQUE_EXCLUSION, NiveauAlerte.CRITIQUE, 30,
                null, null, null);

        creerSiAbsent("Note éliminatoire",
                "Alerte lorsqu'une note d'examen est très basse",
                TypeAlerte.NOTE_ELIMINATOIRE, NiveauAlerte.CRITIQUE, 5.0,
                null, null, null);

        creerSiAbsent("Chute de performance",
                "Alerte lors d'une baisse significative des résultats",
                TypeAlerte.CHUTE_PERFORMANCE, NiveauAlerte.ATTENTION, 0,
                null, null, 20.0);

        log.info("Règles d'alerte par défaut initialisées");
    }

    private void creerSiAbsent(String nom, String description, TypeAlerte type,
                                NiveauAlerte niveau, double seuil, Double seuilCritique,
                                Integer nombreMaxAbsences, Double pourcentageBaisse) {
        if (!regleAlerteRepository.existsByNom(nom)) {
            regleAlerteRepository.save(RegleAlerte.builder()
                    .nom(nom)
                    .description(description)
                    .type(type)
                    .niveauAlerte(niveau)
                    .seuil(seuil)
                    .seuilCritique(seuilCritique)
                    .nombreMaxAbsences(nombreMaxAbsences)
                    .pourcentageBaisse(pourcentageBaisse)
                    .actif(true)
                    .build());
        }
    }
}
