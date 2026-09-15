#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
meta=.local/runtime/backend-build.properties
war=backend/target/booking.war
source_hash() {
  { find backend/src/main .mvn -type f -print0; printf '%s\0' backend/pom.xml mvnw; } |
    sort -z | xargs -0 sha256sum | sha256sum | cut -d ' ' -f1
}
value() { sed -n "s/^$1=//p" "$meta"; }
case "${1:-current}" in
  current)
    [[ -f "$meta" && -f "$war" ]] || exit 1
    [[ "$(value source_hash)" == "$(source_hash)" && "$(value war_hash)" == "$(sha256sum "$war" | cut -d ' ' -f1)" ]]
    ;;
  build|verify)
    # Trust only the known container bind mount before recording Git metadata.
    if [[ -f /.dockerenv && "$PWD" == /workspace ]]; then
      git config --global --fixed-value --get-all safe.directory /workspace >/dev/null ||
        git config --global --add safe.directory /workspace
    fi
    before=$(source_hash)
    # Both goals execute tests. Never bless a skipped-test artifact as verified.
    goal=package; [[ "$1" == verify ]] && goal=verify
    bash mvnw -B -ntp -f backend/pom.xml clean "$goal"
    [[ "$before" == "$(source_hash)" ]] || { echo 'Sources changed during build; retry.' >&2; exit 1; }
    mkdir -p .local/runtime
    {
      echo "source_hash=$before"
      echo "war_hash=$(sha256sum "$war" | cut -d ' ' -f1)"
      echo "source_revision=$(git rev-parse HEAD)"
      echo "source_dirty=$(git status --porcelain -- backend/src/main backend/pom.xml .mvn mvnw | wc -l)"
      echo "built_at_utc=$(date -u +%FT%TZ)"
    } > "$meta.tmp"
    mv "$meta.tmp" "$meta"
    cat "$meta"
    ;;
  *) echo 'Use current|build|verify' >&2; exit 2 ;;
esac
