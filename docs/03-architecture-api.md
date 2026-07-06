# Étape 2 (suite) — Architecture de l'API Spring

Objectif : refondre l'API selon une architecture en couches cohérente, en respectant les principes SOLID, avec Hibernate comme couche de persistance.

## 1. Architecture en couches

```
controller  →  service (interface + impl)  →  repository (Spring Data JPA)  →  Hibernate  →  MySQL
                     ↓
                  mapper  ↔  dto
```

Package structure (`com.gamesUP.gamesUP`) :

- `model` : entités JPA (étape précédente)
- `repository` : interfaces `JpaRepository` / `JpaSpecificationExecutor`, + `repository.spec` pour les critères de recherche dynamiques
- `dto` : objets d'échange exposés par l'API (jamais l'entité elle-même), un sous-package par agrégat (`game`, `category`, `publisher`, `author`, `user`, `purchase`, `review`, `wishlist`)
- `mapper` : conversion entité ↔ DTO
- `service` (+ `service.impl`) : logique métier, une interface par service
- `controller` : endpoints REST, ne contiennent aucune logique métier
- `exception` : exceptions métier (`ResourceNotFoundException`, `BusinessRuleException`) + `GlobalExceptionHandler` (`@RestControllerAdvice`)
- `security` : abstraction `PasswordHasher` (voir §4)

## 2. Application des principes SOLID

- **S — Single Responsibility** : chaque contrôleur ne gère que le mapping HTTP, chaque service ne porte que la logique métier d'un agrégat, chaque mapper ne fait que de la conversion. `GameSpecifications` isole la construction des critères de recherche du reste du service.
- **O — Open/Closed** : les critères de recherche des jeux s'ajoutent en combinant des `Specification<Game>` indépendantes (`GameSpecifications`) sans modifier le repository ; l'abstraction `PasswordHasher` permet de faire évoluer la stratégie de hachage (étape 3) sans toucher à `UserService`.
- **L — Liskov Substitution** : les implémentations de service (`*ServiceImpl`) respectent entièrement le contrat de leur interface (mêmes pré/post-conditions), donc substituables (utile aussi pour les tests avec des mocks à l'étape 3).
- **I — Interface Segregation** : une interface de service par agrégat (`GameService`, `UserService`…) plutôt qu'une interface unique fourre-tout ; les contrôleurs ne dépendent que du service dont ils ont besoin.
- **D — Dependency Inversion** : les contrôleurs dépendent des interfaces `service.*`, jamais des implémentations ; Spring injecte les implémentations concrètes par constructeur (`@RequiredArgsConstructor`). Idem pour `PasswordHasher`.

## 3. DTOs et mapping

Aucune entité JPA n'est jamais exposée directement par un contrôleur (évite la sur-exposition de données comme le mot de passe, et découple le contrat d'API du schéma de base de données). Chaque agrégat a :
- un `XxxResponse` (record, sortie API),
- un ou plusieurs `XxxRequest` (record, entrée API, validés avec Bean Validation `@NotBlank`, `@NotNull`, `@Min`, `@Email`…).

Les mappers (`mapper/*Mapper`) sont des `@Component` Spring injectés dans les services ; ils ne font que de la conversion, sans accès base de données (la résolution des relations, ex. retrouver les `Category` correspondant aux `categoryIds` d'un jeu, reste une responsabilité du service).

## 4. Points de conception notables

- **Recherche de jeux** : `GameRepository` étend `JpaSpecificationExecutor`, et `GameServiceImpl.search(...)` combine dynamiquement les `Specification` correspondant aux critères fournis (nom, catégorie, éditeur, auteur, fourchette de prix, disponibilité), sans générer une explosion de méthodes `findByXxxAndYyy...` dans le repository.
- **Gestion des exceptions** : `GlobalExceptionHandler` centralise la traduction des exceptions métier en réponses HTTP cohérentes (404 pour `ResourceNotFoundException`, 409 pour `BusinessRuleException`, 400 pour les erreurs de validation), plutôt que de dupliquer des `try/catch` dans chaque contrôleur.
- **Commande (`Purchase`)** : la création d'une commande vérifie et décrémente le stock de chaque jeu dans la même transaction (`@Transactional`) que la création de la commande, pour éviter toute survente ; le prix est figé dans `PurchaseLine.unitPrice` au moment de l'achat (voir [docs/02-modele-donnees.md](02-modele-donnees.md)).
- **Mot de passe** : en l'absence de Spring Security à ce stade (prévu étape 3), `UserServiceImpl` dépend de l'interface `PasswordHasher` plutôt que d'un hachage concret. L'implémentation actuelle, `NoOpPasswordHasher`, est explicitement temporaire et sera remplacée par une implémentation `BCryptPasswordEncoder` à l'étape 3 sans changer `UserServiceImpl`.
- **Suppression d'un utilisateur** : refusée (`BusinessRuleException`) si l'utilisateur a des commandes, pour préserver l'intégrité de l'historique des ventes.

## 5. Endpoints exposés

| Ressource | Endpoints |
|---|---|
| Catégories | `GET/POST /api/categories`, `GET/PUT/DELETE /api/categories/{id}` |
| Éditeurs | `GET/POST /api/publishers`, `GET/PUT/DELETE /api/publishers/{id}` |
| Auteurs | `GET/POST /api/authors`, `GET/PUT/DELETE /api/authors/{id}` |
| Jeux | `GET /api/games` (recherche : `name`, `categoryId`, `publisherId`, `authorId`, `minPrice`, `maxPrice`, `inStock`), `GET/PUT/DELETE /api/games/{id}`, `POST /api/games` |
| Utilisateurs | `POST /api/users/register`, `GET /api/users`, `GET/PUT/DELETE /api/users/{id}`, `PATCH /api/users/{id}/role` |
| Commandes | `GET/POST /api/users/{userId}/purchases`, `GET /api/purchases`, `GET /api/purchases/{id}`, `PATCH /api/purchases/{id}/status` |
| Avis | `GET /api/games/{gameId}/reviews`, `POST /api/users/{userId}/reviews`, `PUT/DELETE /api/reviews/{id}` |
| Wishlist | `GET /api/users/{userId}/wishlist`, `POST /api/users/{userId}/wishlist/items`, `DELETE /api/users/{userId}/wishlist/items/{gameId}` |
| Authentification (étape 3) | `POST /api/auth/login` |
| Recommandation (étape 4) | `GET /api/users/{userId}/recommendations` |

Le contrôle d'accès par rôle (client/admin) et par propriétaire est désormais en place via Spring Security — voir [docs/04-securite-tests.md](04-securite-tests.md).

Le projet compile intégralement (`./mvnw compile`).
