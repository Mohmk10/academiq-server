package com.academiq.dto.note;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class NoteSaisieEnMasseRequest {

    @NotNull
    private Long evaluationId;

    @NotEmpty
    private List<NoteSaisieDTO> notes;
}
