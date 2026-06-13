#!/bin/bash
# Load test script for Marketplace Trust Engine
# Tests fraud scoring pipeline end-to-end

BASE_URL="http://localhost:8080/api/v1"

echo "=== Marketplace Trust Engine - API Test ==="
echo ""

# Seed seller IDs (from init.sql)
ALICE_ID="11111111-1111-1111-1111-111111111111"
BOB_ID="22222222-2222-2222-2222-222222222222"
CHARLIE_ID="33333333-3333-3333-3333-333333333333"

echo "1. Submit a LEGIT listing (Alice, established seller)..."
curl -s -X POST "$BASE_URL/listings" \
  -H "Content-Type: application/json" \
  -d "{
    \"sellerId\": \"$ALICE_ID\",
    \"title\": \"Sony WH-1000XM5 Headphones - Like New\",
    \"description\": \"Bought 8 months ago, barely used. Original box, all accessories included. No scratches.\",
    \"price\": 220.00,
    \"category\": \"ELECTRONICS\",
    \"condition\": \"LIKE_NEW\",
    \"locationCity\": \"Seattle\",
    \"locationState\": \"WA\",
    \"imageCount\": 5
  }" | python3 -m json.tool
echo ""

echo "2. Submit a SUSPICIOUS listing (Bob, new account, spam title)..."
curl -s -X POST "$BASE_URL/listings" \
  -H "Content-Type: application/json" \
  -d "{
    \"sellerId\": \"$BOB_ID\",
    \"title\": \"IPHONE 15 PRO MAX BEST PRICE URGENT LIMITED TIME OFFER!!!\",
    \"description\": \"great deal\",
    \"price\": 50.00,
    \"category\": \"ELECTRONICS\",
    \"condition\": \"NEW\",
    \"locationCity\": \"Los Angeles\",
    \"locationState\": \"CA\",
    \"imageCount\": 0
  }" | python3 -m json.tool
echo ""

echo "3. Get flagged listings (moderation queue)..."
curl -s "$BASE_URL/listings/flagged" | python3 -m json.tool
echo ""

echo "4. Get dashboard stats..."
curl -s "$BASE_URL/listings/stats" | python3 -m json.tool
echo ""

echo "5. Get seller reputation (Alice - should be LOW risk)..."
curl -s "$BASE_URL/sellers/$ALICE_ID/reputation" | python3 -m json.tool
echo ""

echo "6. Get seller reputation (Charlie - should be HIGH/CRITICAL risk)..."
curl -s "$BASE_URL/sellers/$CHARLIE_ID/reputation" | python3 -m json.tool
echo ""

echo "7. Get Claude listing improvement suggestions..."
curl -s -X POST "$BASE_URL/listings/improve" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "iphone for sale",
    "description": "works good",
    "category": "ELECTRONICS",
    "price": 350.00
  }' | python3 -m json.tool
echo ""

echo "=== Load test: Submit 50 listings ==="
for i in $(seq 1 50); do
  SELLER=$([ $((i % 3)) -eq 0 ] && echo "$CHARLIE_ID" || ([ $((i % 2)) -eq 0 ] && echo "$BOB_ID" || echo "$ALICE_ID"))
  curl -s -X POST "$BASE_URL/listings" \
    -H "Content-Type: application/json" \
    -d "{
      \"sellerId\": \"$SELLER\",
      \"title\": \"Test item number $i\",
      \"price\": $((RANDOM % 500 + 10)).99,
      \"category\": \"ELECTRONICS\",
      \"condition\": \"USED\",
      \"imageCount\": $((RANDOM % 5))
    }" > /dev/null
done
echo "50 listings submitted. Check stats endpoint for results."
curl -s "$BASE_URL/listings/stats" | python3 -m json.tool
