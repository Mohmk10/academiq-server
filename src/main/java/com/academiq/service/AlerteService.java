package com.academiq.service;

import com.academiq.entity.Alerte;
import com.academiq.entity.NiveauAlerte;
import com.academiq.entity.StatutAlerte;
import com.academiq.entity.TypeAlerte;
import com.academiq.entity.Utilisateur;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.AlerteRepository;
import com.academiq.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlerteService {

    private static final Logger log = LoggerFactory.getLogger(AlerteService.class);

    private final AlerteRepository alerteRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional(readOnly = true)
    public Alerte getAlerteById(Long id) {
        return alerteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Alerte> getAlertesByEtudiant(Long etudiantId) {
        return alerteRepository.findByEtudiantId(etudiantId);
    }

    @Transactional(readOnly = true)
    public List<Alerte> getAlertesActives() {
        return alerteRepository.findByStatut(StatutAlerte.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Alerte> getAlertesByPromotion(Long promotionId) {
        return alerteRepository.findByPromotionId(promotionId);
    }

    @Transactional(readOnly = true)
    public Page<Alerte> rechercherAlertes(StatutAlerte statut, NiveauAlerte niveau,
                                          TypeAlerte type, Pageable pageable) {
        return alerteRepository.rechercherAlertes(statut, niveau, type, pageable);
    }

    @Transactional
    public Alerte traiterAlerte(Long alerteId, Long utilisateurId, String commentaire) {
        Alerte alerte = getAlerteById(alerteId);
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", utilisateurId));

        alerte.setStatut(StatutAlerte.TRAITEE);
        alerte.setTraiteePar(utilisateur);
        alerte.setDateTraitement(LocalDateTime.now());
        alerte.setCommentaireTraitement(commentaire);

        log.info("Alerte {} traitée par {}", alerteId, utilisateur.getEmail());
        return alerteRepository.save(alerte);
    }

    @Transactional
    public Alerte resoudreAlerte(Long alerteId) {
        Alerte alerte = getAlerteById(alerteId);
        alerte.setStatut(StatutAlerte.RESOLUE);
        log.info("Alerte {} résolue", alerteId);
        return alerteRepository.save(alerte);
    }

    @Transactional
    public Alerte ignorerAlerte(Long alerteId, String motif) {
        Alerte alerte = getAlerteById(alerteId);
        alerte.setStatut(StatutAlerte.IGNOREE);
        alerte.setCommentaireTraitement(motif);
        log.info("Alerte {} ignorée : {}", alerteId, motif);
        return alerteRepository.save(alerte);
    }

    @Transactional
    public void deleteAlerte(Long alerteId) {
        Alerte alerte = getAlerteById(alerteId);
        alerteRepository.delete(alerte);
        log.info("Alerte {} supprimée définitivement", alerteId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesAlertes() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalActives", alerteRepository.countByStatut(StatutAlerte.ACTIVE));
        stats.put("totalCritiques", alerteRepository.countByNiveauAndStatut(NiveauAlerte.CRITIQUE, StatutAlerte.ACTIVE));
        stats.put("totalAttention", alerteRepository.countByNiveauAndStatut(NiveauAlerte.ATTENTION, StatutAlerte.ACTIVE));
        stats.put("totalTraitees", alerteRepository.countByStatut(StatutAlerte.TRAITEE));
        stats.put("totalResolues", alerteRepository.countByStatut(StatutAlerte.RESOLUE));

        Map<String, Long> parType = new LinkedHashMap<>();
        for (TypeAlerte type : TypeAlerte.values()) {
            long count = alerteRepository.countByTypeAndStatut(type, StatutAlerte.ACTIVE);
            parType.put(type.name(), count);
        }
        stats.put("parType", parType);

        return stats;
    }
}
