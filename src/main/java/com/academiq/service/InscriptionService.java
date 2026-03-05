package com.academiq.service;

import com.academiq.entity.Etudiant;
import com.academiq.entity.Inscription;
import com.academiq.entity.Promotion;
import com.academiq.entity.StatutInscription;
import com.academiq.exception.BadRequestException;
import com.academiq.exception.DuplicateResourceException;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.InscriptionRepository;
import com.academiq.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InscriptionService {

    private static final Logger log = LoggerFactory.getLogger(InscriptionService.class);

    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PromotionRepository promotionRepository;

    public List<Inscription> getAllInscriptions() {
        return inscriptionRepository.findAll();
    }

    public List<Inscription> getInscriptionsByEtudiant(Long etudiantId) {
        return inscriptionRepository.findByEtudiantId(etudiantId);
    }

    public List<Inscription> getInscriptionsByPromotion(Long promotionId) {
        return inscriptionRepository.findByPromotionId(promotionId);
    }

    @Transactional
    public Inscription inscrireEtudiant(Long etudiantId, Long promotionId, boolean redoublant) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", "id", etudiantId));

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        if (inscriptionRepository.existsByEtudiantIdAndPromotionId(etudiantId, promotionId)) {
            throw new DuplicateResourceException("Inscription", "étudiant/promotion", etudiantId + "/" + promotionId);
        }

        if (!promotion.isActif()) {
            throw new BadRequestException("Impossible de s'inscrire à une promotion inactive");
        }

        Inscription inscription = Inscription.builder()
                .etudiant(etudiant)
                .promotion(promotion)
                .dateInscription(LocalDate.now())
                .statut(StatutInscription.ACTIVE)
                .redoublant(redoublant)
                .build();

        Inscription saved = inscriptionRepository.save(inscription);
        log.info("Étudiant {} inscrit à la promotion {}", etudiantId, promotion.getNom());
        return saved;
    }

    @Transactional
    public void annulerInscription(Long inscriptionId) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", inscriptionId));
        inscription.setStatut(StatutInscription.ANNULEE);
        inscriptionRepository.save(inscription);
        log.info("Inscription {} annulée", inscriptionId);
    }

    public long countInscritsParPromotion(Long promotionId) {
        return inscriptionRepository.countByPromotionId(promotionId);
    }
}
