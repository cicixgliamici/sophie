#!/usr/bin/env bash
set -euo pipefail

SBT_CMD="${SBT_CMD:-sbt}"

if ! command -v ${SBT_CMD%% *} >/dev/null 2>&1; then
  echo "Comando sbt non trovato: imposta SBT_CMD o installa sbt (https://www.scala-sbt.org/)." >&2
  exit 1
fi

pushd "$(dirname "$0")" >/dev/null

echo "Eseguo build del fat JAR con: ${SBT_CMD}"
${SBT_CMD} -batch clean assembly

JAR_PATH="target/scala-3.3.6/sophie-fat.jar"
if [[ -f "$JAR_PATH" ]]; then
  echo "Fat JAR generato: $JAR_PATH"
else
  echo "Attenzione: build terminata ma il file $JAR_PATH non esiste." >&2
  exit 1
fi

popd >/dev/null
