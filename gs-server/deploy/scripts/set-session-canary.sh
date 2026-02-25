#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
CFG_FILE="${ROOT_DIR}/deploy/config/cluster-hosts.properties"
SYNC_SCRIPT="${SCRIPT_DIR}/sync-cluster-hosts.sh"

if [[ ! -f "${CFG_FILE}" ]]; then
  echo "Missing config file: ${CFG_FILE}" >&2
  exit 1
fi

enabled=""
banks=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --enabled)
      enabled="${2:-}"
      shift 2
      ;;
    --banks)
      banks="${2:-}"
      shift 2
      ;;
    -h|--help)
      cat <<USAGE
Usage: $(basename "$0") --enabled true|false [--banks 6274,6275]

Examples:
  $(basename "$0") --enabled true --banks 6274
  $(basename "$0") --enabled false
USAGE
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 1
      ;;
  esac
done

if [[ -z "${enabled}" ]]; then
  echo "Missing required argument: --enabled true|false" >&2
  exit 1
fi

case "${enabled}" in
  true|false) ;;
  *)
    echo "Invalid --enabled value: ${enabled}. Use true|false." >&2
    exit 1
    ;;
esac

if [[ -z "${banks}" ]]; then
  banks_line=""
else
  banks_line="${banks}"
fi

tmp_file="$(mktemp)"

awk -F= -v enabled="${enabled}" -v banks="${banks_line}" '
BEGIN {
  seen_enabled=0;
  seen_banks=0;
}
{
  key=$1;
  gsub(/^[ \t]+|[ \t]+$/, "", key);
  if (key == "SESSION_SERVICE_ROUTE_ENABLED") {
    print "SESSION_SERVICE_ROUTE_ENABLED=" enabled;
    seen_enabled=1;
  } else if (key == "SESSION_SERVICE_CANARY_BANKS") {
    if (banks != "") {
      print "SESSION_SERVICE_CANARY_BANKS=" banks;
    } else {
      print $0;
    }
    seen_banks=1;
  } else {
    print $0;
  }
}
END {
  if (!seen_enabled) {
    print "SESSION_SERVICE_ROUTE_ENABLED=" enabled;
  }
  if (!seen_banks && banks != "") {
    print "SESSION_SERVICE_CANARY_BANKS=" banks;
  }
}
' "${CFG_FILE}" > "${tmp_file}"

mv "${tmp_file}" "${CFG_FILE}"

bash "${SYNC_SCRIPT}" >/dev/null

echo "Updated session canary routing:"
echo "  SESSION_SERVICE_ROUTE_ENABLED=${enabled}"
if [[ -n "${banks_line}" ]]; then
  echo "  SESSION_SERVICE_CANARY_BANKS=${banks_line}"
fi
