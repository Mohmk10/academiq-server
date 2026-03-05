package com.academiq.controller;

import com.academiq.dto.ApiResponse;
import com.academiq.security.IsSuperAdmin;
import com.academiq.service.DatabaseResetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Opérations d'administration système")
public class AdminController {

    private final DatabaseResetService databaseResetService;

    @PostMapping("/reset-database")
    @IsSuperAdmin
    public ResponseEntity<ApiResponse<Void>> resetDatabase(@RequestParam("confirm") String confirm) {
        if (!"RESET_ALL".equals(confirm)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Confirmation invalide. Envoyez confirm=RESET_ALL"));
        }

        databaseResetService.resetDatabase();
        return ResponseEntity.ok(ApiResponse.success("Base de données réinitialisée avec succès"));
    }
}
