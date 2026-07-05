from fastapi import FastAPI, HTTPException

from models import RecommendationsResponse, UserData
from recommendation import generate_recommendations

app = FastAPI(title="GamesUP - API de recommandation")


# Endpoint de base pour tester que l'API est en ligne
@app.get("/")
async def root():
    return {"message": "API de recommandation en ligne"}


# Endpoint pour envoyer les données d'utilisateur et récupérer des recommandations
@app.post("/recommendations/", response_model=RecommendationsResponse)
async def get_recommendations(data: UserData) -> RecommendationsResponse:
    try:
        recommendations = generate_recommendations(data)
        return RecommendationsResponse(recommendations=recommendations)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
