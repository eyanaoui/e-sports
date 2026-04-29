# Esports Management System — Module Forum (Services avancés)

Ce README documente **uniquement le module Forum** et explique les **services avancés Java** ajoutés (sans API externe), ainsi que **comment ils sont utilisés** dans l’application (controllers/FXML/DAO).

---

## Vue globale : “chaîne intelligente” du Forum

Dans ce module, tout fonctionne comme une **chaîne connectée** :

1. **Publication d’un message**
   - Le message passe par **anti‑spam/modération** (règles : longueur, répétition, liens, etc.).
   - Il reçoit un statut : **ACCEPTED / PENDING / REJECTED**.

2. **Si ACCEPTED**
   - Le message est enregistré.
   - Le sujet est automatiquement mis à jour :
     - **replies_count**
     - **trending_score**
     - **status** du sujet (**ACTIVE / HOT / INACTIVE** selon activité récente)

3. **Enrichissement automatique**
   - Un **résumé** peut être généré à partir des messages.
   - Des **mots‑clés** sont extraits.
   - Ces informations s’affichent dans l’interface (user + admin).

4. **Côté Admin**
   - Dashboard pro : sujets regroupés avec leurs messages, actions de modération directes.
   - Statistiques avancées + top trending + top users + activity feed + reports.

5. **Export PDF**
   - Export d’un sujet en PDF (titre, messages, résumé, keywords, lien/QR, etc.).
   - Export d’un **rapport admin** PDF (stats globales + top sujets + top users + reports).

---

## Architecture (structure actuelle utilisée)

Le projet conserve la structure existante et enrichit :

- **Models** : `com.esports.models.*`
- **DAO** : `com.esports.dao.*`
- **Services** : `com.esports.services.*`
- **Controllers** : `com.esports.controllers.user.*`, `com.esports.controllers.admin.*`
- **FXML** : `src/main/resources/views/user/*`, `src/main/resources/views/admin/*`

---

## Migrations SQL (MySQL/MariaDB compatibles)

Ces scripts sont **idempotents** (ils vérifient l’existence des colonnes via `information_schema`).

- `src/main/resources/db/forum_advanced_migration.sql`
  - Colonnes avancées forum (trending, status, summary, keywords, etc.) et modération message.

- `src/main/resources/db/forum_social_features_migration.sql`
  - Like/Dislike, Best Answer, pièces jointes, historique, notifications, activity, score.

- `src/main/resources/db/forum_intelligence_migration.sql`
  - Réputation, badges, favoris, reports, archivage.

---

## Services avancés (ce qui a été ajouté)

### 1) Modération / Anti‑spam
- **Service** : `com.esports.services.ForumModerationService`
- **Rôle** :
  - Analyse le contenu d’un message.
  - Détermine `ACCEPTED / PENDING / REJECTED`.
  - Côté admin : `approveMessage(...)`, `rejectMessage(...)`.
- **Utilisation** :
  - Côté user : `MessageController.handleSend()`
  - Côté admin : `AdminForumController` (actions Approve/Reject sur mini‑cards)

### 2) Trending + activité sujet
- **Service** : `com.esports.services.ForumAdvancedService`
- **Rôle** :
  - `calculateTrendingScore(sujetId)` selon la formule :
    - score = nbMessages * 3 + messages24h * 5
  - Déduit le status **HOT/ACTIVE/INACTIVE**.
  - `updateTopicActivity(sujetId)` après ajout message.
- **Utilisation** :
  - `ForumController.refreshForum()` (recalcul à l’affichage)
  - `MessageController.handleSend()` (update après ajout)

### 3) Résumé automatique
- **Service** : `com.esports.services.ForumAdvancedService.generateSummary(sujetId)`
- **Rôle** : résumé simple en français à partir des messages `ACCEPTED`.
- **Utilisation** :
  - User : bouton “Generate Summary” (popup)
  - Admin : bouton “Generate Summary” sur une card sujet

### 4) Mots‑clés (keywords)
- **Service** : `com.esports.services.ForumKeywordService`
- **Rôle** :
  - extrait 5–8 mots importants (stop‑words ignorés)
  - sauvegarde dans `keywords`
