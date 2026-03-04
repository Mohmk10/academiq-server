package com.academiq.mapper;

import com.academiq.dto.structure.AffectationResponse;
import com.academiq.dto.structure.FiliereRequest;
import com.academiq.dto.structure.FiliereResponse;
import com.academiq.dto.structure.InscriptionResponse;
import com.academiq.dto.structure.ModuleRequest;
import com.academiq.dto.structure.ModuleResponse;
import com.academiq.dto.structure.NiveauRequest;
import com.academiq.dto.structure.NiveauResponse;
import com.academiq.dto.structure.PromotionRequest;
import com.academiq.dto.structure.PromotionResponse;
import com.academiq.dto.structure.SemestreRequest;
import com.academiq.dto.structure.SemestreResponse;
import com.academiq.dto.structure.UeRequest;
import com.academiq.dto.structure.UeResponse;
import com.academiq.entity.Affectation;
import com.academiq.entity.Enseignant;
import com.academiq.entity.Filiere;
import com.academiq.entity.Inscription;
import com.academiq.entity.ModuleFormation;
import com.academiq.entity.Niveau;
import com.academiq.entity.Promotion;
import com.academiq.entity.Semestre;
import com.academiq.entity.UniteEnseignement;
import com.academiq.entity.Utilisateur;
import com.academiq.repository.EnseignantRepository;
import com.academiq.repository.InscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StructureMapper {

    private final EnseignantRepository enseignantRepository;
    private final InscriptionRepository inscriptionRepository;

    // ======================== Filière ========================

    public Filiere toFiliere(FiliereRequest request) {
        Filiere filiere = Filiere.builder()
                .code(request.getCode())
                .nom(request.getNom())
                .description(request.getDescription())
                .build();

        if (request.getResponsableId() != null) {
            enseignantRepository.findById(request.getResponsableId())
                    .ifPresent(filiere::setResponsable);
        }
        return filiere;
    }

    public FiliereResponse toFiliereResponse(Filiere filiere) {
        return FiliereResponse.builder()
                .id(filiere.getId())
                .code(filiere.getCode())
                .nom(filiere.getNom())
                .description(filiere.getDescription())
                .actif(filiere.isActif())
                .responsableNom(resolveEnseignantNom(filiere.getResponsable()))
                .nombreNiveaux(filiere.getNiveaux() != null ? filiere.getNiveaux().size() : 0)
                .build();
    }

    public List<FiliereResponse> toFiliereResponseList(List<Filiere> filieres) {
        return filieres.stream().map(this::toFiliereResponse).toList();
    }

    // ======================== Niveau ========================

    public Niveau toNiveau(NiveauRequest request) {
        return Niveau.builder()
                .niveau(request.getNiveau())
                .nombreSemestres(request.getNombreSemestres() > 0 ? request.getNombreSemestres() : 2)
                .creditsRequis(request.getCreditsRequis())
                .build();
    }

    public NiveauResponse toNiveauResponse(Niveau niveau) {
        Filiere filiere = niveau.getFiliere();
        return NiveauResponse.builder()
                .id(niveau.getId())
                .niveau(niveau.getNiveau() != null ? niveau.getNiveau().name() : null)
                .nombreSemestres(niveau.getNombreSemestres())
                .creditsRequis(niveau.getCreditsRequis())
                .filiereId(filiere != null ? filiere.getId() : null)
                .filiereNom(filiere != null ? filiere.getNom() : null)
                .nombrePromotions(niveau.getPromotions() != null ? niveau.getPromotions().size() : 0)
                .build();
    }

    public List<NiveauResponse> toNiveauResponseList(List<Niveau> niveaux) {
        return niveaux.stream().map(this::toNiveauResponse).toList();
    }

    // ======================== Promotion ========================

    public Promotion toPromotion(PromotionRequest request) {
        return Promotion.builder()
                .anneeUniversitaire(request.getAnneeUniversitaire())
                .nom(request.getNom())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .build();
    }

    public PromotionResponse toPromotionResponse(Promotion promotion) {
        Niveau niveau = promotion.getNiveau();
        Filiere filiere = niveau != null ? niveau.getFiliere() : null;
        return PromotionResponse.builder()
                .id(promotion.getId())
                .anneeUniversitaire(promotion.getAnneeUniversitaire())
                .nom(promotion.getNom())
                .dateDebut(promotion.getDateDebut())
                .dateFin(promotion.getDateFin())
                .actif(promotion.isActif())
                .niveauId(niveau != null ? niveau.getId() : null)
                .niveauNom(niveau != null && niveau.getNiveau() != null ? niveau.getNiveau().name() : null)
                .filiereNom(filiere != null ? filiere.getNom() : null)
                .nombreInscrits(inscriptionRepository.countByPromotionId(promotion.getId()))
                .build();
    }

    public List<PromotionResponse> toPromotionResponseList(List<Promotion> promotions) {
        return promotions.stream().map(this::toPromotionResponse).toList();
    }

    // ======================== Semestre ========================

    public Semestre toSemestre(SemestreRequest request) {
        return Semestre.builder()
                .semestre(request.getSemestre())
                .nom(request.getNom() != null ? request.getNom() : "Semestre " + request.getSemestre().name().substring(1))
                .build();
    }

    public SemestreResponse toSemestreResponse(Semestre semestre) {
        Niveau niveau = semestre.getNiveau();
        return SemestreResponse.builder()
                .id(semestre.getId())
                .semestre(semestre.getSemestre() != null ? semestre.getSemestre().name() : null)
                .nom(semestre.getNom())
                .niveauId(niveau != null ? niveau.getId() : null)
                .nombreUes(semestre.getUniteEnseignements() != null ? semestre.getUniteEnseignements().size() : 0)
                .build();
    }

    public List<SemestreResponse> toSemestreResponseList(List<Semestre> semestres) {
        return semestres.stream().map(this::toSemestreResponse).toList();
    }

    // ======================== UE ========================

    public UniteEnseignement toUe(UeRequest request) {
        return UniteEnseignement.builder()
                .code(request.getCode())
                .nom(request.getNom())
                .description(request.getDescription())
                .credits(request.getCredits())
                .coefficient(request.getCoefficient())
                .build();
    }

    public UeResponse toUeResponse(UniteEnseignement ue) {
        Semestre semestre = ue.getSemestre();
        return UeResponse.builder()
                .id(ue.getId())
                .code(ue.getCode())
                .nom(ue.getNom())
                .description(ue.getDescription())
                .credits(ue.getCredits())
                .coefficient(ue.getCoefficient())
                .semestreId(semestre != null ? semestre.getId() : null)
                .semestreNom(semestre != null ? semestre.getNom() : null)
                .nombreModules(ue.getModules() != null ? ue.getModules().size() : 0)
                .build();
    }

    public List<UeResponse> toUeResponseList(List<UniteEnseignement> ues) {
        return ues.stream().map(this::toUeResponse).toList();
    }

    // ======================== Module ========================

    public ModuleFormation toModule(ModuleRequest request) {
        ModuleFormation module = ModuleFormation.builder()
                .code(request.getCode())
                .nom(request.getNom())
                .description(request.getDescription())
                .credits(request.getCredits())
                .coefficient(request.getCoefficient())
                .volumeHoraireCM(request.getVolumeHoraireCM())
                .volumeHoraireTD(request.getVolumeHoraireTD())
                .volumeHoraireTP(request.getVolumeHoraireTP())
                .ponderationCC(request.getPonderationCC())
                .ponderationExamen(request.getPonderationExamen())
                .build();

        if (request.getEnseignantId() != null) {
            enseignantRepository.findById(request.getEnseignantId())
                    .ifPresent(module::setEnseignant);
        }
        return module;
    }

    public ModuleResponse toModuleResponse(ModuleFormation module) {
        UniteEnseignement ue = module.getUniteEnseignement();
        return ModuleResponse.builder()
                .id(module.getId())
                .code(module.getCode())
                .nom(module.getNom())
                .description(module.getDescription())
                .credits(module.getCredits())
                .coefficient(module.getCoefficient())
                .volumeHoraireCM(module.getVolumeHoraireCM())
                .volumeHoraireTD(module.getVolumeHoraireTD())
                .volumeHoraireTP(module.getVolumeHoraireTP())
                .volumeHoraireTotal(module.getVolumeHoraireTotal())
                .ponderationCC(module.getPonderationCC())
                .ponderationExamen(module.getPonderationExamen())
                .ueId(ue != null ? ue.getId() : null)
                .ueNom(ue != null ? ue.getNom() : null)
                .enseignantId(module.getEnseignant() != null ? module.getEnseignant().getId() : null)
                .enseignantNom(resolveEnseignantNom(module.getEnseignant()))
                .build();
    }

    public List<ModuleResponse> toModuleResponseList(List<ModuleFormation> modules) {
        return modules.stream().map(this::toModuleResponse).toList();
    }

    // ======================== Inscription ========================

    public InscriptionResponse toInscriptionResponse(Inscription inscription) {
        var etudiant = inscription.getEtudiant();
        Utilisateur utilisateur = etudiant != null ? etudiant.getUtilisateur() : null;
        Promotion promotion = inscription.getPromotion();
        return InscriptionResponse.builder()
                .id(inscription.getId())
                .etudiantId(etudiant != null ? etudiant.getId() : null)
                .etudiantNom(utilisateur != null ? utilisateur.getPrenom() + " " + utilisateur.getNom() : null)
                .etudiantMatricule(etudiant != null ? etudiant.getMatricule() : null)
                .promotionId(promotion != null ? promotion.getId() : null)
                .promotionNom(promotion != null ? promotion.getNom() : null)
                .dateInscription(inscription.getDateInscription())
                .statut(inscription.getStatut() != null ? inscription.getStatut().name() : null)
                .redoublant(inscription.isRedoublant())
                .build();
    }

    public List<InscriptionResponse> toInscriptionResponseList(List<Inscription> inscriptions) {
        return inscriptions.stream().map(this::toInscriptionResponse).toList();
    }

    // ======================== Affectation ========================

    public AffectationResponse toAffectationResponse(Affectation affectation) {
        var enseignant = affectation.getEnseignant();
        Utilisateur utilisateur = enseignant != null ? enseignant.getUtilisateur() : null;
        ModuleFormation module = affectation.getModuleFormation();
        Promotion promotion = affectation.getPromotion();
        return AffectationResponse.builder()
                .id(affectation.getId())
                .enseignantId(enseignant != null ? enseignant.getId() : null)
                .enseignantNom(utilisateur != null ? utilisateur.getPrenom() + " " + utilisateur.getNom() : null)
                .moduleId(module != null ? module.getId() : null)
                .moduleNom(module != null ? module.getNom() : null)
                .promotionId(promotion != null ? promotion.getId() : null)
                .promotionNom(promotion != null ? promotion.getNom() : null)
                .anneeUniversitaire(affectation.getAnneeUniversitaire())
                .actif(affectation.isActif())
                .build();
    }

    public List<AffectationResponse> toAffectationResponseList(List<Affectation> affectations) {
        return affectations.stream().map(this::toAffectationResponse).toList();
    }

    // ======================== Utilitaire ========================

    private String resolveEnseignantNom(Enseignant enseignant) {
        if (enseignant == null) {
            return null;
        }
        Utilisateur utilisateur = enseignant.getUtilisateur();
        return utilisateur.getPrenom() + " " + utilisateur.getNom();
    }
}
