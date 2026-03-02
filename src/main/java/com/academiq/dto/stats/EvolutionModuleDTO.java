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
public class EvolutionModuleDTO {
    private String moduleNom;
    private String moduleCode;
    private List<PeriodeModuleDTO> periodes;
}
