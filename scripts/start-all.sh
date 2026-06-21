#!/usr/bin/env bash
#
# Build (if needed) and start every microservice locally as a background JVM.
# Logs are written to ./logs/<service>.log and PIDs to ./logs/<service>.pid.
#
# Prerequisites: JDK 8, Maven, and Kafka running (see `docker compose up -d`).
#
set -euo pipefail
cd "$(dirname "$0")/.."

LOG_DIR="logs"
mkdir -p "$LOG_DIR"

# Boot order matters: registry first, then the gateway and business services.
SERVICES=(
  "discovery-server:8761"
  "api-gateway:8080"
  "order-service:8081"
  "inventory-service:8082"
  "payment-service:8083"
  "notification-service:8084"
)

if [ ! -f "order-service/target/order-service-1.0.0.jar" ]; then
  echo ">> Building project (mvn -DskipTests package)..."
  mvn -q -DskipTests package
fi

for entry in "${SERVICES[@]}"; do
  name="${entry%%:*}"
  port="${entry##*:}"
  jar="${name}/target/${name}-1.0.0.jar"
  echo ">> Starting ${name} on port ${port}..."
  nohup java -jar "${jar}" > "${LOG_DIR}/${name}.log" 2>&1 &
  echo $! > "${LOG_DIR}/${name}.pid"
  # Give the registry a head start so the rest can register cleanly.
  if [ "${name}" = "discovery-server" ]; then
    sleep 20
  else
    sleep 8
  fi
done

echo ""
echo "All services starting. Tail logs with: tail -f logs/<service>.log"
echo "Eureka dashboard: http://localhost:8761"
echo "Kafka UI:         http://localhost:8090"
