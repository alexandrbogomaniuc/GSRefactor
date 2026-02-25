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
bank_flags=""
set_banks="false"
set_bank_flags="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --enabled)
      enabled="${2:-}"
      shift 2
      ;;
    --banks)
      banks="${2:-}"
      set_banks="true"
      shift 2
      ;;
    --bank-flags)
      bank_flags="${2:-}"
      set_bank_flags="true"
      shift 2
      ;;
    -h|--help)
      cat <<USAGE
Usage: $(basename "$0") --enabled true|false [--banks 6274,6275] [--bank-flags 6275:false,6274:true]

Examples:
  $(basename "$0") --enabled true --banks 6274 --bank-flags 6275:false,6274:true
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

tmp_file="$(mktemp)"

awk -F= -v enabled="${enabled}" -v banks="${banks}" -v set_banks="${set_banks}" -v bank_flags="${bank_flags}" -v set_bank_flags="${set_bank_flags}" '
BEGIN {
  seen_enabled=0;
  seen_banks=0;
  seen_bank_flags=0;
}
{
  key=$1;
  gsub(/^[ \t]+|[ \t]+$/, "", key);
  if (key == "MULTIPLAYER_SERVICE_ROUTE_ENABLED") {
    print "MULTIPLAYER_SERVICE_ROUTE_ENABLED=" enabled;
    seen_enabled=1;
  } else if (key == "MULTIPLAYER_SERVICE_CANARY_BANKS") {
    if (set_banks == "true") {
      print "MULTIPLAYER_SERVICE_CANARY_BANKS=" banks;
    } else {
      print $0;
    }
    seen_banks=1;
  } else if (key == "MULTIPLAYER_SERVICE_BANK_FLAGS") {
    if (set_bank_flags == "true") {
      print "MULTIPLAYER_SERVICE_BANK_FLAGS=" bank_flags;
    } else {
      print $0;
    }
    seen_bank_flags=1;
  } else {
    print $0;
  }
}
END {
  if (!seen_enabled) {
    print "MULTIPLAYER_SERVICE_ROUTE_ENABLED=" enabled;
  }
  if (!seen_banks && set_banks == "true") {
    print "MULTIPLAYER_SERVICE_CANARY_BANKS=" banks;
  }
  if (!seen_bank_flags && set_bank_flags == "true") {
    print "MULTIPLAYER_SERVICE_BANK_FLAGS=" bank_flags;
  }
}
' "${CFG_FILE}" > "${tmp_file}"

mv "${tmp_file}" "${CFG_FILE}"

bash "${SYNC_SCRIPT}" >/dev/null

echo "Updated multiplayer canary routing:"
echo "  MULTIPLAYER_SERVICE_ROUTE_ENABLED=${enabled}"
if [[ "${set_banks}" == "true" ]]; then
  echo "  MULTIPLAYER_SERVICE_CANARY_BANKS=${banks}"
fi
if [[ "${set_bank_flags}" == "true" ]]; then
  echo "  MULTIPLAYER_SERVICE_BANK_FLAGS=${bank_flags}"
fi
echo "Config synced to compose/env files."
echo "If multiplayer-service is already running, recreate it to apply env changes."
