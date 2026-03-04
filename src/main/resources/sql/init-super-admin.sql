-- ============================================================
-- INITIALISATION SUPER ADMINISTRATEUR
-- A executer UNE SEULE FOIS sur la base AcademiQ
-- Mot de passe : SuperAdmin@2026
-- ============================================================

-- Migration : ajouter SUPER_ADMIN a la contrainte de role
-- (necessaire si la table a ete creee avant l'ajout du role SUPER_ADMIN)
ALTER TABLE utilisateurs DROP CONSTRAINT IF EXISTS utilisateurs_role_check;
ALTER TABLE utilisateurs ADD CONSTRAINT utilisateurs_role_check
    CHECK (role::text = ANY (ARRAY[
        'SUPER_ADMIN'::character varying,
        'ADMIN'::character varying,
        'RESPONSABLE_PEDAGOGIQUE'::character varying,
        'ENSEIGNANT'::character varying,
        'ETUDIANT'::character varying
    ]::text[]));

-- Creation du SUPER_ADMIN (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM utilisateurs WHERE email = 'superadmin@academiq.sn') THEN

        INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, actif, telephone, created_at, updated_at)
        VALUES (
            'Kouyaté',
            'Makan',
            'superadmin@academiq.sn',
            '$2a$10$DeXY1/TaBcsI1dq9F.Iww.NLW.5VYML1i1v.wmos0M.Z8IXeXb/6O',
            'SUPER_ADMIN',
            true,
            '+221781975048',
            NOW(),
            NOW()
        );

        INSERT INTO admins (utilisateur_id, fonction, departement, niveau, created_at, updated_at)
        VALUES (
            (SELECT id FROM utilisateurs WHERE email = 'superadmin@academiq.sn'),
            'Super Administrateur',
            'Direction Générale',
            'SUPER_ADMIN',
            NOW(),
            NOW()
        );

        RAISE NOTICE 'SUPER_ADMIN cree avec succes : superadmin@academiq.sn / SuperAdmin@2026';
    ELSE
        RAISE NOTICE 'SUPER_ADMIN existe deja, aucune action effectuee';
    END IF;
END $$;
