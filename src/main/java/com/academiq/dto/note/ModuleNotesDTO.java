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
public class ModuleNotesDTO {

    private Long moduleId;
    private String moduleNom;
    private String moduleCode;
    private String ueNom;
    private double coefficient;
    private int credits;
    private List<NoteDetailDTO> notes;
    private Double moyenneModule;
}
