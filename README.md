# GamesUP — Étude de cas

Ce document sert de journal de bord pour la reprise de l'API GamesUP : il explique la démarche suivie étape par étape, les décisions prises et pourquoi. Il servira de base à la documentation finale demandée (point 5 de l'énoncé).

Contexte : la plateforme de vente de jeux de société GamesUP doit voir son back-end repris de zéro. Un stagiaire a fourni une base d'API Spring (`ANNEXES/gamesUP`) de mauvaise qualité, à reprendre plutôt qu'à jeter. Une API Python de recommandation (`ANNEXES/CodeApiPython`) est également à compléter.

Déroulé du travail, en 5 étapes :
1. Analyse des fonctionnalités
2. Refonte de l'API Spring (architecture + Hibernate)
3. Sécurisation et tests
4. Système de recommandation (KNN via FastAPI)
5. Documentation finale (diagrammes, SOLID, couverture de tests, ML)

---

## Étape 1 — Analyse des fonctionnalités

Aucun cahier des charges formel n'a été fourni. Le point de départ a donc été une analyse croisée :
- le **scénario/brief** de l'énoncé (notions de client, jeu, éditeur, auteur, commande, recommandation) ;
- le **code Java existant** du stagiaire (`ANNEXES/gamesUP/src/main/java/com/gamesUP/gamesUP/model` et `controller`), qui révèle les entités déjà envisagées et leurs champs, même incomplets ;
- l'**API Python** fournie (`ANNEXES/CodeApiPython`), qui indique le format de données attendu pour les recommandations (`user_id`, `game_id`, `rating`).

### Entités du domaine identifiées

| Entité | Présente dans le code repris | Constat |
|---|---|---|
| Utilisateur (`User`) | Oui, très minimal (`id`, `nom`) | Manque email, mot de passe, rôle (client/admin), adresse |
| Jeu (`Game`) | Oui | `auteur` est une `String` alors qu'une entité `Author` existe séparément ; `genre` (String) fait doublon avec `category` (objet) |
| Auteur (`Author`) | Oui | Relation avec `Game` définie côté auteur uniquement |
| Éditeur (`Publisher`) | Oui | Juste un `name`, pas de lien inverse vers ses jeux |
| Catégorie (`Category`) | Oui | Un seul champ `type` |
| Commande (`Purchase`) | Oui | Pas de lien vers l'utilisateur qui commande, pas de total |
| Ligne de commande (`PurchaseLine`) | Oui | Pas de quantité, juste jeu + prix |
| Avis (`Avis`) | Oui | Pas de lien vers l'utilisateur ni vers le jeu concerné |
| Stock (`Inventory`) | Oui | `HashMap<Game, Integer>` — non persistable tel quel avec Hibernate |
| Liste de souhaits (`Wishlist`) | Oui (classe vide) | À définir entièrement |

### Comptes et rôles

**Client** (utilisateur authentifié) :
- consulter le catalogue et rechercher des jeux
- gérer son profil (inscription, connexion, modification)
- passer une commande, consulter son historique de commandes
- gérer sa wishlist
- laisser un avis sur un jeu
- recevoir des recommandations personnalisées

**Administrateur** : tout ce que fait le client, plus :
- gérer le catalogue (CRUD jeux, auteurs, éditeurs, catégories)
- gérer le stock
- consulter/gérer la liste de tous les clients
- consulter/gérer toutes les commandes (statut : payée, livrée, archivée)
- modérer les avis

### CRUD attendus par entité

| Entité | Client | Admin |
|---|---|---|
| Jeu | Lecture + recherche | CRUD complet |
| Auteur / Éditeur / Catégorie | Lecture | CRUD complet |
| Utilisateur | Lecture/modif de son propre profil | CRUD complet |
| Commande | Créer, lire les siennes | Lire/modifier toutes |
| Avis | CRUD sur les siens | Modération (lecture/suppression) |
| Wishlist | CRUD sur la sienne | — |
| Stock | Lecture (disponibilité) | CRUD complet |

### Recherche de jeux

Critères retenus : nom (texte partiel), auteur, éditeur, catégorie/genre, fourchette de prix, disponibilité en stock — avec tri (prix, nom, popularité) et pagination.

### Faiblesses relevées dans le code repris 

- `GameController` exécute des requêtes JDBC brutes avec les identifiants MySQL en dur dans le code (faille de sécurité), alors que `spring-boot-starter-data-jpa` est déjà présent en dépendance mais inutilisé.
- Aucune annotation JPA (`@Entity`, `@Id`...), pas de repository, pas de service : tout est mélangé dans le contrôleur.
- Champs publics non encapsulés, incohérences de modélisation (`Game.auteur` en String vs entité `Author`, `genre` vs `Category`).
- Relations manquantes ou incomplètes (commande sans utilisateur, avis sans jeu/utilisateur, pas de quantité en ligne de commande).
- Pas de DTOs, pas de gestion des rôles, `spring-security` présent uniquement en dépendance de test (commenté dans les dépendances principales du `pom.xml`).

Cette analyse sert de base à la refonte du modèle de données (Hibernate/JPA) et à la définition des endpoints REST en étape 2.

---

## Étape 2 — Refonte de l'API Spring

### Modèle relationnel et entités

