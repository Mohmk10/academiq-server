-- ============================================================
-- RESET COMPLET DE LA BASE AcademiQ
-- Supprime TOUTES les données SAUF le SUPER_ADMIN
-- Ordre de suppression : enfants d'abord (respect des FK)
-- ============================================================

-- 1. Données de notes et historique
DELETE FROM historique_notes;
DELETE FROM notes;
DELETE FROM evaluations;

-- 2. Alertes
DELETE FROM alertes;

-- 3. Relations
DELETE FROM affectations;
DELETE FROM inscriptions;

-- 4. Structure académique
DELETE FROM modules_formation;
DELETE FROM unites_enseignement;
DELETE FROM semestres;
DELETE FROM promotions;
DELETE FROM niveaux;
DELETE FROM filieres;

-- 5. Profils (sauf SUPER_ADMIN)
DELETE FROM etudiants;
DELETE FROM enseignants;
DELETE FROM admins WHERE utilisateur_id != (SELECT id FROM utilisateurs WHERE email = 'superadmin@academiq.sn');

-- 6. Règles d'alerte (seront recréées par le DataInitializer)
DELETE FROM regles_alerte;

-- 7. Utilisateurs (sauf SUPER_ADMIN)
DELETE FROM utilisateurs WHERE email != 'superadmin@academiq.sn';

-- Verification
DO $$
DECLARE
    nb_users INTEGER;
BEGIN
    SELECT COUNT(*) INTO nb_users FROM utilisateurs;
    RAISE NOTICE 'Reset termine. Utilisateurs restants : %', nb_users;
END $$;
