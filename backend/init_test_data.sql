-- ================================================
-- Script d'initialisation des données de test
-- Reclamation App - Base de données MySQL
-- ================================================

USE reclamation;

-- ================================================
-- 1. CRÉATION DES RÔLES
-- ================================================

-- Supprimer les rôles existants (optionnel, pour tests)
-- DELETE FROM hab_users_roles;
-- DELETE FROM hab_role WHERE id IN (1,2,3,4,5);

-- Créer les rôles de base
INSERT INTO hab_role (id, cod_role, lib_role) VALUES 
(1, 'ADMIN', 'Administrateur'),
(2, 'USER', 'Utilisateur'),
(3, 'AGENT', 'Agent'),
(4, 'RESPONSABLE', 'Responsable'),
(5, 'CLIENT', 'Client')
ON DUPLICATE KEY UPDATE 
  cod_role = VALUES(cod_role),
  lib_role = VALUES(lib_role);

-- ================================================
-- 2. CRÉATION DES STATUTS DE RÉCLAMATION
-- ================================================

INSERT INTO rec_statut (cod_sta, lib_sta, msg_sta, msg2_sta) VALUES
('NOUVEAU', 'Nouveau', 'Votre réclamation a été enregistrée', 'En attente de traitement'),
('ENCOURS', 'En cours', 'Votre réclamation est en cours de traitement', 'Un agent s''en occupe'),
('TRAITE', 'Traité', 'Votre réclamation a été traitée', 'Réponse envoyée'),
('CLOTURE', 'Clôturé', 'Votre réclamation est clôturée', 'Dossier fermé'),
('REJETE', 'Rejeté', 'Votre réclamation a été rejetée', 'Non recevable')
ON DUPLICATE KEY UPDATE 
  lib_sta = VALUES(lib_sta),
  msg_sta = VALUES(msg_sta),
  msg2_sta = VALUES(msg2_sta);

-- ================================================
-- 3. CRÉATION DES CANAUX DE COMMUNICATION
-- ================================================

INSERT INTO rec_canal (lib_can) VALUES
('Email'),
('Téléphone'),
('Guichet'),
('Site Web'),
('Application Mobile'),
('Courrier');

-- ================================================
-- 4. CRÉATION DES NATURES DE RÉCLAMANTS
-- ================================================

INSERT INTO rec_nature_client (cod_nat, lib_nat) VALUES
('PART', 'Particulier'),
('ENT', 'Entreprise'),
('ASSOC', 'Association'),
('ADMIN', 'Administration');

-- ================================================
-- 5. CRÉATION DES SERVICES
-- ================================================

INSERT INTO rec_service (cod_ser, lib_ser, email1_ser, email2_ser) VALUES
('SRV001', 'Service Commercial', 'commercial@reclamation.tn', 'commercial2@reclamation.tn'),
('SRV002', 'Service Technique', 'technique@reclamation.tn', 'technique2@reclamation.tn'),
('SRV003', 'Service Clientèle', 'clientele@reclamation.tn', 'clientele2@reclamation.tn'),
('SRV004', 'Service Facturation', 'facturation@reclamation.tn', 'facturation2@reclamation.tn'),
('SRV005', 'Service Réclamations', 'reclamations@reclamation.tn', 'reclamations2@reclamation.tn')
ON DUPLICATE KEY UPDATE 
  lib_ser = VALUES(lib_ser),
  email1_ser = VALUES(email1_ser),
  email2_ser = VALUES(email2_ser);

-- ================================================
-- 6. CRÉATION DES OBJETS DE RÉCLAMATION
-- ================================================

INSERT INTO rec_objet (cod_obj, lib_obj, id_cod_ser) VALUES
('OBJ001', 'Problème de facturation', 'SRV004'),
('OBJ002', 'Panne technique', 'SRV002'),
('OBJ003', 'Qualité de service', 'SRV003'),
('OBJ004', 'Délai de livraison', 'SRV001'),
('OBJ005', 'Information produit', 'SRV003'),
('OBJ006', 'Remboursement', 'SRV004'),
('OBJ007', 'Contrat', 'SRV001'),
('OBJ008', 'Installation', 'SRV002')
ON DUPLICATE KEY UPDATE 
  lib_obj = VALUES(lib_obj),
  id_cod_ser = VALUES(id_cod_ser);

