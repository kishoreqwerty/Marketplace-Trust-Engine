"""
Train a synthetic fraud detection model.
Run this once to generate model/fraud_model.pkl before starting the service.
"""
import numpy as np
import pandas as pd
import xgboost as xgb
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, roc_auc_score
import joblib
import os

SEED = 42
np.random.seed(SEED)
N = 10_000

FEATURES = [
    "price_ratio_to_median", "seller_account_age_days", "seller_reputation_score",
    "seller_listing_velocity", "seller_dispute_rate", "seller_total_listings",
    "title_risk_keywords", "description_urgency_score", "is_duplicate_image",
    "price_below_market_pct"
]

def generate_synthetic_data(n):
    n_legit = int(n * 0.80)
    n_fraud = n - n_legit

    legit = pd.DataFrame({
        "price_ratio_to_median": np.random.normal(1.0, 0.3, n_legit).clip(0.3, 3.0),
        "seller_account_age_days": np.random.exponential(400, n_legit).clip(1, 2000),
        "seller_reputation_score": np.random.beta(5, 2, n_legit),
        "seller_listing_velocity": np.random.poisson(2, n_legit).clip(0, 20),
        "seller_dispute_rate": np.random.beta(1, 20, n_legit),
        "seller_total_listings": np.random.poisson(15, n_legit).clip(1, 200),
        "title_risk_keywords": np.random.binomial(1, 0.05, n_legit),
        "description_urgency_score": np.random.beta(1, 5, n_legit),
        "is_duplicate_image": np.random.binomial(1, 0.02, n_legit),
        "price_below_market_pct": np.random.normal(0, 15, n_legit).clip(-30, 30),
        "label": 0
    })

    fraud = pd.DataFrame({
        "price_ratio_to_median": np.random.normal(0.25, 0.15, n_fraud).clip(0.05, 0.6),
        "seller_account_age_days": np.random.exponential(15, n_fraud).clip(0, 60),
        "seller_reputation_score": np.random.beta(2, 8, n_fraud),
        "seller_listing_velocity": np.random.poisson(12, n_fraud).clip(0, 50),
        "seller_dispute_rate": np.random.beta(3, 5, n_fraud),
        "seller_total_listings": np.random.poisson(3, n_fraud).clip(0, 30),
        "title_risk_keywords": np.random.binomial(1, 0.7, n_fraud),
        "description_urgency_score": np.random.beta(4, 2, n_fraud),
        "is_duplicate_image": np.random.binomial(1, 0.35, n_fraud),
        "price_below_market_pct": np.random.normal(-55, 20, n_fraud).clip(-90, -10),
        "label": 1
    })

    return pd.concat([legit, fraud]).sample(frac=1, random_state=SEED).reset_index(drop=True)


def train():
    print("Generating synthetic data...")
    df = generate_synthetic_data(N)
    X, y = df[FEATURES], df["label"]
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=SEED)

    print("Training XGBoost model...")
    model = xgb.XGBClassifier(
        n_estimators=200, max_depth=5, learning_rate=0.1,
        subsample=0.8, colsample_bytree=0.8, scale_pos_weight=4,
        eval_metric="auc", random_state=SEED
    )
    model.fit(X_train, y_train, eval_set=[(X_test, y_test)], verbose=False)

    y_prob = model.predict_proba(X_test)[:, 1]
    print(f"ROC-AUC: {roc_auc_score(y_test, y_prob):.4f}")
    print(classification_report(y_test, model.predict(X_test)))

    os.makedirs("model", exist_ok=True)
    joblib.dump({"model": model, "features": FEATURES}, "model/fraud_model.pkl")
    print("Model saved to model/fraud_model.pkl")

if __name__ == "__main__":
    train()