- **Utilisation** :
  - Admin : “Generate Keywords”
  - User : affichage dans la carte sujet

### 5) Export PDF Sujet
- **Service** : `com.esports.services.ForumPdfExportService`
- **Librairie** : OpenPDF (`com.github.librepdf:openpdf`)
- **Utilisation** :
  - User : bouton “Export PDF” dans `MessageView`
  - Admin : “Export PDF” par sujet

### 6) QR Code (local, sans API externe)
- **Librairie** : ZXing (`com.google.zxing:core`)
- **Utilisation** :
  - `ForumController` : bouton QR Code sur la card sujet
  - Le QR encode un lien Google basé sur le titre du sujet.

### 7) Like / Dislike
- **Service** : `com.esports.services.ForumVoteService`
- **Rôle** :
  - incrémente `likes/dislikes`
  - met à jour la réputation (like reçu)
- **Utilisation** :
  - User : boutons 👍 / 👎 dans les messages (`MessageController`)
  - Admin : actions Like/Dislike sur mini‑cards message

### 8) Best Answer
- **Service** : `com.esports.services.ForumBestAnswerService`
- **Rôle** :
  - un seul message `is_best=1` par sujet
  - met à jour score/réputation si possible
- **Utilisation** :
  - User : “Mark as Best Answer”
  - Admin : “Mark Best Answer”

### 9) Historique des modifications
- **Service** : `com.esports.services.ForumMessageHistoryService`
- **Rôle** :
  - avant update, stocke old/new dans `message_history`
  - affiche l’historique en popup
- **Utilisation** :
  - User : “Voir historique”
  - Admin : “View History”

### 10) Pin / Épingler sujet
- **Service** : `com.esports.services.ForumPinService`
- **Rôle** :
  - `is_pinned` => les sujets apparaissent en haut
- **Utilisation** :
  - User : Pin/Unpin sur card
  - Admin : Pin/Unpin sur card

### 11) Notifications internes
- **Service** : `com.esports.services.ForumNotificationService`
- **Rôle** :
  - crée des notifications (réponse, best answer, etc.)
  - compteur + popup + mark as read
- **Utilisation** :
  - User : bouton 🔔 + compteur dans `ForumView`

### 12) Pièces jointes
- **Service** : `com.esports.services.ForumAttachmentService`
- **Rôle** :
  - validation (png/jpg/jpeg/pdf/txt, max 5MB)
  - copie vers `uploads/forum/`
  - sauvegarde `file_path`
- **Utilisation** :
  - User : bouton “Attach File” dans `MessageView`
  - affichage “Open File” si attaché

### 13) Pagination / Load More
- **Service** : `com.esports.services.ForumPaginationService`
- **Rôle** : charge 10 messages par page
- **Utilisation** :
  - User : bouton “Load More”

### 14) Activity feed
- **Service** : `com.esports.services.ForumActivityService`
- **Rôle** : journalise actions (create topic, pin, approve, best answer, export…)
- **Utilisation** :
  - User : “Recent Forum Activity”
  - Admin : “Recent Activity”

### 15) Détection doublon de sujet
- **Service** : `com.esports.services.ForumDuplicateDetectionService`
- **Rôle** : similarité mots communs, alerte si >= 60%
- **Utilisation** :
  - `ForumController.handleSaveTopic()` (confirmation continuer/annuler)

### 16) Recommandation de sujets similaires
- **Service** : `com.esports.services.ForumRecommendationService`
- **Rôle** :
  - compare titre + contenu + messages
  - propose 3 sujets similaires
- **Utilisation** :
  - User : section “Sujets similaires” dans `MessageView`

### 17) Réputation utilisateur + niveaux
- **Service** : `com.esports.services.ForumReputationService`
- **DAO** : `com.esports.dao.ForumUserReputationDAO`
- **Règles** :
  - +5 message accepté
  - +10 best answer
  - +1 like reçu
  - -3 message rejeté
