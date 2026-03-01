package com.academiq.service;

import com.academiq.dto.auth.AuthResponse;
import com.academiq.dto.auth.LoginRequest;
import com.academiq.dto.auth.RefreshTokenRequest;
import com.academiq.dto.auth.RegisterRequest;
import com.academiq.entity.Role;
import com.academiq.entity.Utilisateur;
import com.academiq.exception.DuplicateResourceException;
import com.academiq.exception.ResourceNotFoundException;
import com.academiq.exception.UnauthorizedException;
import com.academiq.repository.UtilisateurRepository;
import com.academiq.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
        );

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", request.getEmail()));

        if (!utilisateur.isActif()) {
            throw new UnauthorizedException("Compte désactivé");
        }

        utilisateur.setDernierLogin(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        return buildAuthResponse(utilisateur);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Utilisateur", "email", request.getEmail());
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .role(Role.ETUDIANT)
                .actif(true)
                .telephone(request.getTelephone())
                .dateNaissance(request.getDateNaissance())
                .adresse(request.getAdresse())
                .dernierLogin(LocalDateTime.now())
                .build();

        utilisateurRepository.save(utilisateur);

        return buildAuthResponse(utilisateur);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.getRefreshToken());

        Utilisateur utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", username));

        if (!jwtService.isTokenValid(request.getRefreshToken(), utilisateur)) {
            throw new UnauthorizedException("Token de rafraîchissement invalide");
        }

        String accessToken = jwtService.generateAccessToken(utilisateur);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(request.getRefreshToken())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole().name())
                .build();
    }

    private AuthResponse buildAuthResponse(Utilisateur utilisateur) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(utilisateur))
                .refreshToken(jwtService.generateRefreshToken(utilisateur))
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole().name())
                .build();
    }
}
