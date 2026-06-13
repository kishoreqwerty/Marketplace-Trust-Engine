import joblib
import numpy as np
import shap
from fastapi import FastAPI
from pydantic import BaseModel
from typing import Optional, Dict
import os
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Fraud Scorer", version="1.0.0")

MODEL_PATH = os.getenv("MODEL_PATH", "model/fraud_model.pkl")
model_data = None
explainer = None

RISK_KEYWORDS = [
    "urgent", "asap", "must sell today", "moving abroad", "gift card",
    "venmo only", "cash only", "no returns", "wire transfer",
    "western union", "zelle only", "99% off"
]

CATEGORY_MEDIANS = {
    "electronics": 250.0, "clothing": 30.0, "furniture": 150.0,
    "vehicles": 8000.0, "jewelry": 100.0, "toys": 25.0,
    "sports": 75.0, "default": 100.0
}

FEATURE_NAMES = [
    "price_ratio_to_median", "seller_account_age_days", "seller_reputation_score",
    "seller_listing_velocity", "seller_dispute_rate", "seller_total_listings",
    "title_risk_keywords", "description_urgency_score", "is_duplicate_image",
    "price_below_market_pct"
]


class ListingEvent(BaseModel):
    listingId: str
    sellerId: str
    title: str
    description: Optional[str] = ""
    price: float
    category: str
    condition: Optional[str] = "used"
    locationCity: Optional[str] = ""
    locationState: Optional[str] = ""
    sellerReputationScore: float = 0.5
    sellerAccountAgeDays: int = 30
    sellerTotalListings: int = 0
    sellerDisputeCount: int = 0


class ScoreResponse(BaseModel):
    fraudProbability: float
    label: str
    shapValues: Dict[str, float]
    topReason: str


@app.on_event("startup")
def load_model():
    global model_data, explainer
    if os.path.exists(MODEL_PATH):
        model_data = joblib.load(MODEL_PATH)
        explainer = shap.TreeExplainer(model_data["model"])
        logger.info("Model loaded successfully")
    else:
        logger.warning("Model not found. Run train_model.py first. Using rule-based fallback.")


def extract_features(event: ListingEvent) -> np.ndarray:
    text = f"{event.title} {event.description}".lower()
    category_median = CATEGORY_MEDIANS.get(event.category.lower(), CATEGORY_MEDIANS["default"])
    price_ratio = event.price / category_median if category_median > 0 else 1.0
    price_below_pct = (event.price - category_median) / category_median * 100
    has_risk_kw = 1 if any(kw in text for kw in RISK_KEYWORDS) else 0
    urgency_words = ["urgent", "today", "asap", "immediately", "now", "hurry"]
    urgency_score = min(1.0, sum(1 for w in urgency_words if w in text) / 3.0)
    dispute_rate = event.sellerDisputeCount / max(event.sellerTotalListings, 1)

    return np.array([[
        price_ratio, event.sellerAccountAgeDays, event.sellerReputationScore,
        1, dispute_rate, event.sellerTotalListings,
        has_risk_kw, urgency_score, 0, price_below_pct
    ]])


def feature_to_reason(feature: str, val: float) -> str:
    reasons = {
        "price_ratio_to_median": f"Price is {val:.1f}x the category median",
        "seller_account_age_days": f"Seller account only {int(val)} days old",
        "seller_reputation_score": f"Low seller reputation: {val:.2f}",
        "seller_dispute_rate": f"High dispute rate: {val:.1%}",
        "title_risk_keywords": "Risk keywords detected in listing",
        "description_urgency_score": "High urgency language in description",
        "is_duplicate_image": "Possible duplicate image",
        "price_below_market_pct": f"Price is {abs(val):.0f}% below market",
    }
    return reasons.get(feature, f"Anomalous signal: {feature}")


@app.post("/score", response_model=ScoreResponse)
def score_listing(event: ListingEvent):
    if model_data is None:
        return rule_based_score(event)

    features = extract_features(event)
    prob = float(model_data["model"].predict_proba(features)[0][1])
    shap_vals = explainer.shap_values(features)[0]
    shap_dict = {name: float(val) for name, val in zip(FEATURE_NAMES, shap_vals)}
    top_feature = max(shap_dict, key=lambda k: abs(shap_dict[k]))
    top_reason = feature_to_reason(top_feature, features[0][FEATURE_NAMES.index(top_feature)])
    label = "FRAUD" if prob >= 0.65 else "REVIEW" if prob >= 0.45 else "CLEAN"

    return ScoreResponse(
        fraudProbability=round(prob, 4), label=label,
        shapValues=shap_dict, topReason=top_reason
    )


def rule_based_score(event: ListingEvent) -> ScoreResponse:
    score = 0.1
    reason = "No major risk signals"
    text = f"{event.title} {event.description}".lower()

    if any(kw in text for kw in RISK_KEYWORDS):
        score += 0.4
        reason = "High-risk keywords detected"
    category_median = CATEGORY_MEDIANS.get(event.category.lower(), 100.0)
    if event.price < category_median * 0.3:
        score += 0.3
        reason = "Price significantly below market value"
    if event.sellerAccountAgeDays < 7:
        score += 0.2

    score = min(1.0, score)
    label = "FRAUD" if score >= 0.65 else "REVIEW" if score >= 0.45 else "CLEAN"
    return ScoreResponse(
        fraudProbability=round(score, 4), label=label,
        shapValues={"rule_based": score}, topReason=reason
    )


@app.get("/health")
def health():
    return {"status": "ok", "model_loaded": model_data is not None}


@app.get("/model/info")
def model_info():
    if model_data is None:
        return {"status": "no_model", "mode": "rule_based"}
    return {"status": "loaded", "features": FEATURE_NAMES, "model_type": "XGBoostClassifier"}
