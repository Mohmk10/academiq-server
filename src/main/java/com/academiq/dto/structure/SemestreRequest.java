package com.academiq.dto.structure;

import com.academiq.entity.SemestreEnum;
import jakarta.validation.constraints.NotNull;
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
public class SemestreRequest {

    @NotNull
    private SemestreEnum semestre;

    private String nom;
}
