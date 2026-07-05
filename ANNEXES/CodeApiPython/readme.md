# API de recommandation GamesUP

API FastAPI qui reçoit l'historique d'un utilisateur (jeux achetés/notés) depuis l'API Spring et renvoie une liste de jeux recommandés, calculée par un modèle KNN (k plus proches voisins, filtrage collaboratif utilisateur-utilisateur).

## Installation

```bash
pip install -r requirements.txt
```

## Lancer l'API

```bash
uvicorn main:app --reload --port 8000
```

- `GET /` : vérifie que l'API est en ligne.
- `POST /recommendations/` : reçoit `{ "user_id": int, "purchases": [{ "game_id": int, "rating": float }] }`, renvoie `{ "recommendations": [{ "game_id": int, "game_name": str|null, "score": float|null }] }`.

## Données nécessaires à l'algorithme

Le modèle KNN a besoin d'une matrice **utilisateur x jeu** : une ligne par utilisateur, une colonne par jeu, la valeur étant la note de cet utilisateur pour ce jeu (0 si aucune interaction connue). Pour la construire, il faut donc, pour chaque interaction :

- `user_id` : identifiant de l'utilisateur (`User.id` côté Spring) ;
- `game_id` : identifiant du jeu (`Game.id` côté Spring) ;
- `rating` : une note entre 1 et 5.

Ces données viennent de deux sources côté Spring :
- **Avis explicites** (`Review.rating`) : signal le plus fiable.
- **Achats sans avis** (`Purchase`/`PurchaseLine`) : signal implicite (l'utilisateur a acheté le jeu, mais ne l'a pas noté) ; à défaut de mieux, l'API Spring leur attribue une note neutre par défaut avant l'appel (voir `RecommendationServiceImpl` côté Spring).

Plus il y a d'utilisateurs ayant noté un nombre significatif de jeux communs, plus le KNN est pertinent (problème classique de "cold start" pour les nouveaux utilisateurs/jeux, non traité ici).

## Entraîner le modèle

```bash
python train.py chemin/vers/interactions.csv
```

Le CSV attend trois colonnes : `user_id,game_id,rating`. Le modèle entraîné (`sklearn.neighbors.NearestNeighbors`, similarité cosinus) est sauvegardé dans `models/knn_model.joblib` (dossier ignoré par Git, car ce sont des données/artefacts et non du code).

**Tant qu'aucun modèle n'a été entraîné** (pas de données réelles disponibles pour l'instant, voir l'étude de cas), `POST /recommendations/` renvoie une liste de recommandations de test fixe (`Pandemic`, `Catan`, `Ticket to Ride`) : cela permet à l'API Spring d'être développée et testée dès maintenant, sans attendre les données réelles.
