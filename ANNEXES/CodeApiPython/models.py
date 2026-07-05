from pydantic import BaseModel
from typing import List, Optional


class UserPurchase(BaseModel):
    game_id: int
    rating: float


class UserData(BaseModel):
    user_id: int
    purchases: List[UserPurchase]


class GameRecommendation(BaseModel):
    game_id: int
    game_name: Optional[str] = None
    score: Optional[float] = None


class RecommendationsResponse(BaseModel):
    recommendations: List[GameRecommendation]
