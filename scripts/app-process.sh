#!/usr/bin/env bash
set -euo pipefail
cd /workspace
mkdir -p .local/runtime
start_one() {
  local name="$1"; shift
  local file=".local/runtime/$name.pid"
  if [[ -f "$file" ]] && kill -0 "$(cat "$file")" 2>/dev/null; then echo "$name already running"; return; fi
  nohup "$@" > ".local/runtime/$name.log" 2>&1 < /dev/null &
  echo "$!" > "$file"
  echo "$name started"
}
stop_one() {
  local name="$1"; local file=".local/runtime/$name.pid"
  if [[ ! -f "$file" ]]; then return; fi
  local pid; pid=$(cat "$file")
  if [[ -r "/proc/$pid/cmdline" ]]; then
    local command; command=$(tr '\0' ' ' < "/proc/$pid/cmdline")
    if [[ "$command" != *'/workspace/backend/target/booking.war'* ]] && ! { [[ "$command" == 'ng serve booking '* ]] && [[ "$(readlink "/proc/$pid/cwd")" == /workspace/frontend ]]; }; then
      echo "Refusing unrelated PID for $name" >&2; return 1
    fi
    kill "$pid"
  fi
  rm -f "$file"
}
case "${1:-status}" in
 backend) start_one backend java -Xmx384m -jar /workspace/backend/target/booking.war ;;
 frontend) start_one frontend bash -c 'cd /workspace/frontend; exec node /workspace/frontend/node_modules/@angular/cli/bin/ng.js serve booking --host 0.0.0.0 --port 4200 ' ;;
 stop) stop_one frontend; stop_one backend ;;
 status) for name in backend frontend; do file=".local/runtime/$name.pid"; if [[ -f "$file" ]] && kill -0 "$(cat "$file")" 2>/dev/null; then echo "$name running"; else echo "$name stopped"; fi; done ;;
 *) echo "Use backend|frontend|stop|status" >&2; exit 2 ;;
esac
