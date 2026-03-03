package com.academiq.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAdminDTO {
    private long totalUtilisateurs;
    private long totalEtudiants;
    private long totalEnseignants;
    private long totalFilieres;
    private long totalPromotionsActives;
    private long alertesActives;
    private long alertesCritiques;
    private List<PromotionStatsDTO> promotionsStats;
    private Map<String, Long> inscriptionsParFiliere;
}
