package com.academiq.dto.note;

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
public class NotePrepopuleeDTO {

    private Long etudiantId;
    private String etudiantNom;
    private String etudiantMatricule;
    private Double noteExistante;
    private boolean absent;
    private boolean dejaNotee;
}