Le modèle de données a été repensé avec une démarche Merise (MCD → MLD → modèle objet JPA/Hibernate), à partir des entités déjà esquissées par le stagiaire. Toutes les entités (`User`, `Game`, `Category`, `Publisher`, `Author`, `Purchase`, `PurchaseLine`, `Review`, `Wishlist`) sont maintenant des `@Entity` JPA correctement reliées entre elles — fini le JDBC brut et les objets non persistables. Au passage, plusieurs incohérences du code repris ont été corrigées : `Game.auteur`/`genre` faisaient doublon, `Inventory` n'était pas persistable, `Purchase` n'avait ni utilisateur ni statut cohérent, `Avis`/`Wishlist` n'avaient aucune relation.

La `Wishlist` a d'ailleurs été corrigée une seconde fois après relecture : c'est bien une seule wishlist par utilisateur, contenant plusieurs `WishlistItem` (même principe que `Purchase`/`PurchaseLine`), et pas une ligne par couple utilisateur/jeu comme dans la première version. `GameController`, qui faisait du JDBC brut avec les identifiants MySQL en dur, a été supprimé et remplacé par l'architecture en couches ci-dessous.

Détail complet, schéma relationnel et diagramme de classes : [docs/02-modele-donnees.md](docs/02-modele-donnees.md).

### Architecture en couches

`GameController` a été remplacé par une architecture complète : `controller → service (interface + impl) → repository (Spring Data JPA) → Hibernate`, avec des DTOs (records), des mappers, une gestion centralisée des exceptions (`GlobalExceptionHandler`) et une recherche de jeux basée sur des `Specification` dynamiques. Chaque agrégat a son repository Spring Data JPA, `GameRepository` passant par `JpaSpecificationExecutor` pour la recherche multi-critères ; les services suivent le même principe, une interface par agrégat plus son implémentation, pour respecter l'inversion de dépendance et la ségrégation d'interfaces.

Quelques règles métier ont été posées à ce stade : décrément de stock atomique à la commande, prix figé sur la ligne de commande, impossibilité de supprimer un utilisateur qui a des commandes. `PasswordHasher` a aussi été introduit ici comme abstraction, avant même que Spring Security existe, pour être implémentée à l'étape 3 par `BCryptPasswordHasher` sans toucher à `UserService`.

Détail complet (structure des packages, principes SOLID, liste des endpoints) : [docs/03-architecture-api.md](docs/03-architecture-api.md). Rien à signaler comme reste à faire : CRUD, DTOs et recherche de jeux sont en place.

## Étape 3 — Sécurisation et tests

Spring Security (JWT stateless) et une suite de tests complète sont en place. L'authentification passe par `POST /api/auth/login` et le filtre `JwtAuthenticationFilter`, mot de passe haché en BCrypt. L'autorisation combine des règles par rôle (`hasRole('ADMIN')`) et par propriétaire (`#userId == principal.id or hasRole('ADMIN')`, avec un bean dédié `reviewSecurity` pour les avis), le tout via `@PreAuthorize`. Les erreurs d'authentification et d'autorisation renvoient du JSON cohérent (401/403) grâce à `RestAuthenticationEntryPoint` et `RestAccessDeniedHandler`, qui réutilisent le format `ApiError` déjà en place.

À l'époque, cette étape s'est terminée sur 74 tests (53 unitaires sur les services, 20 d'intégration `MockMvc` + H2, plus le test de contexte) et une couverture JaCoCo de 86,2 %. ⚠️ Un défaut de configuration Maven découvert à l'étape 4 rendait en fait l'exécution des tests `*IT` peu fiable — ces chiffres sont donc à relire à la lumière du correctif décrit plus bas.

Détail complet : [docs/04-securite-tests.md](docs/04-securite-tests.md).

## Étape 4 — Système de recommandation

Le modèle KNN (filtrage collaboratif) est implémenté côté API Python, et l'API Spring lui envoie l'historique d'achats/avis de l'utilisateur pour récupérer des recommandations. Côté Python (`ANNEXES/CodeApiPython`), `recommendation.py` porte l'entraînement (`train_model`, `sklearn.neighbors.NearestNeighbors`, persistance via `joblib`) et l'inférence (`generate_recommendations`), avec `train.py` comme script d'entraînement séparé de l'API. Tant qu'aucune donnée réelle n'est disponible, l'API renvoie une liste de recommandations de test, conformément à la consigne — `requirements.txt` et un `README.md` détaillant le format de données attendu ont été ajoutés au passage.

Côté Spring, la chaîne est `RecommendationController` (`GET /api/users/{userId}/recommendations`) → `RecommendationService`, qui reconstruit l'historique utilisateur à partir des achats et des avis, → `RecommendationClient`, qui fait l'appel HTTP réel et traduit les erreurs réseau en 503. La réponse est enrichie avec les données du catalogue local quand le jeu recommandé y existe. Les tests ne couvrent que la partie Spring (consigne oblige) : `RecommendationServiceImplTest` en unitaire, `RecommendationControllerIT` en intégration avec `RecommendationClient` simulé.

C'est en ajoutant ces tests qu'un défaut de fiabilité est apparu : Maven Surefire n'incluait pas les tests `*IT` de façon déterministe. Corrigé explicitement dans `pom.xml` — après quoi on obtient de façon reproductible **81 tests, 0 échec, couverture JaCoCo 85,1 %**.

Détail complet : [docs/05-systeme-recommandation.md](docs/05-systeme-recommandation.md).

## Étape 5 — Documentation finale

Document de synthèse complet — diagrammes d'architecture, de classes, de composants, de séquence, principes SOLID, rapport de couverture, synthèse du système de recommandation, et réflexion sur la démarche de travail : [docs/06-documentation-finale.md](docs/06-documentation-finale.md).
