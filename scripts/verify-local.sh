#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
for tool in java node npm git; do
  command -v "$tool" >/dev/null || { echo "Missing tool: $tool" >&2; exit 1; }
done
java -version
node --version
npm --version
git --version
# Scope Git trust to this explicit Windows bind mount, never to all directories.
[[ -f /.dockerenv && "$PWD" == /workspace ]] || {
  echo "Run this script inside the project's dev container (/workspace)." >&2
  exit 1
}
git config --global --fixed-value --get-all safe.directory /workspace >/dev/null ||
  git config --global --add safe.directory /workspace
git status --short
bash mvnw -version
bash mvnw -B -ntp -f infra/checks/pom.xml dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
bash scripts/jdbc-check.sh check
cd infra/checks/frontend
npm ci --ignore-scripts --no-fund
npm ls --depth=0
npm run check
echo "Infrastructure checks passed. Starter application and WAR are not available."