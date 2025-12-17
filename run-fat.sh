#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH=$(find "$SCRIPT_DIR/target" -name "sophie-fat*.jar" -print -quit 2>/dev/null || true)

if [[ -z "$JAR_PATH" ]]; then
  echo "JAR non trovato in target/. Hai eseguito 'sbt clean assembly' o './build-fat.sh'?" >&2
  exit 1
fi

echo "Eseguo JAR: $JAR_PATH"
exec java -jar "$JAR_PATH" "$@"
