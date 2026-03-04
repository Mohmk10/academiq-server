-- ============================================================
-- RESET COMPLET DE LA BASE AcademiQ
-- Supprime TOUTES les donn\u00e9es (le DataInitializer recr\u00e9e le SUPER_ADMIN)
-- Ordre de suppression : enfants d'abord (respect des FK)
-- ============================================================

-- 1. Audit logs
DELETE FROM audit_logs;

-- 2. Donn\u00e9es de notes et historique
DELETE FROM historique_notes;
DELETE FROM notes;
DELETE FROM evaluations;

-- 3. Alertes
DELETE FROM alertes;

-- 4. Relations
DELETE FROM affectations;
DELETE FROM inscriptions;

-- 5. Structure acad\u00e9mique
DELETE FROM modules_formation;
DELETE FROM unites_enseignement;
DELETE FROM semestres;
DELETE FROM promotions;
DELETE FROM niveaux;
DELETE FROM filieres;

-- 6. Profils
DELETE FROM etudiants;
DELETE FROM enseignants;
DELETE FROM admins;

-- 7. R\u00e8gles d'alerte (recr\u00e9\u00e9es par le DataInitializer)
DELETE FROM regles_alerte;

-- 8. Utilisateurs (le DataInitializer recr\u00e9era le SUPER_ADMIN)
DELETE FROM utilisateurs;

-- V\u00e9rification
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT table_name,
               (xpath('/row/cnt/text()', query_to_xml(format('SELECT count(*) AS cnt FROM %I', table_name), false, true, '')))[1]::text::int AS cnt
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
        ORDER BY table_name
    ) LOOP
        RAISE NOTICE '% : % lignes', r.table_name, r.cnt;
    END LOOP;
END $$;
