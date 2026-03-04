package com.academiq.controller;

import com.academiq.dto.ApiResponse;
import com.academiq.dto.structure.FiliereRequest;
import com.academiq.dto.structure.FiliereResponse;
import com.academiq.dto.structure.ModuleRequest;
import com.academiq.dto.structure.ModuleResponse;
import com.academiq.dto.structure.NiveauRequest;
import com.academiq.dto.structure.NiveauResponse;
import com.academiq.dto.structure.PromotionRequest;
import com.academiq.dto.structure.PromotionResponse;
import com.academiq.dto.structure.SemestreRequest;
import com.academiq.dto.structure.SemestreResponse;
import com.academiq.dto.structure.UeRequest;
import com.academiq.dto.structure.UeResponse;
import com.academiq.entity.Filiere;
import com.academiq.entity.ModuleFormation;
import com.academiq.entity.Niveau;
import com.academiq.entity.Promotion;
import com.academiq.entity.Semestre;
import com.academiq.entity.UniteEnseignement;
import com.academiq.mapper.StructureMapper;
import com.academiq.security.IsAdmin;
import com.academiq.security.IsAllExceptEtudiant;
import com.academiq.security.IsEnseignantOrAdmin;
import com.academiq.service.SecurityService;
import com.academiq.service.StructureAcademiqueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/structure")
@RequiredArgsConstructor
@Tag(name = "Structure Académique", description = "Gestion de la structure académique")
public class StructureAcademiqueController {

    private final StructureAcademiqueService structureService;
    private final StructureMapper structureMapper;
    private final SecurityService securityService;

    // ======================== Filières ========================

