# Étape 4 — Système de recommandation

## 1. Données nécessaires

Un modèle KNN de filtrage collaboratif (recommander à un utilisateur les jeux appréciés par des utilisateurs qui lui ressemblent) a besoin d'une matrice **utilisateur x jeu** : une ligne par utilisateur, une colonne par jeu, la valeur étant la note de cet utilisateur pour ce jeu.

Trois informations suffisent donc, pour chaque interaction :

| Donnée | Origine côté Spring |
|---|---|
| `user_id` | `User.id` |
| `game_id` | `Game.id` |
| `rating` (1 à 5) | `Review.rating` si l'utilisateur a noté le jeu ; sinon note implicite par défaut (achat sans avis) |

Plus il y a d'utilisateurs ayant noté un nombre significatif de jeux communs, plus les recommandations sont pertinentes. Le cas des nouveaux utilisateurs/jeux sans historique ("cold start") n'est pas traité pour l'instant — il faudrait un modèle hybride (contenu : catégorie, éditeur, prix) en complément, hors du périmètre demandé ici.

## 2. Algorithme (API Python)

L'algorithme est un **KNN utilisateur-utilisateur** (`sklearn.neighbors.NearestNeighbors`, similarité cosinus) :

- `data_loader.py` : `load_training_data` charge un CSV (`user_id,game_id,rating`) ; `build_user_item_matrix` le transforme en matrice utilisateur x jeu (`pandas.pivot_table`, valeurs manquantes à 0).
- `recommendation.py` :
  - `train_model(csv_path)` : construit la matrice, entraîne `NearestNeighbors`, persiste modèle + matrice + index utilisateurs/jeux via `joblib` dans `models/knn_model.joblib`. Fonction indépendante de l'API, destinée à être rejouée (`train.py`) quand des données réelles seront disponibles.
  - `generate_recommendations(user_data)` : si aucun modèle n'a encore été entraîné (`models/knn_model.joblib` absent — cas actuel, faute de données réelles exploitables), renvoie une liste de test fixe. Sinon, transforme les achats/notes reçus en vecteur aligné sur la matrice d'entraînement, cherche les *k* utilisateurs les plus proches (`model.kneighbors`), et agrège les jeux qu'ils ont notés ≥ 4 et que l'utilisateur ne possède pas encore, pondérés par similarité.

Ce découpage répond explicitement à la contrainte de l'énoncé : implémenter l'algorithme dès maintenant, même si l'entraînement réel se fera plus tard une fois des données exploitables collectées.

## 3. Intégration Spring → Python

```
Client REST → RecommendationController → RecommendationService → RecommendationClient → API Python (POST /recommendations/)
```

- `RecommendationServiceImpl` reconstruit l'historique de l'utilisateur à partir de `PurchaseRepository` (jeux achetés) et `ReviewRepository` (notes explicites, qui priment sur la note implicite par défaut `app.recommendation.api.default-rating`), puis délègue l'appel HTTP à `RecommendationClient`.
- `RecommendationClient` (interface) / `RecommendationClientImpl` : appelle l'API Python via un bean `RestClient` (configuré dans `RecommendationClientConfig`, URL de base `app.recommendation.api.base-url`, timeout `app.recommendation.api.timeout-ms`). Toute erreur réseau est traduite en `RecommendationServiceUnavailableException` → HTTP 503, géré par `GlobalExceptionHandler`.
- La réponse Python (`game_id`, `game_name` optionnel, `score` optionnel) est enrichie côté Spring : si le `game_id` renvoyé existe dans le catalogue local, son nom officiel (`Game.name`) remplace celui fourni par Python (qui n'a pas accès au catalogue).
- Endpoint exposé : `GET /api/users/{userId}/recommendations`, protégé comme les autres ressources personnelles (`#userId == principal.id or hasRole('ADMIN')`).

Cette séparation respecte le principe de responsabilité unique (le client HTTP ne connaît que le protocole, le service ne connaît que la logique métier) et l'inversion de dépendance (`RecommendationService` dépend de l'interface `RecommendationClient`, pas de `RestClient` directement).

## 4. Tests

Conformément à la consigne (ne pas tester l'API Python), seule l'intégration côté Spring est testée :
- `RecommendationServiceImplTest` (unitaire) : construction de la requête envoyée à l'API Python à partir des achats/avis, enrichissement de la réponse avec les données du catalogue local.
- `RecommendationControllerIT` (intégration, `MockMvc` + H2) : `RecommendationClient` est simulé (`@MockBean`) pour ne pas dépendre d'une instance FastAPI réellement démarrée ; vérifie l'authentification, l'autorisation self-ou-admin, et le format de réponse.

## 5. Correction annexe : fiabilité de la suite de tests

En ajoutant ces nouveaux tests, une non-déterminisme est apparue dans l'exécution de `mvn test` : les classes `*IT` (tests d'intégration) n'étaient pas incluses de façon fiable par Maven Surefire, qui ne les reconnaît pas nativement (ce rôle revient historiquement au plugin Failsafe, non utilisé ici). Un même `./mvnw test` pouvait exécuter 58, 74 ou 81 tests selon les runs, sans erreur signalée. Corrigé en déclarant explicitement les patterns d'inclusion dans le `maven-surefire-plugin` (`pom.xml`) :

```xml
<includes>
    <include>**/*Test.java</include>
    <include>**/*Tests.java</include>
    <include>**/*IT.java</include>
</includes>
```

Depuis ce correctif, deux exécutions propres (`./mvnw clean test`) consécutives donnent le même résultat : **81 tests, 0 échec**, couverture JaCoCo **85,2 %** (contre 86,2 % à l'étape 3, la légère baisse s'expliquant par le nouveau code du client HTTP de recommandation, non testé directement puisque simulé dans les tests).
