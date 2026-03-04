package com.academiq.config;

import com.academiq.entity.Admin;
import com.academiq.entity.NiveauAdmin;
import com.academiq.entity.Role;
import com.academiq.entity.Utilisateur;
import com.academiq.repository.AdminRepository;
import com.academiq.repository.UtilisateurRepository;
import com.academiq.service.RegleAlerteService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RegleAlerteService regleAlerteService;
    private final UtilisateurRepository utilisateurRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        creerSuperAdminSiAbsent();
        regleAlerteService.initialiserReglesParDefaut();
        log.info("Initialisation des données terminée");
    }

    private void creerSuperAdminSiAbsent() {
        if (utilisateurRepository.existsByEmail("superadmin@academiq.sn")) {
            log.info("SUPER_ADMIN déjà existant, aucune action");
            return;
        }

        Utilisateur superAdmin = Utilisateur.builder()
                .nom("Kouyaté")
                .prenom("Makan")
                .email("superadmin@academiq.sn")
                .motDePasse(passwordEncoder.encode("SuperAdmin@2026"))
                .role(Role.SUPER_ADMIN)
                .actif(true)
                .telephone("+221781975048")
                .build();

        utilisateurRepository.save(superAdmin);

        Admin adminProfile = Admin.builder()
                .utilisateur(superAdmin)
                .fonction("Super Administrateur")
                .departement("Direction Générale")
                .niveau(NiveauAdmin.SUPER_ADMIN)
                .build();

        adminRepository.save(adminProfile);

        log.info("========================================");
        log.info("SUPER_ADMIN créé : superadmin@academiq.sn");
        log.info("Mot de passe : SuperAdmin@2026");
        log.info("========================================");
    }
}
