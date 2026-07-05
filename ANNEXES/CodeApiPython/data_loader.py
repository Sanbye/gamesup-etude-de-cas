import pandas as pd


def load_training_data(file_path: str) -> pd.DataFrame:
    """Charge les données d'entraînement.

    Le fichier CSV attendu contient une ligne par interaction utilisateur/jeu,
    avec les colonnes : user_id, game_id, rating.
    """
    return pd.read_csv(file_path)


def build_user_item_matrix(data: pd.DataFrame) -> pd.DataFrame:
    """Transforme les interactions (user_id, game_id, rating) en une matrice
    utilisateur x jeu, exploitable par un modèle de plus proches voisins.
    Les couples utilisateur/jeu sans interaction connue sont mis à 0.
    """
    return data.pivot_table(index="user_id", columns="game_id", values="rating", fill_value=0)
