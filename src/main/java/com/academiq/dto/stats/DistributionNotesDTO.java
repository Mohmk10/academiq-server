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
public class DistributionNotesDTO {
    private String contexte;
    private List<TrancheDTO> tranches;
    private Double moyenne;
    private Double mediane;
    private Double ecartType;
    private Double noteMin;
    private Double noteMax;
    private int totalNotes;
}
