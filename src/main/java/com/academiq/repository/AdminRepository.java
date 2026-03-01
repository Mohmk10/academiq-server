package com.academiq.repository;

import com.academiq.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUtilisateurId(Long utilisateurId);

    Optional<Admin> findByUtilisateurEmail(String email);
}
