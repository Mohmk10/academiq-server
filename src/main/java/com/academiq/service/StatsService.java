package com.academiq.service;

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

    private double arrondir(double valeur) {
        return BigDecimal.valueOf(valeur)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
