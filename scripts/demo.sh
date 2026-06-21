#!/usr/bin/env bash
#
# End-to-end smoke test of the saga through the API gateway (port 8080).
# Demonstrates the three flows: happy path, payment failure, out-of-stock.
#
set -euo pipefail
BASE="${BASE_URL:-http://localhost:8080}"

say() { printf "\n=== %s ===\n" "$1"; }

say "1) HAPPY PATH: small order should end CONFIRMED"
curl -s -X POST "${BASE}/api/orders" -H 'Content-Type: application/json' \
  -d '{"productCode":"LAPTOP-001","quantity":1,"amount":999,"customerEmail":"alice@example.com"}'

say "2) PAYMENT FAILURE: amount over approval limit (5000) should end CANCELLED"
curl -s -X POST "${BASE}/api/orders" -H 'Content-Type: application/json' \
  -d '{"productCode":"LAPTOP-001","quantity":6,"amount":6000,"customerEmail":"bob@example.com"}'

say "3) OUT OF STOCK: HEADSET-001 only has 3 units"
curl -s -X POST "${BASE}/api/orders" -H 'Content-Type: application/json' \
  -d '{"productCode":"HEADSET-001","quantity":10,"amount":1490,"customerEmail":"carol@example.com"}'

sleep 4
say "ORDERS (final statuses)"
curl -s "${BASE}/api/orders"
say "INVENTORY"
curl -s "${BASE}/api/inventory"
say "PAYMENTS"
curl -s "${BASE}/api/payments"
say "NOTIFICATIONS (port 8084 direct)"
curl -s "http://localhost:8084/api/notifications"
echo ""
