#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  cat <<USAGE
Usage: $(basename "$0")

Runs Phase 4 runtime evidence pack with unreachable URLs and verifies
degraded classification statuses are reported (skip/block, not fail).
USAGE
  exit 0
fi

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

OUT_DIR="${TMP_DIR}/out"
mkdir -p "${OUT_DIR}"

"${ROOT}/gs-server/deploy/scripts/phase4-protocol-runtime-evidence-pack.sh" \
  --bank-id 6275 \
  --base-url "http://127.0.0.1:65531" \
  --gs-base-url "http://127.0.0.1:65532" \
  --transport host \
  --run-security-probe true \
  --allow-missing-runtime true \
  --out-dir "${OUT_DIR}" >/tmp/phase4-runtime-evidence-pack-degraded-smoke.reportpath.txt

report_file="$(sed -n 's/^report=//p' /tmp/phase4-runtime-evidence-pack-degraded-smoke.reportpath.txt | tail -n1)"
if [[ -z "${report_file}" || ! -f "${report_file}" ]]; then
  echo "FAIL: report file not produced" >&2
  exit 1
fi

grep -q -- '- runtime_readiness: ' "${report_file}" || {
  echo "FAIL: runtime_readiness status missing" >&2
  sed -n '1,80p' "${report_file}" >&2
  exit 2
}
grep -q -- '- parity_check: SKIP_RUNTIME_NOT_READY' "${report_file}" || {
  echo "FAIL: parity_check status not marked SKIP_RUNTIME_NOT_READY" >&2
  exit 3
}
grep -q -- '- wallet_shadow_probe: SKIP_RUNTIME_NOT_READY' "${report_file}" || {
  echo "FAIL: wallet_shadow_probe status not marked SKIP_RUNTIME_NOT_READY" >&2
  exit 4
}
grep -q -- '- json_security_probe: SKIP_RUNTIME_NOT_READY' "${report_file}" || {
  echo "FAIL: json_security_probe status not marked SKIP_RUNTIME_NOT_READY" >&2
  exit 5
}
grep -q 'note: runtime probes skipped because readiness failed and allowMissingRuntime=true' "${report_file}" || {
  echo "FAIL: readiness skip note missing" >&2
  exit 6
}

echo "PASS: degraded Phase 4 runtime evidence classification works"
echo "  report: ${report_file}"
