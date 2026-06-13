#!/bin/bash
echo "Seeding test sellers and listings..."

BASE_URL="http://localhost:8080"

# Create sellers
for i in {1..5}; do
  curl -s -X POST "$BASE_URL/api/sellers" \
    -H "Content-Type: application/json" \
    -d "{\"externalId\":\"seller_$i\",\"username\":\"testuser$i\",\"accountCreatedAt\":\"2023-0$i-01T00:00:00Z\"}" > /dev/null
done

# Submit listings (mix of clean and suspicious)
curl -s -X POST "$BASE_URL/api/listings" \
  -H "Content-Type: application/json" \
  -d '{"sellerId":"seller_1","title":"iPhone 14 Pro Max 256GB","description":"Good condition, minor scratches","price":650,"category":"electronics","condition":"used","locationCity":"Seattle","locationState":"WA"}' | jq .

curl -s -X POST "$BASE_URL/api/listings" \
  -H "Content-Type: application/json" \
  -d '{"sellerId":"seller_2","title":"URGENT!! iPhone 14 Pro 99% off send gift card NOW","description":"Moving abroad must sell TODAY venmo only no returns","price":50,"category":"electronics","condition":"new","locationCity":"Miami","locationState":"FL"}' | jq .

echo "Seeding complete."
