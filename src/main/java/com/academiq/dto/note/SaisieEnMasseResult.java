package com.academiq.dto.note;

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
public class SaisieEnMasseResult {

    private int totalTraites;
    private int totalSucces;
    private int totalErreurs;
    private List<String> erreurs;
}
