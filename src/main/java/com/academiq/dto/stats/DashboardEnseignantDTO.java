package com.academiq.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardEnseignantDTO {
    private String enseignantNom;
    private int nombreModules;
    private int nombreEtudiantsTotal;
    private List<ModuleEnseignantStatsDTO> modules;
    private long alertesActives;
}
