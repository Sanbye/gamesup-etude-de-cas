# Étape 3 — Sécurité et tests

## 1. Authentification

L'API expose une authentification par JWT (stateless), adaptée à une API REST consommée par le front Angular séparé :

- `POST /api/auth/login` : reçoit `{ email, password }`, authentifie via `AuthenticationManager` (délègue à `CustomUserDetailsService` + `BCryptPasswordEncoder`), renvoie `{ token, tokenType: "Bearer" }`.
- Chaque requête suivante doit porter l'en-tête `Authorization: Bearer <token>`.
- `JwtAuthenticationFilter` (un `OncePerRequestFilter` inséré avant `UsernamePasswordAuthenticationFilter`) valide le token à chaque requête, recharge l'utilisateur via `CustomUserDetailsService` et peuple le `SecurityContext`.
- Le token embarque l'email (`subject`), l'identifiant utilisateur (`userId`) et le rôle (`role`) ; il est signé HMAC-SHA (clé dans `app.jwt.secret`, expiration dans `app.jwt.expiration-ms`, tous deux surchargeables par variables d'environnement `JWT_SECRET` / `JWT_EXPIRATION_MS`).
- Un changement de rôle (`PATCH /api/users/{id}/role`) n'est reflété qu'après une nouvelle connexion, puisque le rôle est figé dans le token au moment du login (comportement stateless assumé, à documenter côté front).

Pas de session HTTP (`SessionCreationPolicy.STATELESS`), CSRF désactivé (non pertinent pour une API sans session/cookie), CORS restreint à l'origine du front Angular (`app.cors.allowed-origins`, par défaut `http://localhost:4200`).

## 2. Rôles et autorisation

Deux rôles, comme défini à l'étape 1 : `CLIENT` et `ADMIN` (enum `Role`, porté par `User.role`).

L'autorisation est appliquée à deux niveaux :

- **Au niveau `SecurityFilterChain`** (`SecurityConfig`) : les routes publiques (`/api/auth/**`, `/api/users/register`, et les `GET` sur `/api/games`, `/api/categories`, `/api/publishers`, `/api/authors`) sont en `permitAll()` ; tout le reste exige d'être authentifié.
- **Au niveau méthode** (`@EnableMethodSecurity` + `@PreAuthorize`) pour les règles fines : rôle requis, ou propriété de la ressource.

| Règle | Où | Exemple |
|---|---|---|
| Rôle ADMIN requis | Mutations catalogue (jeux, catégories, éditeurs, auteurs), gestion des utilisateurs, liste/statut des commandes | `@PreAuthorize("hasRole('ADMIN')")` |
| Self-ou-admin | Consultation/modification de son propre profil, ses commandes, sa wishlist, création d'un avis | `@PreAuthorize("#userId == principal.id or hasRole('ADMIN')")` |
| Propriétaire-ou-admin (sans `userId` dans l'URL) | Modification/suppression d'un avis (`/api/reviews/{id}`) | `@PreAuthorize("hasRole('ADMIN') or @reviewSecurity.isOwner(#id, principal.id)")`, `reviewSecurity` étant un petit bean dédié qui va chercher le propriétaire réel de l'avis en base |

`principal` désigne `CustomUserDetails` (implémentation de `UserDetails` portée par l'entité `User`), qui expose `getId()` utilisable directement dans les expressions SpEL.

Les erreurs d'authentification (token absent/invalide) renvoient 401 via `RestAuthenticationEntryPoint`, les erreurs d'autorisation (rôle/propriétaire incorrect) renvoient 403 via `RestAccessDeniedHandler` — tous deux au format JSON `ApiError` déjà utilisé par `GlobalExceptionHandler`, pour une cohérence des réponses d'erreur sur toute l'API.

## 3. Mot de passe

`BCryptPasswordHasher` (implémentation de l'abstraction `PasswordHasher` posée à l'étape 2) délègue à `PasswordEncoder` (`BCryptPasswordEncoder`, bean déclaré dans `SecurityConfig`). `UserServiceImpl` n'a pas eu besoin d'être modifié pour ce changement — c'est la démonstration concrète du principe ouvert/fermé évoqué à l'étape 2.

## 4. Stratégie de tests

### Tests unitaires (`service.impl`, Mockito, sans contexte Spring)

Un test par service métier (`CategoryServiceImplTest`, `PublisherServiceImplTest`, `AuthorServiceImplTest`, `GameServiceImplTest`, `UserServiceImplTest`, `PurchaseServiceImplTest`, `ReviewServiceImplTest`, `WishlistServiceImplTest`), couvrant :
- les cas nominaux (création, mise à jour, suppression) ;
- les cas d'erreur (ressource introuvable → `ResourceNotFoundException`, règle métier violée → `BusinessRuleException`) ;
- les règles métier sensibles : décrément de stock et calcul du montant total à la commande, unicité d'un jeu dans une wishlist, blocage de suppression d'un utilisateur ayant des commandes, hachage du mot de passe et création automatique de la wishlist à l'inscription.

### Tests d'intégration (`controller`, `@SpringBootTest` + `MockMvc`, base H2 en mémoire)

Une base `AbstractIntegrationTest` factorise l'obtention de tokens (inscription + connexion, ou promotion admin directe en base suivie d'une reconnexion). Les tests couvrent le comportement de bout en bout, filtre JWT et règles `@PreAuthorize` inclus :
- `AuthControllerIT` : inscription puis connexion, email déjà utilisé (409), mauvais mot de passe (401), payload invalide (400).
- `GameControllerIT` : recherche publique sans authentification, création refusée sans token (401) puis en tant que client (403), cycle CRUD complet en tant qu'admin.
- `UserControllerIT` : accès à la liste des utilisateurs réservé à l'admin, consultation de son propre profil vs profil d'un autre client (403).
- `PurchaseControllerIT` : décrément de stock et calcul du total à la commande, conflit si stock insuffisant (409), consultation des commandes d'autrui refusée (403).
- `ReviewControllerIT` : lecture publique des avis, suppression réservée au propriétaire ou à l'admin.
- `WishlistControllerIT` : ajout/consultation de sa propre wishlist, accès à la wishlist d'un autre utilisateur refusé (403).

### Configuration de test

`src/test/resources/application.properties` bascule le datasource sur H2 en mémoire (`ddl-auto=create-drop`) pour ne dépendre d'aucune instance MySQL locale, tout en gardant le même schéma (mode de compatibilité MySQL). Chaque test d'intégration s'exécute dans une transaction annulée en fin de test (`@Transactional`), pour l'isolation entre tests sans avoir à nettoyer manuellement les données.

## 5. Couverture de code

Mesurée avec JaCoCo (`org.jacoco:jacoco-maven-plugin`, rapport généré à chaque `./mvnw test` dans `target/site/jacoco/index.html`) :

| Portée | Couverture (instructions) |
|---|---|
| **Globale** | **86,2 %** (objectif : 70 %) |
| `service.impl` (logique métier) | 88,1 % |
| `security` / `security.jwt` | 95–98 % |
| `exception` | 89,6 % |
| `mapper`, `model`, `dto.*` | 100 % |
| `controller` | 51,7 % |
| `repository.spec` | 42,5 % |

Les points les plus bas (`controller`, `repository.spec`) s'expliquent par le fait que certains contrôleurs CRUD simples (catégories, éditeurs, auteurs) et certains critères de recherche (`hasAuthor`, `inStock`) ne sont exercés qu'indirectement (via la préparation des données d'autres tests, ou pas du tout), leur logique équivalente étant déjà entièrement couverte au niveau service. La couverture globale dépasse largement le seuil demandé.

Pour reproduire : `./mvnw test` puis ouvrir `ANNEXES/gamesUP/target/site/jacoco/index.html`.
