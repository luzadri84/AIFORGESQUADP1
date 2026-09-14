#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
classpath_file=infra/checks/target/classpath.txt
if [[ ! -s "$classpath_file" ]]; then
  echo "Missing JDBC classpath. Run bash scripts/verify-local.sh first." >&2
  exit 1
fi
java --class-path "$(cat "$classpath_file")" infra/checks/JdbcCheck.java "${1:-check}"