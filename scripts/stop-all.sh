#!/usr/bin/env bash
#
# Stop every microservice started by start-all.sh using the recorded PIDs.
#
set -uo pipefail
cd "$(dirname "$0")/.."

LOG_DIR="logs"

for pidfile in "${LOG_DIR}"/*.pid; do
  [ -e "${pidfile}" ] || continue
  name="$(basename "${pidfile}" .pid)"
  pid="$(cat "${pidfile}")"
  if kill -0 "${pid}" 2>/dev/null; then
    echo ">> Stopping ${name} (pid ${pid})..."
    kill "${pid}"
  fi
  rm -f "${pidfile}"
done

echo "All services stopped."
