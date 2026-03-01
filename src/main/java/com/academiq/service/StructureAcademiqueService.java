package com.academiq.service;

import com.academiq.entity.Filiere;
import com.academiq.entity.ModuleFormation;
import com.academiq.entity.Niveau;
import com.academiq.entity.Promotion;
import com.academiq.entity.Semestre;
import com.academiq.entity.UniteEnseignement;
import com.academiq.exception.BadRequestException;
import com.academiq.exception.DuplicateResourceException;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.FiliereRepository;
import com.academiq.repository.ModuleFormationRepository;
import com.academiq.repository.NiveauRepository;
import com.academiq.repository.PromotionRepository;
import com.academiq.repository.SemestreRepository;
import com.academiq.repository.UniteEnseignementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StructureAcademiqueService {

    private static final Logger log = LoggerFactory.getLogger(StructureAcademiqueService.class);

    private final FiliereRepository filiereRepository;
    private final NiveauRepository niveauRepository;
    private final PromotionRepository promotionRepository;
    private final SemestreRepository semestreRepository;
    private final UniteEnseignementRepository uniteEnseignementRepository;
    private final ModuleFormationRepository moduleFormationRepository;

    // ======================== Filières ========================

    public List<Filiere> getAllFilieres() {
        return filiereRepository.findByActifTrue();
    }

    public Filiere getFiliereById(Long id) {
        return filiereRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filière", "id", id));
    }

    @Transactional
    public Filiere createFiliere(Filiere filiere) {
        if (filiereRepository.existsByCode(filiere.getCode())) {
            throw new DuplicateResourceException("Filière", "code", filiere.getCode());
        }
        if (filiereRepository.existsByNom(filiere.getNom())) {
            throw new DuplicateResourceException("Filière", "nom", filiere.getNom());
        }
        Filiere saved = filiereRepository.save(filiere);
        log.info("Filière créée : {} ({})", saved.getNom(), saved.getCode());
        return saved;
    }

    @Transactional
    public Filiere updateFiliere(Long id, Filiere data) {
        Filiere filiere = getFiliereById(id);
        if (data.getNom() != null) {
            filiere.setNom(data.getNom());
        }
        if (data.getDescription() != null) {
            filiere.setDescription(data.getDescription());
        }
        if (data.getResponsable() != null) {
            filiere.setResponsable(data.getResponsable());
        }
        return filiereRepository.save(filiere);
    }

    @Transactional
    public void deleteFiliere(Long id) {
        Filiere filiere = getFiliereById(id);
        if (!filiere.getNiveaux().isEmpty()) {
            throw new BadRequestException("Impossible de supprimer une filière contenant des niveaux");
        }
        filiereRepository.delete(filiere);
        log.info("Filière supprimée : {}", id);
    }

    // ======================== Niveaux ========================

    public List<Niveau> getNiveauxByFiliere(Long filiereId) {
        return niveauRepository.findByFiliereId(filiereId);
    }

    public Niveau getNiveauById(Long id) {
        return niveauRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Niveau", "id", id));
    }

    @Transactional
    public Niveau createNiveau(Long filiereId, Niveau niveau) {
        Filiere filiere = getFiliereById(filiereId);
        if (niveauRepository.existsByFiliereIdAndNiveau(filiereId, niveau.getNiveau())) {
            throw new DuplicateResourceException("Niveau", "filière/niveau", filiereId + "/" + niveau.getNiveau());
        }
        niveau.setFiliere(filiere);
        Niveau saved = niveauRepository.save(niveau);
        log.info("Niveau {} créé pour la filière {}", saved.getNiveau(), filiere.getCode());
        return saved;
    }

    @Transactional
    public void deleteNiveau(Long id) {
        Niveau niveau = getNiveauById(id);
        niveauRepository.delete(niveau);
        log.info("Niveau supprimé : {}", id);
    }

    // ======================== Promotions ========================

    public List<Promotion> getPromotionsByNiveau(Long niveauId) {
        return promotionRepository.findByNiveauId(niveauId);
    }

    public List<Promotion> getPromotionsActives() {
        return promotionRepository.findByActifTrue();
    }

    public Promotion getPromotionById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
    }

    @Transactional
    public Promotion createPromotion(Long niveauId, Promotion promotion) {
        Niveau niveau = getNiveauById(niveauId);
        if (promotionRepository.existsByNiveauIdAndAnneeUniversitaire(niveauId, promotion.getAnneeUniversitaire())) {
            throw new DuplicateResourceException("Promotion", "niveau/année", niveauId + "/" + promotion.getAnneeUniversitaire());
        }
        promotion.setNiveau(niveau);
        Promotion saved = promotionRepository.save(promotion);
        log.info("Promotion créée : {}", saved.getNom());
        return saved;
    }

    @Transactional
    public Promotion updatePromotion(Long id, Promotion data) {
        Promotion promotion = getPromotionById(id);
        if (data.getNom() != null) {
            promotion.setNom(data.getNom());
        }
        if (data.getDateDebut() != null) {
            promotion.setDateDebut(data.getDateDebut());
        }
        if (data.getDateFin() != null) {
            promotion.setDateFin(data.getDateFin());
        }
        promotion.setActif(data.isActif());
        return promotionRepository.save(promotion);
    }

    // ======================== Semestres ========================

    public List<Semestre> getSemestresByNiveau(Long niveauId) {
        return semestreRepository.findByNiveauId(niveauId);
    }

    public Semestre getSemestreById(Long id) {
        return semestreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semestre", "id", id));
    }

    @Transactional
    public Semestre createSemestre(Long niveauId, Semestre semestre) {
        Niveau niveau = getNiveauById(niveauId);
        if (semestreRepository.existsByNiveauIdAndSemestre(niveauId, semestre.getSemestre())) {
            throw new DuplicateResourceException("Semestre", "niveau/semestre", niveauId + "/" + semestre.getSemestre());
        }
        semestre.setNiveau(niveau);
        Semestre saved = semestreRepository.save(semestre);
        log.info("Semestre {} créé pour le niveau {}", saved.getSemestre(), niveau.getNiveau());
        return saved;
    }

    // ======================== UEs ========================

    public List<UniteEnseignement> getUesBySemestre(Long semestreId) {
        return uniteEnseignementRepository.findBySemestreId(semestreId);
    }

    public UniteEnseignement getUeById(Long id) {
        return uniteEnseignementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unité d'enseignement", "id", id));
    }

    @Transactional
    public UniteEnseignement createUe(Long semestreId, UniteEnseignement ue) {
        Semestre semestre = getSemestreById(semestreId);
        if (uniteEnseignementRepository.existsBySemestreIdAndCode(semestreId, ue.getCode())) {
            throw new DuplicateResourceException("UE", "semestre/code", semestreId + "/" + ue.getCode());
        }
        ue.setSemestre(semestre);
        UniteEnseignement saved = uniteEnseignementRepository.save(ue);
        log.info("UE créée : {} ({})", saved.getNom(), saved.getCode());
        return saved;
    }

    @Transactional
    public UniteEnseignement updateUe(Long id, UniteEnseignement data) {
        UniteEnseignement ue = getUeById(id);
        if (data.getNom() != null) {
            ue.setNom(data.getNom());
        }
        if (data.getDescription() != null) {
            ue.setDescription(data.getDescription());
        }
        if (data.getCredits() > 0) {
            ue.setCredits(data.getCredits());
        }
        if (data.getCoefficient() > 0) {
            ue.setCoefficient(data.getCoefficient());
        }
        return uniteEnseignementRepository.save(ue);
    }

    @Transactional
    public void deleteUe(Long id) {
        UniteEnseignement ue = getUeById(id);
        uniteEnseignementRepository.delete(ue);
        log.info("UE supprimée : {}", id);
    }

    // ======================== Modules ========================

    public List<ModuleFormation> getModulesByUe(Long ueId) {
        return moduleFormationRepository.findByUniteEnseignementId(ueId);
    }

    public List<ModuleFormation> getModulesByEnseignant(Long enseignantId) {
        return moduleFormationRepository.findByEnseignantId(enseignantId);
    }

    public ModuleFormation getModuleById(Long id) {
        return moduleFormationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", id));
    }

    @Transactional
    public ModuleFormation createModule(Long ueId, ModuleFormation module) {
        UniteEnseignement ue = getUeById(ueId);
        if (moduleFormationRepository.existsByUniteEnseignementIdAndCode(ueId, module.getCode())) {
            throw new DuplicateResourceException("Module", "UE/code", ueId + "/" + module.getCode());
        }
        double sommePonderations = module.getPonderationCC() + module.getPonderationExamen();
        if (Math.abs(sommePonderations - 1.0) > 0.01) {
            throw new BadRequestException("La somme des pondérations CC et Examen doit être égale à 1.0 (actuelle : " + sommePonderations + ")");
        }
        module.setUniteEnseignement(ue);
        ModuleFormation saved = moduleFormationRepository.save(module);
        log.info("Module créé : {} ({})", saved.getNom(), saved.getCode());
        return saved;
    }

    @Transactional
    public ModuleFormation updateModule(Long id, ModuleFormation data) {
        ModuleFormation module = getModuleById(id);
        if (data.getNom() != null) {
            module.setNom(data.getNom());
        }
        if (data.getDescription() != null) {
            module.setDescription(data.getDescription());
        }
        if (data.getCredits() > 0) {
            module.setCredits(data.getCredits());
        }
        if (data.getCoefficient() > 0) {
            module.setCoefficient(data.getCoefficient());
        }
        if (data.getVolumeHoraireCM() > 0) {
            module.setVolumeHoraireCM(data.getVolumeHoraireCM());
        }
        if (data.getVolumeHoraireTD() > 0) {
            module.setVolumeHoraireTD(data.getVolumeHoraireTD());
        }
        if (data.getVolumeHoraireTP() > 0) {
            module.setVolumeHoraireTP(data.getVolumeHoraireTP());
        }
        if (data.getPonderationCC() > 0 || data.getPonderationExamen() > 0) {
            double cc = data.getPonderationCC() > 0 ? data.getPonderationCC() : module.getPonderationCC();
            double ex = data.getPonderationExamen() > 0 ? data.getPonderationExamen() : module.getPonderationExamen();
            if (Math.abs(cc + ex - 1.0) > 0.01) {
                throw new BadRequestException("La somme des pondérations CC et Examen doit être égale à 1.0");
            }
            module.setPonderationCC(cc);
            module.setPonderationExamen(ex);
        }
        if (data.getEnseignant() != null) {
            module.setEnseignant(data.getEnseignant());
        }
        return moduleFormationRepository.save(module);
    }

    @Transactional
    public void deleteModule(Long id) {
        ModuleFormation module = getModuleById(id);
        moduleFormationRepository.delete(module);
        log.info("Module supprimé : {}", id);
    }
}
