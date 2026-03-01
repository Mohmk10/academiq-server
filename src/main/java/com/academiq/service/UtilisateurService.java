package com.academiq.service;

import com.academiq.entity.Role;
import com.academiq.entity.Utilisateur;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UtilisateurService {

    private static final Logger log = LoggerFactory.getLogger(UtilisateurService.class);

    private final UtilisateurRepository utilisateurRepository;

    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
    }

    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));
    }

    public Page<Utilisateur> findAll(Pageable pageable) {
        return utilisateurRepository.findAll(pageable);
    }

    public Page<Utilisateur> findByRole(Role role, Pageable pageable) {
        return utilisateurRepository.findByRole(role, pageable);
    }

    @Transactional
    public Utilisateur updateUtilisateur(Long id, Utilisateur updatedData) {
        Utilisateur utilisateur = findById(id);

        if (updatedData.getNom() != null) {
            utilisateur.setNom(updatedData.getNom());
        }
        if (updatedData.getPrenom() != null) {
            utilisateur.setPrenom(updatedData.getPrenom());
        }
        if (updatedData.getTelephone() != null) {
            utilisateur.setTelephone(updatedData.getTelephone());
        }
        if (updatedData.getDateNaissance() != null) {
            utilisateur.setDateNaissance(updatedData.getDateNaissance());
        }
        if (updatedData.getAdresse() != null) {
            utilisateur.setAdresse(updatedData.getAdresse());
        }
        if (updatedData.getPhotoProfil() != null) {
            utilisateur.setPhotoProfil(updatedData.getPhotoProfil());
        }

        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void toggleActivation(Long id) {
        Utilisateur utilisateur = findById(id);
        utilisateur.setActif(!utilisateur.isActif());
        utilisateurRepository.save(utilisateur);
        log.info("Utilisateur {} {}", id, utilisateur.isActif() ? "activé" : "désactivé");
    }

    @Transactional
    public void changeRole(Long id, Role newRole) {
        Utilisateur utilisateur = findById(id);
        utilisateur.setRole(newRole);
        utilisateurRepository.save(utilisateur);
        log.info("Rôle de l'utilisateur {} changé en {}", id, newRole);
    }

    public long countByRole(Role role) {
        return utilisateurRepository.countByRole(role);
    }

    public long countActifs() {
        return utilisateurRepository.countByActifTrue();
    }
}
