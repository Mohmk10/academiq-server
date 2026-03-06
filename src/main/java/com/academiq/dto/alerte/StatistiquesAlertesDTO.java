package com.academiq.dto.alerte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatistiquesAlertesDTO {
    private long totalActives;
    private long totalCritiques;
    private long totalAttention;
    private long totalTraitees;
    private long totalResolues;
    private Map<String, Long> parType;
}
