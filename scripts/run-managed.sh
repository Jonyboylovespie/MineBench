#!/usr/bin/env bash
set -euo pipefail

# Safe orchestration hook. It launches only commands explicitly supplied by the caller; it never swaps jars or
# overwrites third-party configuration. A launcher command should block until Minecraft exits.
if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <status-file> <launch-command> [args...]" >&2
  echo "Example: $0 instance-a/config/minebench/managed-status.json prism-launcher --launch MineBenchBaseline" >&2
  exit 64
fi

status_file=$1
shift
launch_command=("$@")

echo "Launching explicit managed profile command: ${launch_command[*]}"
"${launch_command[@]}"

if [[ ! -f "$status_file" ]]; then
  echo "Managed run exited without a status file: $status_file" >&2
  exit 1
fi

status=$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1], encoding="utf-8")).get("status", "missing"))' "$status_file")
case "$status" in
  success) echo "MineBench managed run succeeded: $status_file" ;;
  *) echo "MineBench managed run status: $status" >&2; exit 1 ;;
esac
