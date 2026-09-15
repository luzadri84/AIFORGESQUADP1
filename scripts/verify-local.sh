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
if [[ -f .local/persistence-token ]]; then bash scripts/jdbc-check.sh read; else bash scripts/jdbc-check.sh check; fi
cd infra/checks/frontend
npm ls --depth=0 >/dev/null 2>&1 || npm ci --ignore-scripts --no-audit --no-fund
npm ls --depth=0
npm run check
echo "Toolchain and Oracle checks passed. Application build/test: node scripts/environment.mjs verify (host)."