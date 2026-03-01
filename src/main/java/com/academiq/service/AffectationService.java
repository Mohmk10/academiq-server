package com.academiq.service;

import com.academiq.entity.Affectation;
import com.academiq.entity.Enseignant;
import com.academiq.entity.ModuleFormation;
import com.academiq.entity.Promotion;
import com.academiq.exception.DuplicateResourceException;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.AffectationRepository;
import com.academiq.repository.EnseignantRepository;
import com.academiq.repository.ModuleFormationRepository;
import com.academiq.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AffectationService {

    private static final Logger log = LoggerFactory.getLogger(AffectationService.class);

    private final AffectationRepository affectationRepository;
    private final EnseignantRepository enseignantRepository;
    private final ModuleFormationRepository moduleFormationRepository;
    private final PromotionRepository promotionRepository;

    public List<Affectation> getAffectationsByEnseignant(Long enseignantId) {
        return affectationRepository.findByEnseignantId(enseignantId);
    }

    public List<Affectation> getAffectationsByModule(Long moduleId) {
        return affectationRepository.findByModuleFormationId(moduleId);
    }

    @Transactional
    public Affectation affecterEnseignant(Long enseignantId, Long moduleId, Long promotionId, String anneeUniversitaire) {
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant", "id", enseignantId));

        ModuleFormation module = moduleFormationRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        if (affectationRepository.existsByEnseignantIdAndModuleFormationIdAndPromotionId(enseignantId, moduleId, promotionId)) {
            throw new DuplicateResourceException("Affectation", "enseignant/module/promotion",
                    enseignantId + "/" + moduleId + "/" + promotionId);
        }

        Affectation affectation = Affectation.builder()
                .enseignant(enseignant)
                .moduleFormation(module)
                .promotion(promotion)
                .anneeUniversitaire(anneeUniversitaire)
                .actif(true)
                .build();

        Affectation saved = affectationRepository.save(affectation);
        log.info("Enseignant {} affecté au module {} pour la promotion {}",
                enseignantId, module.getNom(), promotion.getNom());
        return saved;
    }

    @Transactional
    public void desactiverAffectation(Long affectationId) {
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", "id", affectationId));
        affectation.setActif(false);
        affectationRepository.save(affectation);
        log.info("Affectation {} désactivée", affectationId);
    }
}
