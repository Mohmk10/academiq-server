package com.academiq.service;

import com.academiq.repository.EvaluationRepository;
import com.academiq.repository.InscriptionRepository;
import com.academiq.repository.ModuleFormationRepository;
import com.academiq.repository.NoteRepository;
import com.academiq.repository.SemestreRepository;
import com.academiq.repository.UniteEnseignementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service centralisé pour tous les calculs académiques :
 * moyennes, crédits, décisions de jury et mentions.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalculService {

    private static final Logger log = LoggerFactory.getLogger(CalculService.class);

    private final NoteRepository noteRepository;
    private final EvaluationRepository evaluationRepository;
    private final ModuleFormationRepository moduleFormationRepository;
    private final UniteEnseignementRepository uniteEnseignementRepository;
    private final SemestreRepository semestreRepository;
    private final InscriptionRepository inscriptionRepository;

    /**
     * Arrondit une valeur au nombre de décimales spécifié (arrondi demi-supérieur).
     */
    private double arrondir(double valeur, int decimales) {
        return BigDecimal.valueOf(valeur)
                .setScale(decimales, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
