package com.academiq.controller;

import com.academiq.dto.ApiResponse;
import com.academiq.dto.note.EvaluationRequest;
import com.academiq.dto.note.EvaluationResponse;
import com.academiq.dto.note.NotePrepopuleeDTO;
import com.academiq.dto.note.HistoriqueNoteResponse;
import com.academiq.dto.note.NoteResponse;
import com.academiq.dto.note.RecapitulatifEtudiantDTO;
import com.academiq.dto.note.RecapitulatifModuleDTO;
import com.academiq.dto.note.StatistiquesEvaluationDTO;
import com.academiq.dto.note.NoteSaisieEnMasseRequest;
import com.academiq.dto.note.NoteSaisieRequest;
import com.academiq.dto.note.SaisieEnMasseResult;
import com.academiq.entity.Evaluation;
import com.academiq.entity.Note;
import com.academiq.entity.Utilisateur;
import com.academiq.mapper.NoteMapper;
import com.academiq.security.IsAdmin;
import com.academiq.security.IsAdminOrResponsable;
import com.academiq.security.IsEnseignantOrAdmin;
import com.academiq.service.HistoriqueNoteService;
import com.academiq.service.ImportNotesService;
import com.academiq.service.NoteService;
import com.academiq.service.SaisieEnMasseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@Tag(name = "Notes & Évaluations", description = "Gestion des notes et évaluations")
public class NoteController {

    private final NoteService noteService;
    private final NoteMapper noteMapper;
    private final SaisieEnMasseService saisieEnMasseService;
    private final HistoriqueNoteService historiqueNoteService;
    private final ImportNotesService importNotesService;

    // ======================== Évaluations ========================

