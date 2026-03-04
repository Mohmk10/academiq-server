package com.academiq.service;

import com.academiq.dto.calcul.BulletinEtudiantDTO;
import com.academiq.dto.stats.DistributionNotesDTO;
import com.academiq.dto.stats.ComparaisonPromotionsDTO;
import com.academiq.dto.stats.DashboardAdminDTO;
import com.academiq.dto.stats.DashboardEnseignantDTO;
import com.academiq.dto.stats.DashboardEtudiantDTO;
import com.academiq.dto.stats.ModuleEnseignantStatsDTO;
import com.academiq.dto.stats.NoteRecenteDTO;
import com.academiq.dto.stats.SystemStatsDTO;
import com.academiq.entity.Alerte;
import com.academiq.entity.Enseignant;
import com.academiq.entity.Filiere;
import com.academiq.entity.NiveauAlerte;
import com.academiq.entity.Role;
import com.academiq.entity.StatutAlerte;
import com.academiq.dto.stats.EvolutionModuleDTO;
import com.academiq.dto.stats.EvolutionPerformanceDTO;
import com.academiq.dto.stats.PeriodeModuleDTO;
import com.academiq.dto.stats.PeriodePerformanceDTO;
import com.academiq.dto.stats.PromotionStatsDTO;
import com.academiq.entity.Niveau;
import com.academiq.dto.stats.TauxReussiteDTO;
import com.academiq.dto.stats.TrancheDTO;
import com.academiq.entity.Etudiant;
import com.academiq.entity.Evaluation;
import com.academiq.entity.ModuleFormation;
import com.academiq.entity.Note;
import com.academiq.entity.Promotion;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.entity.DecisionJury;
import com.academiq.entity.Inscription;
import com.academiq.entity.StatutInscription;
import com.academiq.repository.AlerteRepository;
import com.academiq.repository.EnseignantRepository;
import com.academiq.repository.EtudiantRepository;
import com.academiq.repository.EvaluationRepository;
import com.academiq.repository.FiliereRepository;
import com.academiq.repository.InscriptionRepository;
import com.academiq.repository.ModuleFormationRepository;
import com.academiq.repository.NoteRepository;
import com.academiq.repository.PromotionRepository;
import com.academiq.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private static final Logger log = LoggerFactory.getLogger(StatsService.class);

    private final CalculService calculService;
    private final BulletinService bulletinService;
    private final NoteRepository noteRepository;
    private final EvaluationRepository evaluationRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRepository etudiantRepository;
    private final EnseignantRepository enseignantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PromotionRepository promotionRepository;
    private final ModuleFormationRepository moduleFormationRepository;
    private final AlerteRepository alerteRepository;
    private final FiliereRepository filiereRepository;

    public TauxReussiteDTO calculerTauxReussiteModule(Long moduleId, Long promotionId) {
        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        int totalReussis = 0;
        List<Double> moyennes = new ArrayList<>();

        for (Inscription inscription : inscriptions) {
            Double moyenne = calculService.calculerMoyenneModule(
                    inscription.getEtudiant().getId(), moduleId, promotionId);
            if (moyenne != null) {
                moyennes.add(moyenne);
                if (moyenne >= 10) {
                    totalReussis++;
                }
            }
        }

        int total = inscriptions.size();
        double taux = total > 0 ? arrondir(totalReussis * 100.0 / total) : 0;
        Double moyenneGenerale = calculerMoyenneListe(moyennes);

        return TauxReussiteDTO.builder()
                .contexte("Module")
                .totalInscrits(total)
                .totalReussis(totalReussis)
                .totalEchecs(total - totalReussis)
                .tauxReussite(taux)
                .moyenneGenerale(moyenneGenerale)
                .build();
    }

    public TauxReussiteDTO calculerTauxReussiteUE(Long ueId, Long promotionId) {
        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        int totalReussis = 0;
        List<Double> moyennes = new ArrayList<>();

        for (Inscription inscription : inscriptions) {
            Double moyenne = calculService.calculerMoyenneUE(
                    inscription.getEtudiant().getId(), ueId, promotionId);
            if (moyenne != null) {
                moyennes.add(moyenne);
                if (moyenne >= 10) {
                    totalReussis++;
                }
            }
        }

        int total = inscriptions.size();
        double taux = total > 0 ? arrondir(totalReussis * 100.0 / total) : 0;
        Double moyenneGenerale = calculerMoyenneListe(moyennes);

        return TauxReussiteDTO.builder()
                .contexte("Unité d'enseignement")
                .totalInscrits(total)
                .totalReussis(totalReussis)
                .totalEchecs(total - totalReussis)
                .tauxReussite(taux)
                .moyenneGenerale(moyenneGenerale)
                .build();
    }

    public TauxReussiteDTO calculerTauxReussitePromotion(Long promotionId) {
        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        int totalReussis = 0;
        List<Double> moyennes = new ArrayList<>();

        for (Inscription inscription : inscriptions) {
            try {
                BulletinEtudiantDTO bulletin = bulletinService.genererBulletin(
                        inscription.getEtudiant().getId(), promotionId);
                if (bulletin.getMoyenneAnnuelle() != null) {
                    moyennes.add(bulletin.getMoyenneAnnuelle());
                }
                if (DecisionJury.ADMIS.name().equals(bulletin.getDecision())
                        || DecisionJury.ADMIS_COMPENSATION.name().equals(bulletin.getDecision())) {
                    totalReussis++;
                }
            } catch (Exception e) {
                log.warn("Erreur bulletin étudiant {}", inscription.getEtudiant().getId(), e);
            }
        }

        int total = inscriptions.size();
        double taux = total > 0 ? arrondir(totalReussis * 100.0 / total) : 0;
        Double moyenneGenerale = calculerMoyenneListe(moyennes);

        return TauxReussiteDTO.builder()
                .contexte("Promotion")
                .totalInscrits(total)
                .totalReussis(totalReussis)
                .totalEchecs(total - totalReussis)
                .tauxReussite(taux)
                .moyenneGenerale(moyenneGenerale)
                .build();
    }

    public DistributionNotesDTO calculerDistributionModule(Long moduleId, Long promotionId) {
        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        List<Double> valeurs = new ArrayList<>();
        for (Inscription inscription : inscriptions) {
            Double moyenne = calculService.calculerMoyenneModule(
                    inscription.getEtudiant().getId(), moduleId, promotionId);
            if (moyenne != null) {
                valeurs.add(moyenne);
            }
        }

        return construireDistribution("Module", valeurs);
    }

    public DistributionNotesDTO calculerDistributionEvaluation(Long evaluationId) {
        List<Note> notes = noteRepository.findByEvaluationId(evaluationId);

        List<Double> valeurs = new ArrayList<>();
        for (Note note : notes) {
            if (!note.isAbsent() && note.getValeur() != null) {
                valeurs.add(note.getValeur());
            }
        }

        return construireDistribution("Évaluation", valeurs);
    }

    public DashboardEtudiantDTO getDashboardEtudiant(Long etudiantId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", "id", etudiantId));

        List<Inscription> inscriptions = inscriptionRepository.findByEtudiantId(etudiantId);
        Inscription inscriptionActive = inscriptions.stream()
                .filter(i -> i.getStatut() == StatutInscription.ACTIVE)
                .findFirst()
                .orElse(null);

        if (inscriptionActive == null) {
            var utilisateur = etudiant.getUtilisateur();
            return DashboardEtudiantDTO.builder()
                    .etudiantNom(utilisateur.getPrenom() + " " + utilisateur.getNom())
                    .etudiantMatricule(etudiant.getMatricule())
                    .alertes(List.of())
                    .notesRecentes(List.of())
                    .moyennesParModule(Map.of())
                    .build();
        }

        Long promotionId = inscriptionActive.getPromotion().getId();
        BulletinEtudiantDTO bulletin = bulletinService.genererBulletin(etudiantId, promotionId);

        List<Inscription> tousInscrits = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        List<Double> toutesLes = new ArrayList<>();
        for (Inscription insc : tousInscrits) {
            try {
                BulletinEtudiantDTO b = bulletinService.genererBulletin(
                        insc.getEtudiant().getId(), promotionId);
                if (b.getMoyenneAnnuelle() != null) {
                    toutesLes.add(b.getMoyenneAnnuelle());
                }
            } catch (Exception e) {
                log.warn("Erreur calcul rang étudiant {}", insc.getEtudiant().getId());
            }
        }

        toutesLes.sort(Comparator.reverseOrder());
        int rang = 1;
        if (bulletin.getMoyenneAnnuelle() != null) {
            for (Double m : toutesLes) {
                if (m > bulletin.getMoyenneAnnuelle()) {
                    rang++;
                } else {
                    break;
                }
            }
        }

        List<Alerte> alertesEtudiant = alerteRepository.findByEtudiantIdAndStatut(
                etudiantId, StatutAlerte.ACTIVE);
        List<String> messagesAlertes = alertesEtudiant.stream()
                .map(Alerte::getMessage)
                .toList();

        List<Note> toutesNotes = noteRepository.findByEtudiantId(etudiantId);
        List<NoteRecenteDTO> notesRecentes = toutesNotes.stream()
                .filter(n -> n.getValeur() != null && !n.isAbsent())
                .sorted(Comparator.comparing(Note::getDateSaisie,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(n -> NoteRecenteDTO.builder()
                        .evaluationNom(n.getEvaluation().getNom())
                        .moduleNom(n.getEvaluation().getModuleFormation().getNom())
                        .type(n.getEvaluation().getType().name())
                        .valeur(n.getValeur())
                        .noteMaximale(n.getEvaluation().getNoteMaximale())
                        .date(n.getEvaluation().getDateEvaluation())
                        .build())
                .toList();

        Map<String, Double> moyennesParModule = new LinkedHashMap<>();
        Set<Long> moduleIds = new LinkedHashSet<>();
        List<Note> notesPromotion = noteRepository.findByEtudiantIdAndPromotionId(etudiantId, promotionId);
        for (Note note : notesPromotion) {
            moduleIds.add(note.getEvaluation().getModuleFormation().getId());
        }
        for (Long moduleId : moduleIds) {
            ModuleFormation module = moduleFormationRepository.findById(moduleId).orElse(null);
            if (module == null) continue;
            Double moyenne = calculService.calculerMoyenneModule(etudiantId, moduleId, promotionId);
            if (moyenne != null) {
                moyennesParModule.put(module.getNom(), moyenne);
            }
        }

        var utilisateur = etudiant.getUtilisateur();
        return DashboardEtudiantDTO.builder()
                .etudiantNom(utilisateur.getPrenom() + " " + utilisateur.getNom())
                .etudiantMatricule(etudiant.getMatricule())
                .promotionNom(bulletin.getPromotionNom())
                .filiereNom(bulletin.getFiliereNom())
                .niveauNom(bulletin.getNiveauNom())
                .moyenneActuelle(bulletin.getMoyenneAnnuelle())
                .creditsValides(bulletin.getCreditsValides())
                .creditsTotaux(bulletin.getCreditsTotaux())
                .rang(rang)
                .totalEtudiants(tousInscrits.size())
                .nombreAlertes(alertesEtudiant.size())
                .alertes(messagesAlertes)
                .notesRecentes(notesRecentes)
                .moyennesParModule(moyennesParModule)
                .build();
    }

    public DashboardEnseignantDTO getDashboardEnseignant(Long enseignantId) {
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant", "id", enseignantId));

        List<ModuleFormation> modules = moduleFormationRepository.findByEnseignantId(enseignantId);

        List<ModuleEnseignantStatsDTO> modulesStats = new ArrayList<>();
        int totalEtudiants = 0;
        long totalAlertes = 0;

        for (ModuleFormation module : modules) {
            List<Evaluation> evaluations = evaluationRepository.findByModuleFormationId(module.getId());
            Set<Long> promotionIds = new LinkedHashSet<>();
            for (Evaluation eval : evaluations) {
                promotionIds.add(eval.getPromotion().getId());
            }

            for (Long promotionId : promotionIds) {
                Promotion promotion = promotionRepository.findById(promotionId).orElse(null);
                if (promotion == null || !promotion.isActif()) continue;

                List<Inscription> inscriptions = inscriptionRepository
                        .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);
                int nbInscrits = inscriptions.size();
                totalEtudiants += nbInscrits;

                int nbNotesSaisies = 0;
                List<Evaluation> evalsModule = evaluationRepository
                        .findByModuleFormationIdAndPromotionId(module.getId(), promotionId);
                for (Evaluation eval : evalsModule) {
                    nbNotesSaisies += (int) noteRepository.countByEvaluationIdAndValeurIsNotNull(eval.getId());
                }

                List<Double> moyennes = new ArrayList<>();
                int reussis = 0;
                for (Inscription inscription : inscriptions) {
                    Double moyenne = calculService.calculerMoyenneModule(
                            inscription.getEtudiant().getId(), module.getId(), promotionId);
                    if (moyenne != null) {
                        moyennes.add(moyenne);
                        if (moyenne >= 10) reussis++;
                    }
                }

                Double moyenneClasse = calculerMoyenneListe(moyennes);
                double taux = nbInscrits > 0 ? arrondir(reussis * 100.0 / nbInscrits) : 0;

                List<Alerte> alertesModule = alerteRepository.findByPromotionIdAndStatut(
                        promotionId, StatutAlerte.ACTIVE);
                int nbAlertes = (int) alertesModule.stream()
                        .filter(a -> a.getModuleFormation() != null
                                && a.getModuleFormation().getId().equals(module.getId()))
                        .count();
                totalAlertes += nbAlertes;

                modulesStats.add(ModuleEnseignantStatsDTO.builder()
                        .moduleId(module.getId())
                        .moduleNom(module.getNom())
                        .moduleCode(module.getCode())
                        .promotionNom(promotion.getNom())
                        .nombreInscrits(nbInscrits)
                        .nombreNotesSaisies(nbNotesSaisies)
                        .moyenneClasse(moyenneClasse)
                        .tauxReussite(taux)
                        .nombreAlertes(nbAlertes)
                        .build());
            }
        }

        var utilisateur = enseignant.getUtilisateur();
        return DashboardEnseignantDTO.builder()
                .enseignantNom(utilisateur.getPrenom() + " " + utilisateur.getNom())
                .nombreModules(modules.size())
                .nombreEtudiantsTotal(totalEtudiants)
                .modules(modulesStats)
                .alertesActives(totalAlertes)
                .build();
    }

    public DashboardAdminDTO getDashboardAdmin() {
        long totalUtilisateurs = utilisateurRepository.countByActifTrue();
        long totalEtudiants = utilisateurRepository.countByRole(Role.ETUDIANT);
        long totalEnseignants = utilisateurRepository.countByRole(Role.ENSEIGNANT);

        List<Filiere> filieres = filiereRepository.findByActifTrue();
        long totalFilieres = filieres.size();

        List<Promotion> promotionsActives = promotionRepository.findByActifTrue();
        long totalPromotionsActives = promotionsActives.size();

        long alertesActives = alerteRepository.countByStatut(StatutAlerte.ACTIVE);
        long alertesCritiques = alerteRepository.countByNiveauAndStatut(
                NiveauAlerte.CRITIQUE, StatutAlerte.ACTIVE);

        List<PromotionStatsDTO> promotionsStats = new ArrayList<>();
        for (Promotion promo : promotionsActives) {
            try {
                promotionsStats.add(calculerStatsPromotion(promo.getId()));
            } catch (Exception e) {
                log.warn("Erreur stats promotion {}", promo.getId(), e);
            }
        }

        Map<String, Long> inscriptionsParFiliere = new LinkedHashMap<>();
        for (Promotion promo : promotionsActives) {
            try {
                String filiereNom = promo.getNiveau() != null && promo.getNiveau().getFiliere() != null
                        ? promo.getNiveau().getFiliere().getNom() : "Non classée";
                long nbInscrits = inscriptionRepository.countByPromotionId(promo.getId());
                inscriptionsParFiliere.merge(filiereNom, nbInscrits, Long::sum);
            } catch (Exception e) {
                log.warn("Erreur inscriptions filière pour promotion {}", promo.getId(), e);
            }
        }

        return DashboardAdminDTO.builder()
                .totalUtilisateurs(totalUtilisateurs)
                .totalEtudiants(totalEtudiants)
                .totalEnseignants(totalEnseignants)
                .totalFilieres(totalFilieres)
                .totalPromotionsActives(totalPromotionsActives)
                .alertesActives(alertesActives)
                .alertesCritiques(alertesCritiques)
                .promotionsStats(promotionsStats)
                .inscriptionsParFiliere(inscriptionsParFiliere)
                .build();
    }

    public ComparaisonPromotionsDTO comparerPromotions(List<Long> promotionIds) {
        List<PromotionStatsDTO> stats = new ArrayList<>();

        for (Long promotionId : promotionIds) {
            stats.add(calculerStatsPromotion(promotionId));
        }

        return ComparaisonPromotionsDTO.builder()
                .promotions(stats)
                .build();
    }

    PromotionStatsDTO calculerStatsPromotion(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));
        Niveau niveau = promotion.getNiveau();

        List<Inscription> inscriptions = inscriptionRepository
                .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

        int nbAdmis = 0, nbAjournes = 0, nbRattrapage = 0;
        List<Double> moyennes = new ArrayList<>();
        Map<String, Integer> mentions = new LinkedHashMap<>();
        mentions.put("PASSABLE", 0);
        mentions.put("ASSEZ_BIEN", 0);
        mentions.put("BIEN", 0);
        mentions.put("TRES_BIEN", 0);
        mentions.put("EXCELLENT", 0);

        for (Inscription inscription : inscriptions) {
            try {
                BulletinEtudiantDTO bulletin = bulletinService.genererBulletin(
                        inscription.getEtudiant().getId(), promotionId);

                if (bulletin.getMoyenneAnnuelle() != null) {
                    moyennes.add(bulletin.getMoyenneAnnuelle());
                }

                if (DecisionJury.ADMIS.name().equals(bulletin.getDecision())
                        || DecisionJury.ADMIS_COMPENSATION.name().equals(bulletin.getDecision())) {
                    nbAdmis++;
                    if (bulletin.getMention() != null) {
                        mentions.merge(bulletin.getMention(), 1, Integer::sum);
                    }
                } else if (DecisionJury.AJOURNE.name().equals(bulletin.getDecision())) {
                    nbAjournes++;
                } else if (DecisionJury.RATTRAPAGE.name().equals(bulletin.getDecision())) {
                    nbRattrapage++;
                }
            } catch (Exception e) {
                log.warn("Erreur stats promotion {} étudiant {}",
                        promotionId, inscription.getEtudiant().getId(), e);
            }
        }

        int total = inscriptions.size();
        double taux = total > 0 ? arrondir(nbAdmis * 100.0 / total) : 0;

        return PromotionStatsDTO.builder()
                .promotionId(promotionId)
                .promotionNom(promotion.getNom())
                .anneeUniversitaire(promotion.getAnneeUniversitaire())
                .filiereNom(niveau != null && niveau.getFiliere() != null ? niveau.getFiliere().getNom() : "N/A")
                .niveauNom(niveau != null && niveau.getNiveau() != null ? niveau.getNiveau().name() : "N/A")
                .nombreInscrits(total)
                .moyenneGenerale(calculerMoyenneListe(moyennes))
                .tauxReussite(taux)
                .mentions(mentions)
                .nombreAdmis(nbAdmis)
                .nombreAjournes(nbAjournes)
                .nombreRattrapage(nbRattrapage)
                .build();
    }

    public EvolutionPerformanceDTO calculerEvolutionEtudiant(Long etudiantId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", "id", etudiantId));

        List<Inscription> inscriptions = inscriptionRepository.findByEtudiantId(etudiantId);
        inscriptions.sort(Comparator.comparing(Inscription::getDateInscription));

        List<PeriodePerformanceDTO> periodes = new ArrayList<>();

        for (Inscription inscription : inscriptions) {
            try {
                BulletinEtudiantDTO bulletin = bulletinService.genererBulletin(
                        etudiantId, inscription.getPromotion().getId());

                periodes.add(PeriodePerformanceDTO.builder()
                        .promotionNom(bulletin.getPromotionNom())
                        .anneeUniversitaire(bulletin.getAnneeUniversitaire())
                        .niveauNom(bulletin.getNiveauNom())
                        .moyenneAnnuelle(bulletin.getMoyenneAnnuelle())
                        .creditsValides(bulletin.getCreditsValides())
                        .creditsTotaux(bulletin.getCreditsTotaux())
                        .decision(bulletin.getDecision())
                        .build());
            } catch (Exception e) {
                log.warn("Erreur évolution étudiant {} promotion {}",
                        etudiantId, inscription.getPromotion().getId(), e);
            }
        }

        var utilisateur = etudiant.getUtilisateur();
        return EvolutionPerformanceDTO.builder()
                .etudiantNom(utilisateur.getPrenom() + " " + utilisateur.getNom())
                .etudiantMatricule(etudiant.getMatricule())
                .periodes(periodes)
                .build();
    }

    public EvolutionModuleDTO calculerEvolutionModule(Long moduleId) {
        ModuleFormation module = moduleFormationRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));

        List<Evaluation> evaluations = evaluationRepository.findByModuleFormationId(moduleId);
        Set<Long> promotionIds = new LinkedHashSet<>();
        for (Evaluation eval : evaluations) {
            promotionIds.add(eval.getPromotion().getId());
        }

        List<PeriodeModuleDTO> periodes = new ArrayList<>();

        for (Long promotionId : promotionIds) {
            Promotion promotion = promotionRepository.findById(promotionId).orElse(null);
            if (promotion == null) continue;

            List<Inscription> inscriptions = inscriptionRepository
                    .findByPromotionIdAndStatut(promotionId, StatutInscription.ACTIVE);

            List<Double> moyennes = new ArrayList<>();
            int reussis = 0;

            for (Inscription inscription : inscriptions) {
                Double moyenne = calculService.calculerMoyenneModule(
                        inscription.getEtudiant().getId(), moduleId, promotionId);
                if (moyenne != null) {
                    moyennes.add(moyenne);
                    if (moyenne >= 10) reussis++;
                }
            }

            Double moyenneClasse = calculerMoyenneListe(moyennes);
            int total = inscriptions.size();
            double taux = total > 0 ? arrondir(reussis * 100.0 / total) : 0;

            periodes.add(PeriodeModuleDTO.builder()
                    .anneeUniversitaire(promotion.getAnneeUniversitaire())
                    .promotionNom(promotion.getNom())
                    .moyenneClasse(moyenneClasse)
                    .tauxReussite(taux)
                    .nombreInscrits(total)
                    .build());
        }

        periodes.sort(Comparator.comparing(PeriodeModuleDTO::getAnneeUniversitaire));

        return EvolutionModuleDTO.builder()
                .moduleNom(module.getNom())
                .moduleCode(module.getCode())
                .periodes(periodes)
                .build();
    }

    private DistributionNotesDTO construireDistribution(String contexte, List<Double> valeurs) {
        if (valeurs.isEmpty()) {
            return DistributionNotesDTO.builder()
                    .contexte(contexte)
                    .tranches(List.of())
                    .totalNotes(0)
                    .build();
        }

        Collections.sort(valeurs);
        int total = valeurs.size();

        double[][] bornes = {
                {0, 4}, {4, 8}, {8, 10}, {10, 12}, {12, 14}, {14, 16}, {16, 20}
        };
        String[] labels = {"0-4", "4-8", "8-10", "10-12", "12-14", "14-16", "16-20"};

        List<TrancheDTO> tranches = new ArrayList<>();
        for (int i = 0; i < bornes.length; i++) {
            double inf = bornes[i][0];
            double sup = bornes[i][1];
            int count = 0;
            for (double v : valeurs) {
                if (i == bornes.length - 1) {
                    if (v >= inf && v <= sup) count++;
                } else {
                    if (v >= inf && v < sup) count++;
                }
            }
            tranches.add(TrancheDTO.builder()
                    .label(labels[i])
                    .borneInf(inf)
                    .borneSup(sup)
                    .nombre(count)
                    .pourcentage(arrondir(count * 100.0 / total))
                    .build());
        }

        double somme = valeurs.stream().mapToDouble(Double::doubleValue).sum();
        double moyenne = somme / total;

        double mediane;
        if (total % 2 == 0) {
            mediane = (valeurs.get(total / 2 - 1) + valeurs.get(total / 2)) / 2.0;
        } else {
            mediane = valeurs.get(total / 2);
        }

        double varianceSum = valeurs.stream()
                .mapToDouble(v -> Math.pow(v - moyenne, 2))
                .sum();
        double ecartType = Math.sqrt(varianceSum / total);

        return DistributionNotesDTO.builder()
                .contexte(contexte)
                .tranches(tranches)
                .moyenne(arrondir(moyenne))
                .mediane(arrondir(mediane))
                .ecartType(arrondir(ecartType))
                .noteMin(valeurs.getFirst())
                .noteMax(valeurs.getLast())
                .totalNotes(total)
                .build();
    }

    private Double calculerMoyenneListe(List<Double> valeurs) {
        if (valeurs.isEmpty()) return null;
        double somme = valeurs.stream().mapToDouble(Double::doubleValue).sum();
        return arrondir(somme / valeurs.size());
    }

    public SystemStatsDTO getSystemStats() {
        long total = utilisateurRepository.count();
        long actifs = utilisateurRepository.countByActifTrue();

        return SystemStatsDTO.builder()
                .totalUtilisateurs(total)
                .utilisateursActifs(actifs)
                .totalEtudiants(utilisateurRepository.countByRole(Role.ETUDIANT))
                .totalEnseignants(utilisateurRepository.countByRole(Role.ENSEIGNANT))
                .totalAdmins(utilisateurRepository.countByRole(Role.ADMIN)
                        + utilisateurRepository.countByRole(Role.SUPER_ADMIN))
                .totalResponsables(utilisateurRepository.countByRole(Role.RESPONSABLE_PEDAGOGIQUE))
                .comptesInactifs(total - actifs)
                .totalEvaluations(evaluationRepository.count())
                .totalNotes(noteRepository.count())
                .alertesActives(alerteRepository.countByStatut(StatutAlerte.ACTIVE))
                .build();
    }

    private double arrondir(double valeur) {
        return BigDecimal.valueOf(valeur)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
