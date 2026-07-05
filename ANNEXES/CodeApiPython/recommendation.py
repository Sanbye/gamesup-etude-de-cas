# recommendation.py
#
# Système de recommandation basé sur un modèle KNN (filtrage collaboratif
# utilisateur-utilisateur) : on entraîne un modèle de plus proches voisins
# sur une matrice utilisateur x jeu (notes explicites), puis, pour un
# utilisateur donné, on cherche les utilisateurs les plus proches et on lui
# recommande les jeux qu'ils ont appréciés et qu'il ne possède pas encore.
#
# Tant qu'aucune donnée d'entraînement réelle n'est disponible (voir
# train_model), aucun modèle n'est persisté sur disque : generate_recommendations
# retombe alors sur une liste de test, ce qui permet à l'API Spring d'être
# développée et intégrée dès maintenant sans attendre les données réelles.

import os
from typing import List, Optional

import joblib
import numpy as np
from sklearn.neighbors import NearestNeighbors

from data_loader import build_user_item_matrix, load_training_data
from models import GameRecommendation, UserData

MODEL_DIR = os.path.join(os.path.dirname(__file__), "models")
MODEL_PATH = os.path.join(MODEL_DIR, "knn_model.joblib")

DEFAULT_N_NEIGHBORS = 5
LIKED_RATING_THRESHOLD = 4.0

_FALLBACK_RECOMMENDATIONS = [
    GameRecommendation(game_id=101, game_name="Pandemic"),
    GameRecommendation(game_id=102, game_name="Catan"),
    GameRecommendation(game_id=103, game_name="Ticket to Ride"),
]


def train_model(training_data_path: str, n_neighbors: int = DEFAULT_N_NEIGHBORS) -> None:
    """Entraîne le modèle KNN à partir d'un CSV (colonnes user_id, game_id, rating)
    et persiste le modèle ainsi que la matrice utilisateur x jeu sur disque.

    Cette fonction est indépendante de l'API : elle est destinée à être rejouée
    (via train.py) dès que des données d'utilisation réelles seront disponibles.
    """
    data = load_training_data(training_data_path)
    matrix = build_user_item_matrix(data)

    n_neighbors = min(n_neighbors, len(matrix.index))
    model = NearestNeighbors(metric="cosine", algorithm="brute", n_neighbors=n_neighbors)
    model.fit(matrix.values)

    os.makedirs(MODEL_DIR, exist_ok=True)
    joblib.dump(
        {
            "model": model,
            "user_ids": matrix.index.to_numpy(),
            "game_ids": matrix.columns.to_numpy(),
            "matrix": matrix.values,
        },
        MODEL_PATH,
    )


def _load_artifacts() -> Optional[dict]:
    if not os.path.exists(MODEL_PATH):
        return None
    try:
        return joblib.load(MODEL_PATH)
    except Exception:
        return None


def _build_user_vector(user_data: UserData, game_ids: np.ndarray) -> np.ndarray:
    game_index = {game_id: position for position, game_id in enumerate(game_ids)}
    vector = np.zeros(len(game_ids))
    for purchase in user_data.purchases:
        position = game_index.get(purchase.game_id)
        if position is not None:
            vector[position] = purchase.rating
    return vector.reshape(1, -1)


def generate_recommendations(user_data: UserData, k: int = DEFAULT_N_NEIGHBORS) -> List[GameRecommendation]:
    artifacts = _load_artifacts()
    if artifacts is None:
        # Aucun modèle entraîné pour l'instant : on renvoie des données de test
        # pour permettre à l'API Spring d'être développée et intégrée dès maintenant.
        return _FALLBACK_RECOMMENDATIONS

    model: NearestNeighbors = artifacts["model"]
    user_ids: np.ndarray = artifacts["user_ids"]
    game_ids: np.ndarray = artifacts["game_ids"]
    matrix: np.ndarray = artifacts["matrix"]

    already_owned = {purchase.game_id for purchase in user_data.purchases}
    user_vector = _build_user_vector(user_data, game_ids)

    n_neighbors = min(k, len(user_ids))
    distances, indices = model.kneighbors(user_vector, n_neighbors=n_neighbors)

    scores: dict[int, float] = {}
    for neighbor_position, distance in zip(indices[0], distances[0]):
        similarity = 1 - distance
        neighbor_ratings = matrix[neighbor_position]
        for game_position, rating in enumerate(neighbor_ratings):
            game_id = int(game_ids[game_position])
            if rating >= LIKED_RATING_THRESHOLD and game_id not in already_owned:
                scores[game_id] = scores.get(game_id, 0.0) + similarity * rating

    ranked_game_ids = sorted(scores, key=scores.get, reverse=True)[:k]
    return [GameRecommendation(game_id=game_id, score=scores[game_id]) for game_id in ranked_game_ids]