    @PostMapping("/filieres")
    @IsAdmin
    public ResponseEntity<ApiResponse<FiliereResponse>> creerFiliere(@Valid @RequestBody FiliereRequest request) {
        Filiere filiere = structureService.createFiliere(structureMapper.toFiliere(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Filière créée avec succès", structureMapper.toFiliereResponse(filiere)));
    }

    @GetMapping("/filieres")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<List<FiliereResponse>>> listerFilieres() {
        List<FiliereResponse> filieres = structureMapper.toFiliereResponseList(structureService.getAllFilieres());
        return ResponseEntity.ok(ApiResponse.success(filieres));
    }

    @GetMapping("/filieres/{id}")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<FiliereResponse>> getFiliere(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(structureMapper.toFiliereResponse(structureService.getFiliereById(id))));
    }

    @PutMapping("/filieres/{id}")
    @IsAdmin
    public ResponseEntity<ApiResponse<FiliereResponse>> modifierFiliere(@PathVariable Long id, @Valid @RequestBody FiliereRequest request) {
        Filiere data = structureMapper.toFiliere(request);
        Filiere updated = structureService.updateFiliere(id, data);
        return ResponseEntity.ok(ApiResponse.success("Filière modifiée avec succès", structureMapper.toFiliereResponse(updated)));
    }

    @DeleteMapping("/filieres/{id}")
    @IsAdmin
    public ResponseEntity<ApiResponse<Void>> supprimerFiliere(@PathVariable Long id) {
        structureService.deleteFiliere(id);
        return ResponseEntity.ok(ApiResponse.success("Filière supprimée avec succès"));
    }

    // ======================== Niveaux ========================

    @PostMapping("/filieres/{filiereId}/niveaux")
    @IsAdmin
    public ResponseEntity<ApiResponse<NiveauResponse>> creerNiveau(@PathVariable Long filiereId, @Valid @RequestBody NiveauRequest request) {
        Niveau niveau = structureService.createNiveau(filiereId, structureMapper.toNiveau(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Niveau créé avec succès", structureMapper.toNiveauResponse(niveau)));
    }

    @GetMapping("/filieres/{filiereId}/niveaux")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<List<NiveauResponse>>> listerNiveaux(@PathVariable Long filiereId) {
        List<NiveauResponse> niveaux = structureMapper.toNiveauResponseList(structureService.getNiveauxByFiliere(filiereId));
        return ResponseEntity.ok(ApiResponse.success(niveaux));
    }

    @GetMapping("/niveaux/{id}")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<NiveauResponse>> getNiveau(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(structureMapper.toNiveauResponse(structureService.getNiveauById(id))));
    }

    // ======================== Promotions ========================

    @PostMapping("/niveaux/{niveauId}/promotions")
    @IsAdmin
    public ResponseEntity<ApiResponse<PromotionResponse>> creerPromotion(@PathVariable Long niveauId, @Valid @RequestBody PromotionRequest request) {
        Promotion promotion = structureService.createPromotion(niveauId, structureMapper.toPromotion(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Promotion créée avec succès", structureMapper.toPromotionResponse(promotion)));
    }

    @GetMapping("/niveaux/{niveauId}/promotions")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> listerPromotions(@PathVariable Long niveauId) {
        List<PromotionResponse> promotions = structureMapper.toPromotionResponseList(structureService.getPromotionsByNiveau(niveauId));
        return ResponseEntity.ok(ApiResponse.success(promotions));
    }

    @GetMapping("/promotions")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> listerPromotionsActives() {
        List<PromotionResponse> promotions = structureMapper.toPromotionResponseList(structureService.getPromotionsActives());
        return ResponseEntity.ok(ApiResponse.success(promotions));
    }

    @GetMapping("/promotions/{id}")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<PromotionResponse>> getPromotion(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(structureMapper.toPromotionResponse(structureService.getPromotionById(id))));
    }

    @PutMapping("/promotions/{id}")
    @IsAdmin
    public ResponseEntity<ApiResponse<PromotionResponse>> modifierPromotion(@PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
        Promotion data = structureMapper.toPromotion(request);
        Promotion updated = structureService.updatePromotion(id, data);
        return ResponseEntity.ok(ApiResponse.success("Promotion modifiée avec succès", structureMapper.toPromotionResponse(updated)));
    }

    // ======================== Semestres ========================

    @PostMapping("/niveaux/{niveauId}/semestres")
    @IsAdmin
    public ResponseEntity<ApiResponse<SemestreResponse>> creerSemestre(@PathVariable Long niveauId, @Valid @RequestBody SemestreRequest request) {
        Semestre semestre = structureService.createSemestre(niveauId, structureMapper.toSemestre(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Semestre créé avec succès", structureMapper.toSemestreResponse(semestre)));
    }

    @GetMapping("/niveaux/{niveauId}/semestres")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<List<SemestreResponse>>> listerSemestres(@PathVariable Long niveauId) {
        List<SemestreResponse> semestres = structureMapper.toSemestreResponseList(structureService.getSemestresByNiveau(niveauId));
        return ResponseEntity.ok(ApiResponse.success(semestres));
    }

    // ======================== UEs ========================

    @PostMapping("/semestres/{semestreId}/ues")
    @IsAdmin
    public ResponseEntity<ApiResponse<UeResponse>> creerUe(@PathVariable Long semestreId, @Valid @RequestBody UeRequest request) {
        UniteEnseignement ue = structureService.createUe(semestreId, structureMapper.toUe(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("UE créée avec succès", structureMapper.toUeResponse(ue)));
    }

    @GetMapping("/semestres/{semestreId}/ues")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<List<UeResponse>>> listerUes(@PathVariable Long semestreId) {
        List<UeResponse> ues = structureMapper.toUeResponseList(structureService.getUesBySemestre(semestreId));
        return ResponseEntity.ok(ApiResponse.success(ues));
    }

    @GetMapping("/ues/{id}")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<UeResponse>> getUe(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(structureMapper.toUeResponse(structureService.getUeById(id))));
    }

    @PutMapping("/ues/{id}")
    @IsAdmin
    public ResponseEntity<ApiResponse<UeResponse>> modifierUe(@PathVariable Long id, @Valid @RequestBody UeRequest request) {
        UniteEnseignement data = structureMapper.toUe(request);
        UniteEnseignement updated = structureService.updateUe(id, data);
        return ResponseEntity.ok(ApiResponse.success("UE modifiée avec succès", structureMapper.toUeResponse(updated)));
    }

    @DeleteMapping("/ues/{id}")
    @IsAdmin
    public ResponseEntity<ApiResponse<Void>> supprimerUe(@PathVariable Long id) {
        structureService.deleteUe(id);
        return ResponseEntity.ok(ApiResponse.success("UE supprimée avec succès"));
    }

    // ======================== Modules ========================

    @PostMapping("/ues/{ueId}/modules")
    @IsAdmin
    public ResponseEntity<ApiResponse<ModuleResponse>> creerModule(@PathVariable Long ueId, @Valid @RequestBody ModuleRequest request) {
        ModuleFormation module = structureService.createModule(ueId, structureMapper.toModule(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Module créé avec succès", structureMapper.toModuleResponse(module)));
    }

    @GetMapping("/ues/{ueId}/modules")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<List<ModuleResponse>>> listerModules(@PathVariable Long ueId) {
        List<ModuleResponse> modules = structureMapper.toModuleResponseList(structureService.getModulesByUe(ueId));
        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    @GetMapping("/modules/{id}")
    @IsAllExceptEtudiant
    public ResponseEntity<ApiResponse<ModuleResponse>> getModule(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(structureMapper.toModuleResponse(structureService.getModuleById(id))));
    }

    @GetMapping("/modules/enseignant/{enseignantId}")
    @IsEnseignantOrAdmin
    public ResponseEntity<ApiResponse<List<ModuleResponse>>> getModulesParEnseignant(@PathVariable Long enseignantId) {
        securityService.verifierAccesEnseignant(enseignantId);
        List<ModuleResponse> modules = structureMapper.toModuleResponseList(structureService.getModulesByEnseignant(enseignantId));
        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    @PutMapping("/modules/{id}")
    @IsAdmin
    public ResponseEntity<ApiResponse<ModuleResponse>> modifierModule(@PathVariable Long id, @Valid @RequestBody ModuleRequest request) {
        ModuleFormation data = structureMapper.toModule(request);
        ModuleFormation updated = structureService.updateModule(id, data);
        return ResponseEntity.ok(ApiResponse.success("Module modifié avec succès", structureMapper.toModuleResponse(updated)));
    }

    @DeleteMapping("/modules/{id}")
    @IsAdmin
    public ResponseEntity<ApiResponse<Void>> supprimerModule(@PathVariable Long id) {
        structureService.deleteModule(id);
        return ResponseEntity.ok(ApiResponse.success("Module supprimé avec succès"));
    }
}