-- ================================================
-- 7. CRÉATION DES DOMAINES (pour les modules)
-- ================================================

INSERT INTO hab_domaine (cod_dom, lib_mod) VALUES
('DOM001', 'Gestion des Réclamations'),
('DOM002', 'Administration Système'),
('DOM003', 'Reporting et Statistiques')
ON DUPLICATE KEY UPDATE 
  lib_mod = VALUES(lib_mod);

-- ================================================
-- 8. CRÉATION DES MODULES
-- ================================================

INSERT INTO hab_module (cod_mod, lib_mod, sta_mod, cod_dom) VALUES
('MOD001', 'Réclamations', 'ACTIF', 'DOM001'),
('MOD002', 'Utilisateurs', 'ACTIF', 'DOM002'),
('MOD003', 'Statistiques', 'ACTIF', 'DOM003'),
('MOD004', 'Paramètres', 'ACTIF', 'DOM002')
ON DUPLICATE KEY UPDATE 
  lib_mod = VALUES(lib_mod),
  sta_mod = VALUES(sta_mod),
  cod_dom = VALUES(cod_dom);

-- ================================================
-- 9. CRÉATION DES PARAMÈTRES GÉNÉRAUX
-- ================================================

INSERT INTO rec_param_gen (lib_param, val_param) VALUES
('AGE_JEUNE', '25'),
('AGE_INTERMEDIATE', '45'),
('AGE_MIDDLE', '60'),
('DELAI_REPONSE_JOURS', '5'),
('MAX_TENTATIVES_LOGIN', '3')
ON DUPLICATE KEY UPDATE 
  val_param = VALUES(val_param);

-- ================================================
-- VÉRIFICATION DES DONNÉES
-- ================================================

-- Compter les rôles
SELECT COUNT(*) AS 'Nombre de Rôles' FROM hab_role;

-- Compter les statuts
SELECT COUNT(*) AS 'Nombre de Statuts' FROM rec_statut;

-- Compter les canaux
SELECT COUNT(*) AS 'Nombre de Canaux' FROM rec_canal;

-- Compter les services
SELECT COUNT(*) AS 'Nombre de Services' FROM rec_service;

-- Compter les objets
SELECT COUNT(*) AS 'Nombre d''Objets' FROM rec_objet;

-- Afficher les rôles créés
SELECT * FROM hab_role ORDER BY id;

-- Afficher les statuts créés
SELECT * FROM rec_statut ORDER BY cod_sta;

-- ================================================
-- NOTES IMPORTANTES
-- ================================================

/*
APRÈS L'EXÉCUTION DE CE SCRIPT:

1. Utilisez Postman pour créer votre premier utilisateur ADMIN via:
   POST http://localhost:8080/api/auth/signup
   
   Body JSON:
   {
     "id": "admin001",
     "firstName": "Admin",
     "lastName": "System",
     "email": "admin@reclamation.tn",
     "password": "Admin@123",
     "roles": ["ADMIN"]
   }

2. Puis connectez-vous pour obtenir le JWT token:
   POST http://localhost:8080/api/auth/login
   
   Body JSON:
   {
     "email": "admin@reclamation.tn",
     "password": "Admin@123"
   }

3. Le token sera automatiquement sauvegardé si vous utilisez 
   le script de test Postman fourni.

4. Tous les autres endpoints nécessitent ce token JWT dans le header:
   Authorization: Bearer <votre_token_jwt>

DÉPANNAGE:

- Si vous obtenez "Role not found", vérifiez que les rôles sont bien insérés:
  SELECT * FROM hab_role;

- Si vous obtenez 401 sur /api/auth/**, vérifiez que l'application 
  Spring Boot est bien démarrée et que le AuthTokenFilter contient 
  la méthode shouldNotFilter().

- Pour réinitialiser un compte bloqué:
  UPDATE hab_users SET disabled = 0, nbcnx = 0 WHERE email = 'votre@email.com';

*/
