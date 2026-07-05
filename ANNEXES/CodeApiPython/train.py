"""Script d'entraînement du modèle de recommandation KNN.

Usage :
    python train.py chemin/vers/donnees.csv

Le CSV attendu contient une ligne par interaction utilisateur/jeu, avec les
colonnes : user_id, game_id, rating (voir README.md pour le détail du format
et de la manière dont ces données sont extraites de l'API Spring).

Ce script est volontairement séparé de l'API (main.py) : l'entraînement est une
opération ponctuelle/planifiée, distincte du service de recommandation appelé
à chaque requête.
"""

import sys

from recommendation import train_model

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage : python train.py chemin/vers/donnees.csv")
        sys.exit(1)

    data_path = sys.argv[1]
    train_model(data_path)
    print(f"Modèle entraîné et sauvegardé (modele KNN) à partir de {data_path}")
