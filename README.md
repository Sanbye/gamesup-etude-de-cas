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

Le modèle de données a été repensé avec une démarche Merise (MCD → MLD → modèle objet JPA/Hibernate), à partir des entités déjà esquissées par le stagiaire. Détail complet, schéma relationnel et diagramme de classes : [docs/02-modele-donnees.md](docs/02-modele-donnees.md).

Points clés :
- Toutes les entités (`User`, `Game`, `Category`, `Publisher`, `Author`, `Purchase`, `PurchaseLine`, `Review`, `Wishlist`) sont désormais des `@Entity` JPA correctement liées entre elles (fini le JDBC brut et les objets non persistables).
- Correction des incohérences du code repris : `Game.auteur`/`genre` en doublon, `Inventory` non persistable, `Purchase` sans utilisateur ni statut cohérent, `Avis`/`Wishlist` sans relations.
- `Wishlist` corrigée une deuxième fois suite à une relecture : une seule wishlist par utilisateur, contenant plusieurs `WishlistItem` (même principe que `Purchase`/`PurchaseLine`), au lieu d'une ligne par (utilisateur, jeu).
- `GameController` (JDBC brut, identifiants MySQL en dur) a été supprimé : il a été remplacé par une architecture en couches complète (voir ci-dessous).

### Architecture en couches 

`GameController` a été remplacé par une architecture complète : `controller → service (interface + impl) → repository (Spring Data JPA) → Hibernate`, avec DTOs (records), mappers, gestion centralisée des exceptions (`GlobalExceptionHandler`) et recherche de jeux via `Specification` dynamiques. Détail complet (structure des packages, application des principes SOLID, liste des endpoints) : [docs/03-architecture-api.md](docs/03-architecture-api.md).

Points clés :
- Repositories Spring Data JPA pour chaque agrégat, `GameRepository` en `JpaSpecificationExecutor` pour la recherche multi-critères.
- Services métier avec interface + implémentation (Dependency Inversion), une interface par agrégat (Interface Segregation).
- DTOs (records) systématiques en entrée/sortie d'API : aucune entité JPA n'est exposée directement.
- Règles métier implémentées : décrément de stock atomique à la commande, prix figé sur la ligne de commande, blocage de la suppression d'un utilisateur ayant des commandes.
- `PasswordHasher` : abstraction posée à l'étape 2, implémentée à l'étape 3 par `BCryptPasswordHasher` sans modifier `UserService` (principe ouvert/fermé).

### Reste à faire pour cette étape

- Rien : le CRUD de base, les DTOs et la recherche de jeux sont en place.

## Étape 3 — Sécurisation et tests

Spring Security (JWT stateless) et une suite de tests complète sont en place. Détail complet : [docs/04-securite-tests.md](docs/04-securite-tests.md).

Points clés :
- Authentification par JWT : `POST /api/auth/login`, filtre `JwtAuthenticationFilter`, mot de passe haché en BCrypt (`BCryptPasswordHasher` remplace le temporaire de l'étape 2).
- Autorisation par rôle (`hasRole('ADMIN')`) et par propriétaire (`#userId == principal.id or hasRole('ADMIN')`, bean dédié `reviewSecurity` pour les avis) via `@PreAuthorize`.
- Réponses d'erreur JSON cohérentes (401/403) via `RestAuthenticationEntryPoint` / `RestAccessDeniedHandler`, réutilisant le format `ApiError` existant.
- 74 tests à l'origine (53 unitaires sur les services avec Mockito, 20 d'intégration avec `MockMvc` + H2 couvrant les flux d'authentification et les règles d'autorisation de bout en bout, plus le test de chargement de contexte). Couverture JaCoCo mesurée à l'époque : 86,2 %.
- ⚠️ Un défaut de configuration Maven découvert à l'étape 4 rendait l'exécution des tests d'intégration (`*IT`) non fiable (voir étape 4) ; les chiffres ci-dessus doivent être relus à la lumière du correctif.

## Étape 4 — Système de recommandation

Le modèle KNN (filtrage collaboratif) est implémenté côté API Python, et l'API Spring lui envoie l'historique d'achats/avis de l'utilisateur pour récupérer des recommandations. Détail complet : [docs/05-systeme-recommandation.md](docs/05-systeme-recommandation.md).

Points clés :
- **API Python** (`ANNEXES/CodeApiPython`) : `recommendation.py` implémente l'entraînement (`train_model`, `sklearn.neighbors.NearestNeighbors`, persistance via `joblib`) et l'inférence (`generate_recommendations`) ; `train.py` est le script d'entraînement, séparé de l'API. Tant qu'aucune donnée réelle n'est disponible, l'API renvoie une liste de recommandations de test (conforme à la consigne). Ajout de `requirements.txt` et d'un `README.md` détaillant le format de données attendu.
- **Spring → Python** : `RecommendationController` (`GET /api/users/{userId}/recommendations`) → `RecommendationService` (reconstruit l'historique utilisateur à partir des achats/avis) → `RecommendationClient` (appel HTTP via `RestClient`, erreurs réseau traduites en 503). Réponse enrichie avec les données du catalogue local quand le jeu recommandé y existe.
- **Tests** : uniquement côté Spring (conformément à la consigne de ne pas tester l'API Python) — `RecommendationServiceImplTest` (unitaire) et `RecommendationControllerIT` (intégration, `RecommendationClient` simulé).
- **Correctif de fiabilité** : l'inclusion des tests `*IT` par Maven Surefire n'était pas déterministe (non documentée par défaut pour ce plugin) ; désormais fixée explicitement dans `pom.xml`. Après ce correctif : **81 tests, 0 échec, couverture JaCoCo 85,2 %** (reproductible sur plusieurs `./mvnw clean test` consécutifs).

## Étape 5 — Documentation finale

Document de synthèse complet (diagrammes d'architecture, de classes, de composants, de séquence, principes SOLID, rapport de couverture, synthèse du système de recommandation, réflexion sur la démarche de travail) : [docs/06-documentation-finale.md](docs/06-documentation-finale.md).
