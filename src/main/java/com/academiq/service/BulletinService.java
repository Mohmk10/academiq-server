package com.academiq.service;

import com.academiq.dto.calcul.BulletinEtudiantDTO;
import com.academiq.dto.calcul.BulletinModuleDTO;
import com.academiq.dto.calcul.BulletinSemestreDTO;
import com.academiq.dto.calcul.BulletinUeDTO;
import com.academiq.entity.DecisionJury;
import com.academiq.entity.Etudiant;
import com.academiq.entity.Inscription;
import com.academiq.entity.Mention;
import com.academiq.entity.ModuleFormation;
import com.academiq.entity.Niveau;
import com.academiq.entity.Promotion;
import com.academiq.entity.Semestre;
import com.academiq.entity.UniteEnseignement;
import com.academiq.exception.BadRequestException;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.InscriptionRepository;
import com.academiq.repository.ModuleFormationRepository;
import com.academiq.repository.PromotionRepository;
import com.academiq.repository.SemestreRepository;
import com.academiq.repository.UniteEnseignementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BulletinService {

    private final CalculService calculService;
    private final EtudiantRepository etudiantRepository;
    private final InscriptionRepository inscriptionRepository;
    private final PromotionRepository promotionRepository;
    private final SemestreRepository semestreRepository;
    private final UniteEnseignementRepository uniteEnseignementRepository;
    private final ModuleFormationRepository moduleFormationRepository;

    /**
     * Génère le bulletin complet d'un étudiant pour une promotion donnée.
     */
    public BulletinEtudiantDTO genererBulletin(Long etudiantId, Long promotionId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", "id", etudiantId));

        Inscription inscription = inscriptionRepository
                .findByEtudiantIdAndPromotionId(etudiantId, promotionId)
                .orElseThrow(() -> new BadRequestException("L'étudiant n'est pas inscrit à cette promotion"));

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        Niveau niveau = promotion.getNiveau();
        List<Semestre> semestres = semestreRepository.findByNiveauId(niveau.getId());

        List<BulletinSemestreDTO> semestresDTO = new ArrayList<>();
        int totalCreditsValides = 0;
        int totalCredits = 0;

        for (Semestre semestre : semestres) {
            List<UniteEnseignement> ues = uniteEnseignementRepository.findBySemestreId(semestre.getId());
            List<BulletinUeDTO> uesDTO = new ArrayList<>();

            int creditsSemestre = 0;

            for (UniteEnseignement ue : ues) {
                List<ModuleFormation> modules = moduleFormationRepository
                        .findByUniteEnseignementId(ue.getId());

                List<BulletinModuleDTO> modulesDTO = new ArrayList<>();

                for (ModuleFormation module : modules) {
                    Double[] details = calculService.calculerDetailsModule(
                            etudiantId, module.getId(), promotionId);

                    String enseignantNom = null;
                    if (module.getEnseignant() != null) {
                        var user = module.getEnseignant().getUtilisateur();
                        enseignantNom = user.getPrenom() + " " + user.getNom();
                    }

                    modulesDTO.add(BulletinModuleDTO.builder()
                            .moduleNom(module.getNom())
                            .moduleCode(module.getCode())
                            .coefficient(module.getCoefficient())
                            .credits(module.getCredits())
                            .moyenneCC(details[0])
                            .noteExamen(details[1])
                            .moyenneModule(details[2])
                            .enseignantNom(enseignantNom)
                            .build());
                }

                Double moyenneUE = calculService.calculerMoyenneUE(etudiantId, ue.getId(), promotionId);
                boolean validee = calculService.isUEValidee(etudiantId, ue.getId(), promotionId);

                if (validee) {
                    creditsSemestre += ue.getCredits();
                }
                totalCredits += ue.getCredits();

                uesDTO.add(BulletinUeDTO.builder()
                        .ueNom(ue.getNom())
                        .ueCode(ue.getCode())
                        .credits(ue.getCredits())
                        .coefficient(ue.getCoefficient())
                        .modules(modulesDTO)
                        .moyenneUE(moyenneUE)
                        .validee(validee)
                        .build());
            }

            totalCreditsValides += creditsSemestre;

            Double moyenneSemestre = calculService.calculerMoyenneSemestre(
                    etudiantId, semestre.getId(), promotionId);

            semestresDTO.add(BulletinSemestreDTO.builder()
                    .semestreNom(semestre.getNom())
                    .ues(uesDTO)
                    .moyenneSemestre(moyenneSemestre)
                    .creditsValidesSemestre(creditsSemestre)
                    .build());
        }

        Double moyenneAnnuelle = calculService.calculerMoyenneAnnuelle(
                etudiantId, niveau.getId(), promotionId);
        DecisionJury decision = calculService.determinerDecision(
                etudiantId, niveau.getId(), promotionId);
        Mention mention = calculService.determinerMention(moyenneAnnuelle);

        var utilisateur = etudiant.getUtilisateur();

        return BulletinEtudiantDTO.builder()
                .etudiantId(etudiant.getId())
                .etudiantNom(utilisateur.getNom())
                .etudiantPrenom(utilisateur.getPrenom())
                .etudiantMatricule(etudiant.getMatricule())
                .promotionNom(promotion.getNom())
                .anneeUniversitaire(promotion.getAnneeUniversitaire())
                .filiereNom(niveau.getFiliere().getNom())
                .niveauNom(niveau.getNiveau().name())
                .semestres(semestresDTO)
                .moyenneAnnuelle(moyenneAnnuelle)
                .creditsValides(totalCreditsValides)
                .creditsTotaux(totalCredits)
                .decision(decision.name())
                .mention(mention != null ? mention.name() : null)
                .build();
    }
}
