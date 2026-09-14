#!/usr/bin/env bash
set -euo pipefail
cd /workspace
mkdir -p .local/runtime
owned_pid() {
  local name="$1" pid="$2" command
  [[ "$pid" =~ ^[0-9]+$ && -r "/proc/$pid/cmdline" ]] || return 1
  command=$(tr '\0' ' ' < "/proc/$pid/cmdline")
  case "$name" in
    backend) [[ "$command" == *'java '* && "$command" == *'/workspace/backend/target/booking.war'* ]] ;;
    frontend) [[ "$command" == 'ng serve booking '* && "$(readlink "/proc/$pid/cwd")" == /workspace/frontend ]] ;;
    *) return 1 ;;
  esac
}
start_one() {
  local name="$1"; shift
  local file=".local/runtime/$name.pid"
  if [[ -f "$file" ]] && owned_pid "$name" "$(cat "$file")"; then echo "$name already running"; return; fi
  # A stale PID file is only our metadata; never signal the process it happens to name.
  nohup "$@" > ".local/runtime/$name.log" 2>&1 < /dev/null &
  echo "$!" > "$file"
  echo "$name started"
}
stop_one() {
  local name="$1" file=".local/runtime/$1.pid" pid
  [[ -f "$file" ]] || return 0
  pid=$(cat "$file")
  if [[ -d "/proc/$pid" ]]; then
    owned_pid "$name" "$pid" || { echo "Refusing unrelated PID for $name" >&2; return 1; }
    kill "$pid"
    for attempt in {1..30}; do owned_pid "$name" "$pid" || break; sleep 1; done
    if owned_pid "$name" "$pid"; then echo "$name did not stop; inspect before continuing" >&2; return 1; fi
  fi
  rm -f "$file"
}
case "${1:-status}" in
 backend) start_one backend java -Xmx384m -jar /workspace/backend/target/booking.war ;;
 frontend) start_one frontend bash -c 'cd /workspace/frontend; exec node /workspace/frontend/node_modules/@angular/cli/bin/ng.js serve booking --host 0.0.0.0 --port 4200 --poll 1000' ;;
 stop-backend) stop_one backend ;;
 stop-frontend) stop_one frontend ;;
 stop) stop_one frontend; stop_one backend ;;
 status) for name in backend frontend; do file=".local/runtime/$name.pid"; if [[ -f "$file" ]] && owned_pid "$name" "$(cat "$file")"; then echo "$name running"; else echo "$name stopped"; fi; done ;;
 *) echo "Use backend|frontend|stop|status" >&2; exit 2 ;;
esac
