package com.academiq.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemStatsDTO {
    private long totalUtilisateurs;
    private long utilisateursActifs;
    private long totalEtudiants;
    private long totalEnseignants;
    private long totalAdmins;
    private long totalResponsables;
    private long comptesInactifs;
    private long totalEvaluations;
    private long totalNotes;
    private long alertesActives;
}
