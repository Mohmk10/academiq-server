-- ============================================================
-- SCRIPT D'INITIALISATION — SUPER ADMIN
-- À exécuter UNE SEULE FOIS lors du premier déploiement
-- ============================================================

-- Le mot de passe est : SuperAdmin@2026
-- Hashé avec BCrypt (strength 10)
-- IMPORTANT : Remplacer $BCRYPT_HASH par le vrai hash généré
-- via le PasswordHashGenerator au démarrage en profil dev

INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, telephone, created_at, updated_at)
VALUES (
    'Kouyaté',
    'Makan',
    'superadmin@academiq.sn',
    '$2a$10$PLACEHOLDER_HASH_TO_REPLACE',
    'SUPER_ADMIN',
    true,
    '+221781975048',
    NOW(),
    NOW()
);

-- Créer le profil Admin associé
INSERT INTO admins (utilisateur_id, fonction, departement, niveau, created_at, updated_at)
VALUES (
    (SELECT id FROM utilisateurs WHERE email = 'superadmin@academiq.sn'),
    'Directeur Général',
    'Direction',
    'SUPER_ADMIN',
    NOW(),
    NOW()
);