- **Niveaux** :
  - BRONZE < 50, SILVER >= 50, GOLD >= 100, DIAMOND >= 200
- **Utilisation** :
  - branché dans `MessageController` sur accept/reject/best/like
  - Admin : top users affiche niveau + score

### 18) Badges utilisateur
- **Service** : `com.esports.services.ForumBadgeService`
- **DAO** : `com.esports.dao.ForumUserBadgeDAO`
- **Règles** :
  - Débutant (1 msg), Actif (10), Expert (50), Helper (3 best), Popular (20 likes reçus)
- **Utilisation** :
  - évalué automatiquement lors des updates réputation (`ForumReputationService`)

### 19) Archivage automatique / manuel
- **Service** : `com.esports.services.ForumArchiveService`
- **Règle** :
  - inactif 30 jours => status `ARCHIVED` + `archived_at` + `archive_reason`
- **Utilisation** :
  - Admin : boutons Archive / Restore sur card sujet

### 20) Favoris
- **Service** : `com.esports.services.ForumFavoriteService`
- **DAO** : `com.esports.dao.ForumFavoriteTopicDAO`
- **Utilisation** :
  - User : bouton Favorite/Unfavorite sur card sujet
  - User : section “Mes sujets favoris”

### 21) Signalement (Reports)
- **Service** : `com.esports.services.ForumReportService`
- **DAO** : `com.esports.dao.ForumReportDAO`
- **Rôle** :
  - crée une ligne `forum_report`
  - incrémente `messageforum.report_count`
- **Utilisation** :
  - User : bouton “Report” sur chaque message (popup raison + description)
  - Admin : “Reports Pending” dans dashboard

### 22) Rapport PDF Admin
- **Service** : `com.esports.services.ForumAdminPdfReportService`
- **Rôle** :
  - stats globales
  - top sujets actifs
  - top users (réputation)
  - reports pending
  - + phrase de rapport fournie
- **Utilisation** :
  - Admin : bouton “Export Report PDF”

---

## Où sont branchées les features (fichiers)

- **Forum user**
  - `src/main/resources/views/user/ForumView.fxml`
  - `src/main/java/com/esports/controllers/user/ForumController.java`

- **Détail sujet / messages**
  - `src/main/resources/views/user/MessageView.fxml`
  - `src/main/java/com/esports/controllers/user/MessageController.java`

- **Forum admin dashboard**
  - `src/main/resources/views/admin/admin-forum.fxml`
  - `src/main/java/com/esports/controllers/admin/AdminForumController.java`
  - Style: `src/main/resources/forum-admin.css`

---

## Tests manuels (scénario démo)

1. **Modération**
   - créer un message suspect => PENDING/REJECTED
   - admin => Approve/Reject, stats mises à jour

2. **Trending + status**
   - ajouter plusieurs messages => score augmente => status HOT

3. **Summary / Keywords**
   - Generate Summary => popup + saved en DB
   - Generate Keywords => affiché sur card

4. **Like/Dislike + Best Answer**
   - Like => compteur augmente + réputation auteur
   - Mark Best Answer => badge + score

5. **Historique**
   - modifier un message => View History (user/admin)

6. **Favoris**
   - favorite/unfavorite => “Mes sujets favoris”

7. **Reports**
   - Report message => apparaît dans “Reports Pending”

8. **Archivage**
   - Archive/Restore côté admin

9. **PDF**
   - Export topic PDF (user/admin)
   - Export admin report PDF

---

## Note “userId”

Si l’authentification n’est pas totalement branchée, le projet utilise temporairement :

- `currentUserId = 1`

---

## Phrase pour rapport (à réutiliser)

“Ces services avancés enrichissent le module Forum en le rendant plus intelligent, interactif et administrable. Le système recommande automatiquement des sujets similaires, détecte les doublons lors de la création d’un sujet, récompense les utilisateurs grâce à un score de réputation et des badges, permet de sauvegarder des sujets favoris, offre un mécanisme de signalement des messages, archive automatiquement les sujets inactifs et génère un rapport PDF complet pour l’administrateur.”