    @PostMapping("/evaluations")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<EvaluationResponse>> creerEvaluation(
            @Valid @RequestBody EvaluationRequest request) {
        Evaluation evaluation = noteService.createEvaluation(noteMapper.toEvaluation(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Évaluation créée avec succès", noteMapper.toEvaluationResponse(evaluation)));
    }

    @GetMapping("/evaluations/module/{moduleId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<List<EvaluationResponse>>> getEvaluationsParModule(
            @PathVariable Long moduleId) {
        return ResponseEntity.ok(ApiResponse.success(
                noteMapper.toEvaluationResponseList(noteService.getEvaluationsByModule(moduleId))));
    }

    @GetMapping("/evaluations/module/{moduleId}/promotion/{promotionId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<List<EvaluationResponse>>> getEvaluationsParModuleEtPromotion(
            @PathVariable Long moduleId, @PathVariable Long promotionId) {
        return ResponseEntity.ok(ApiResponse.success(
                noteMapper.toEvaluationResponseList(noteService.getEvaluationsByModuleAndPromotion(moduleId, promotionId))));
    }

    @GetMapping("/evaluations/{id}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<EvaluationResponse>> getEvaluation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                noteMapper.toEvaluationResponse(noteService.getEvaluationById(id))));
    }

    @PutMapping("/evaluations/{id}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<EvaluationResponse>> modifierEvaluation(
            @PathVariable Long id, @Valid @RequestBody EvaluationRequest request) {
        Evaluation data = noteMapper.toEvaluation(request);
        Evaluation updated = noteService.updateEvaluation(id, data);
        return ResponseEntity.ok(ApiResponse.success("Évaluation modifiée avec succès",
                noteMapper.toEvaluationResponse(updated)));
    }

    @DeleteMapping("/evaluations/{id}")
    @IsAdmin
    public ResponseEntity<ApiResponse<Void>> supprimerEvaluation(@PathVariable Long id) {
        noteService.deleteEvaluation(id);
        return ResponseEntity.ok(ApiResponse.success("Évaluation supprimée avec succès"));
    }

    @PatchMapping("/evaluations/{id}/terminer")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<Void>> terminerSaisie(@PathVariable Long id) {
        noteService.terminerSaisie(id);
        return ResponseEntity.ok(ApiResponse.success("Saisie terminée avec succès"));
    }

    // ======================== Notes ========================

    @PostMapping("/saisir")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<NoteResponse>> saisirNote(
            @Valid @RequestBody NoteSaisieRequest request,
            @AuthenticationPrincipal Utilisateur utilisateur) {
        Note note = noteService.saisirNote(
                request.getEvaluationId(), request.getEtudiantId(),
                request.getValeur(), request.isAbsent(),
                request.getCommentaire(), utilisateur.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note saisie avec succès", noteMapper.toNoteResponse(note)));
    }

    @PostMapping("/saisir-en-masse")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<List<NoteResponse>>> saisirNotesEnMasse(
            @Valid @RequestBody NoteSaisieEnMasseRequest request,
            @AuthenticationPrincipal Utilisateur utilisateur) {
        List<Note> notes = noteService.saisirNotesEnMasse(
                request.getEvaluationId(), request.getNotes(), utilisateur.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notes saisies avec succès", noteMapper.toNoteResponseList(notes)));
    }

    @GetMapping("/evaluation/{evaluationId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getNotesByEvaluation(
            @PathVariable Long evaluationId) {
        return ResponseEntity.ok(ApiResponse.success(
                noteMapper.toNoteResponseList(noteService.getNotesByEvaluation(evaluationId))));
    }

    @GetMapping("/etudiant/{etudiantId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getNotesByEtudiant(
            @PathVariable Long etudiantId) {
        return ResponseEntity.ok(ApiResponse.success(
                noteMapper.toNoteResponseList(noteService.getNotesByEtudiant(etudiantId))));
    }

    @GetMapping("/etudiant/{etudiantId}/module/{moduleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getNotesByEtudiantAndModule(
            @PathVariable Long etudiantId, @PathVariable Long moduleId) {
        return ResponseEntity.ok(ApiResponse.success(
                noteMapper.toNoteResponseList(noteService.getNotesByEtudiantAndModule(etudiantId, moduleId))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NoteResponse>> getNote(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                noteMapper.toNoteResponse(noteService.getNoteById(id))));
    }

    // ======================== Statistiques ========================

    @GetMapping("/evaluations/{id}/statistiques")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<StatistiquesEvaluationDTO>> getStatistiquesEvaluation(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                noteService.getStatistiquesEvaluation(id)));
    }

    // ======================== Verrouillage ========================

    @PatchMapping("/evaluations/{id}/verrouiller")
    @IsAdminOrResponsable
    public ResponseEntity<ApiResponse<Void>> verrouillerEvaluation(@PathVariable Long id) {
        noteService.verrouillerEvaluation(id);
        return ResponseEntity.ok(ApiResponse.success("Évaluation verrouillée avec succès"));
    }

    @PatchMapping("/evaluations/{id}/deverrouiller")
    @IsAdmin
    public ResponseEntity<ApiResponse<Void>> deverrouillerEvaluation(@PathVariable Long id) {
        noteService.deverrouillerEvaluation(id);
        return ResponseEntity.ok(ApiResponse.success("Évaluation déverrouillée avec succès"));
    }

    // ======================== Historique ========================

    @GetMapping("/{noteId}/historique")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<List<HistoriqueNoteResponse>>> getHistoriqueNote(
            @PathVariable Long noteId) {
        var historiques = historiqueNoteService.getHistoriqueByNote(noteId);
        List<HistoriqueNoteResponse> responses = historiques.stream()
                .map(h -> HistoriqueNoteResponse.builder()
                        .id(h.getId())
                        .ancienneValeur(h.getAncienneValeur())
                        .nouvelleValeur(h.getNouvelleValeur())
                        .ancienAbsent(h.isAncienAbsent())
                        .nouveauAbsent(h.isNouveauAbsent())
                        .motifModification(h.getMotifModification())
                        .modifieParNom(h.getModifiePar() != null
                                ? h.getModifiePar().getPrenom() + " " + h.getModifiePar().getNom()
                                : null)
                        .dateModification(h.getDateModification())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ======================== Saisie en masse améliorée ========================

    @GetMapping("/evaluations/{evaluationId}/preparer-saisie")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<List<NotePrepopuleeDTO>>> preparerSaisie(
            @PathVariable Long evaluationId) {
        return ResponseEntity.ok(ApiResponse.success(
                saisieEnMasseService.preparerSaisie(evaluationId)));
    }

    @PostMapping("/evaluations/{evaluationId}/saisie-classe")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<SaisieEnMasseResult>> saisirNotesClasse(
            @PathVariable Long evaluationId,
            @Valid @RequestBody List<com.academiq.dto.note.NoteSaisieDTO> notes,
            @AuthenticationPrincipal Utilisateur utilisateur) {
        SaisieEnMasseResult result = saisieEnMasseService.saisirNotesClasse(
                evaluationId, notes, utilisateur.getId());
        return ResponseEntity.ok(ApiResponse.success("Saisie en masse terminée", result));
    }

    // ======================== Récapitulatifs ========================

    @GetMapping("/modules/{moduleId}/promotion/{promotionId}/recapitulatif")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<RecapitulatifModuleDTO>> getRecapitulatifModule(
            @PathVariable Long moduleId, @PathVariable Long promotionId) {
        return ResponseEntity.ok(ApiResponse.success(
                noteService.getRecapitulatifModule(moduleId, promotionId)));
    }

    @GetMapping("/etudiant/{etudiantId}/promotion/{promotionId}/recapitulatif")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RecapitulatifEtudiantDTO>> getRecapitulatifEtudiant(
            @PathVariable Long etudiantId, @PathVariable Long promotionId) {
        return ResponseEntity.ok(ApiResponse.success(
                noteService.getRecapitulatifEtudiant(etudiantId, promotionId)));
    }

    // ======================== Import/Export Excel ========================

    @PostMapping("/evaluations/{evaluationId}/import-excel")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<SaisieEnMasseResult>> importerNotesExcel(
            @PathVariable Long evaluationId,
            @RequestParam("fichier") MultipartFile fichier,
            @AuthenticationPrincipal Utilisateur utilisateur) {
        SaisieEnMasseResult result = importNotesService.importerNotesExcel(
                evaluationId, fichier, utilisateur.getId());
        return ResponseEntity.ok(ApiResponse.success("Import Excel terminé", result));
    }

    @GetMapping("/evaluations/{evaluationId}/template-excel")
    @IsEnseignantOrAdmin
    public ResponseEntity<byte[]> telechargerTemplateExcel(@PathVariable Long evaluationId) throws IOException {
        Workbook workbook = importNotesService.genererTemplateExcel(evaluationId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "template_notes_" + evaluationId + ".xlsx");

        return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
    }
}
