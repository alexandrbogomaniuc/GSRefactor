#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/alexb/Documents/Dev/Dev_new"
OUT_DIR="${ROOT}/docs/quality/local-verification"
CHECK_DOCKER_COMPOSE="true"
CHECK_GIT_DIFF="true"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --out-dir DIR              Default: ${OUT_DIR}
  --check-docker-compose B   true|false (default: ${CHECK_DOCKER_COMPOSE})
  --check-git-diff B         true|false (default: ${CHECK_GIT_DIFF})
  -h, --help                 Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --out-dir)
      OUT_DIR="$2"; shift 2 ;;
    --check-docker-compose)
      CHECK_DOCKER_COMPOSE="$2"; shift 2 ;;
    --check-git-diff)
      CHECK_GIT_DIFF="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/phase5-6-local-verification-${TS}.md"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

PASS_COUNT=0
FAIL_COUNT=0
SKIP_COUNT=0
CHECK_KEYS=(
  help_phase4_security_logic
  help_phase4_security_runtime
  help_phase8_precision_scan
  help_phase8_precision_vectors
  help_phase8_precision_buckets
  help_phase8_wave1_reporting_vectors
  help_phase8_wave1_asmoney_parity
  help_phase8_wave2_coinrule_vectors
  help_phase8_history_reporting_vectors
  help_phase8_wallet_contract_vectors
  help_phase8_nonprod_canary
  help_phase8_wave3_dualcalc_vectors
  help_phase8_wave3_applymode_vectors
  help_phase8_policy_matrix
  help_phase8_wave3_discrepancy_evidence
  help_phase8_wave3_discrepancy_export
  logic_smoke_phase4_protocol
  logic_smoke_phase8_precision_vectors
  logic_smoke_phase8_precision_buckets
  logic_smoke_phase8_wave1_reporting_vectors
  logic_smoke_phase8_wave1_asmoney_parity
  logic_smoke_phase8_wave2_coinrule_vectors
  logic_smoke_phase8_history_reporting_vectors
  logic_smoke_phase8_wallet_contract_vectors
  logic_smoke_phase8_nonprod_canary
  logic_smoke_phase8_wave3_dualcalc_vectors
  logic_smoke_phase8_wave3_applymode_vectors
  logic_smoke_phase8_policy_matrix
  logic_smoke_phase8_wave3_discrepancy_evidence
  logic_smoke_phase8_wave3_discrepancy_export
  bash_syntax_bonus
  bash_syntax_history
  bash_syntax_wallet
  bash_syntax_gameplay
  bash_syntax_mp
  help_bonus_evidence
  help_history_evidence
  help_wallet_evidence
  help_gameplay_evidence
  help_mp_policy
  help_mp_evidence
  logic_smoke_phase56
  node_bonus
  node_history
  node_mp
  checklist_json
  git_diff_check
  compose_services
)

run_check() {
  local key="$1"
  local title="$2"
  shift 2
  local out_file="${TMP_DIR}/${key}.out"
  if "$@" >"${out_file}" 2>&1; then
    echo "PASS" > "${TMP_DIR}/${key}.status"
    PASS_COUNT=$((PASS_COUNT + 1))
  else
    echo "FAIL" > "${TMP_DIR}/${key}.status"
    FAIL_COUNT=$((FAIL_COUNT + 1))
  fi
  echo "${title}" > "${TMP_DIR}/${key}.title"
}

skip_check() {
  local key="$1"
  local title="$2"
  local reason="$3"
  echo "SKIP" > "${TMP_DIR}/${key}.status"
  echo "${title}" > "${TMP_DIR}/${key}.title"
  echo "${reason}" > "${TMP_DIR}/${key}.out"
  SKIP_COUNT=$((SKIP_COUNT + 1))
}

run_check "bash_syntax_bonus" "Bash syntax: Phase 5 bonus/FRB scripts" \
  bash -lc "bash -n '${ROOT}/gs-server/deploy/scripts/phase5-bonus-frb-canary-probe.sh' && bash -n '${ROOT}/gs-server/deploy/scripts/phase5-bonus-frb-runtime-readiness-check.sh' && bash -n '${ROOT}/gs-server/deploy/scripts/phase5-bonus-frb-runtime-evidence-pack.sh'"

run_check "help_phase4_security_logic" "CLI help: Phase 4 protocol security logic smoke" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase4-protocol-security-logic-smoke.sh' --help | sed -n '1,60p'"

run_check "help_phase4_security_runtime" "CLI help: Phase 4 protocol JSON security runtime canary" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase4-protocol-json-security-canary-probe.sh' --help | sed -n '1,80p'"

run_check "help_phase8_precision_scan" "CLI help: Phase 8 precision/min-bet audit scanner" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-minbet-audit-scan.sh' --help | sed -n '1,60p'"

run_check "help_phase8_precision_vectors" "CLI help: Phase 8 precision regression vector smoke" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-regression-vector-smoke.sh' --help | sed -n '1,60p'"

run_check "help_phase8_precision_buckets" "CLI help: Phase 8 precision remediation buckets" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-remediation-buckets.sh' --help | sed -n '1,80p'"

run_check "help_phase8_wave1_reporting_vectors" "CLI help: Phase 8 Wave 1 reporting/display vector smoke" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave1-reporting-vector-smoke.sh' --help | sed -n '1,80p'"

run_check "help_phase8_wave1_asmoney_parity" "CLI help: Phase 8 Wave 1 NumberUtils.asMoney parity smoke" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave1-numberutils-asmoney-parity-smoke.sh' --help | sed -n '1,80p'"

run_check "help_phase8_wave2_coinrule_vectors" "CLI help: Phase 8 Wave 2 settings/coin-rule vector smoke" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave2-settings-coinrule-vector-smoke.sh' --help | sed -n '1,80p'"

run_check "help_phase8_history_reporting_vectors" "CLI help: Phase 8 history/reporting export precision vector smoke" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-history-reporting-export-vector-smoke.sh' --help | sed -n '1,80p'"

run_check "help_phase8_wallet_contract_vectors" "CLI help: Phase 8 wallet contract/rounding precision vector smoke" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wallet-contract-vector-smoke.sh' --help | sed -n '1,80p'"

run_check "help_phase8_nonprod_canary" "CLI help: Phase 8 non-prod precision canary readiness/evidence tools" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-nonprod-canary-readiness-check.sh' --help | sed -n '1,100p' && '${ROOT}/gs-server/deploy/scripts/phase8-precision-nonprod-canary-evidence-pack.sh' --help | sed -n '1,100p'"

run_check "help_phase8_wave3_dualcalc_vectors" "CLI help: Phase 8 Wave 3 dual-calculation comparison vector smoke" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-dualcalc-comparison-vector-smoke.sh' --help | sed -n '1,80p'"

run_check "help_phase8_wave3_applymode_vectors" "CLI help: Phase 8 Wave 3 apply-mode vector smoke" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-applymode-vector-smoke.sh' --help | sed -n '1,80p'"

run_check "help_phase8_policy_matrix" "CLI help: Phase 8 precision policy/matrix tools" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/sync-phase8-precision-policy.sh' --help | sed -n '1,60p' && '${ROOT}/gs-server/deploy/scripts/phase8-precision-verification-matrix.sh' --help | sed -n '1,100p' && '${ROOT}/gs-server/deploy/scripts/phase8-precision-policy-matrix-smoke.sh' --help | sed -n '1,60p'"

run_check "help_phase8_wave3_discrepancy_evidence" "CLI help: Phase 8 Wave 3 discrepancy evidence smoke" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-evidence-smoke.sh' --help | sed -n '1,80p'"

run_check "help_phase8_wave3_discrepancy_export" "CLI help: Phase 8 Wave 3 discrepancy export tool" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export.sh' --help | sed -n '1,100p' && '${ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export-smoke.sh' --help | sed -n '1,80p'"
run_check "help_phase8_wave3_discrepancy_compare_export" "CLI help: Phase 8 Wave 3 discrepancy compare/export tool" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-compare-export.sh' --help | sed -n '1,120p' && '${ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-compare-export-smoke.sh' --help | sed -n '1,80p'"

run_check "logic_smoke_phase4_protocol" "Executable logic smoke: Phase 4 protocol hash/replay security" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase4-protocol-security-logic-smoke.sh'"

run_check "logic_smoke_phase8_precision_vectors" "Executable logic smoke: Phase 8 precision regression vectors" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-regression-vector-smoke.sh'"

run_check "logic_smoke_phase8_precision_buckets" "Executable logic smoke: Phase 8 precision remediation buckets" \
  bash -lc "TMPP=\"\$(mktemp -d)\"; trap 'rm -rf \"\$TMPP\"' EXIT; '${ROOT}/gs-server/deploy/scripts/phase8-precision-remediation-buckets.sh' --out-dir \"\$TMPP\""

run_check "logic_smoke_phase8_wave1_reporting_vectors" "Executable logic smoke: Phase 8 Wave 1 reporting/display vectors" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave1-reporting-vector-smoke.sh'"

run_check "logic_smoke_phase8_wave1_asmoney_parity" "Executable logic smoke: Phase 8 Wave 1 NumberUtils.asMoney parity" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave1-numberutils-asmoney-parity-smoke.sh'"

run_check "logic_smoke_phase8_wave2_coinrule_vectors" "Executable logic smoke: Phase 8 Wave 2 settings/coin-rule vectors" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave2-settings-coinrule-vector-smoke.sh'"

run_check "logic_smoke_phase8_history_reporting_vectors" "Executable logic smoke: Phase 8 history/reporting export precision vectors" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-history-reporting-export-vector-smoke.sh'"

run_check "logic_smoke_phase8_wallet_contract_vectors" "Executable logic smoke: Phase 8 wallet contract/rounding precision vectors" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wallet-contract-vector-smoke.sh'"

run_check "logic_smoke_phase8_nonprod_canary" "Executable logic smoke: Phase 8 non-prod canary readiness/evidence scaffold" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-nonprod-canary-readiness-check.sh' --allow-missing-runtime true && '${ROOT}/gs-server/deploy/scripts/phase8-precision-nonprod-canary-evidence-pack.sh' --allow-missing-runtime true"

run_check "logic_smoke_phase8_wave3_dualcalc_vectors" "Executable logic smoke: Phase 8 Wave 3 dual-calculation comparison vectors" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-dualcalc-comparison-vector-smoke.sh'"

run_check "logic_smoke_phase8_wave3_applymode_vectors" "Executable logic smoke: Phase 8 Wave 3 apply-mode vectors" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-applymode-vector-smoke.sh'"

run_check "logic_smoke_phase8_policy_matrix" "Executable logic smoke: Phase 8 precision policy/matrix generator" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/sync-phase8-precision-policy.sh' && '${ROOT}/gs-server/deploy/scripts/phase8-precision-policy-matrix-smoke.sh'"

run_check "logic_smoke_phase8_wave3_discrepancy_evidence" "Executable logic smoke: Phase 8 Wave 3 discrepancy evidence scaffold" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-evidence-smoke.sh'"

run_check "logic_smoke_phase8_wave3_discrepancy_export" "Executable logic smoke: Phase 8 Wave 3 discrepancy export parser" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export-smoke.sh'"
run_check "logic_smoke_phase8_wave3_discrepancy_compare_export" "Executable logic smoke: Phase 8 Wave 3 discrepancy compare/export CLI" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-compare-export-smoke.sh'"

run_check "bash_syntax_history" "Bash syntax: Phase 5 history scripts" \
  bash -lc "bash -n '${ROOT}/gs-server/deploy/scripts/phase5-history-canary-probe.sh' && bash -n '${ROOT}/gs-server/deploy/scripts/phase5-history-runtime-readiness-check.sh' && bash -n '${ROOT}/gs-server/deploy/scripts/phase5-history-runtime-evidence-pack.sh'"

run_check "bash_syntax_wallet" "Bash syntax: Phase 5 wallet scripts" \
  bash -lc "bash -n '${ROOT}/gs-server/deploy/scripts/phase5-wallet-adapter-canary-probe.sh' && bash -n '${ROOT}/gs-server/deploy/scripts/phase5-wallet-runtime-readiness-check.sh' && bash -n '${ROOT}/gs-server/deploy/scripts/phase5-wallet-runtime-evidence-pack.sh'"

run_check "bash_syntax_gameplay" "Bash syntax: Phase 5 gameplay scripts" \
  bash -lc "bash -n '${ROOT}/gs-server/deploy/scripts/phase5-gameplay-canary-probe.sh' && bash -n '${ROOT}/gs-server/deploy/scripts/phase5-runtime-readiness-check.sh' && bash -n '${ROOT}/gs-server/deploy/scripts/phase5-gameplay-runtime-evidence-pack.sh'"

run_check "bash_syntax_mp" "Bash syntax: Phase 6 multiplayer scripts" \
  bash -lc "bash -n '${ROOT}/gs-server/deploy/scripts/phase6-multiplayer-canary-probe.sh' && bash -n '${ROOT}/gs-server/deploy/scripts/phase6-multiplayer-routing-policy-probe.sh' && bash -n '${ROOT}/gs-server/deploy/scripts/phase6-multiplayer-runtime-readiness-check.sh' && bash -n '${ROOT}/gs-server/deploy/scripts/phase6-multiplayer-runtime-evidence-pack.sh'"

run_check "help_bonus_evidence" "CLI help: Phase 5 bonus/FRB evidence-pack" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase5-bonus-frb-runtime-evidence-pack.sh' --help | sed -n '1,80p'"

run_check "help_history_evidence" "CLI help: Phase 5 history evidence-pack" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase5-history-runtime-evidence-pack.sh' --help | sed -n '1,80p'"

run_check "help_wallet_evidence" "CLI help: Phase 5 wallet evidence-pack" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase5-wallet-runtime-evidence-pack.sh' --help | sed -n '1,100p'"

run_check "help_gameplay_evidence" "CLI help: Phase 5 gameplay evidence-pack" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase5-gameplay-runtime-evidence-pack.sh' --help | sed -n '1,120p'"

run_check "help_mp_policy" "CLI help: Phase 6 multiplayer routing-policy probe" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase6-multiplayer-routing-policy-probe.sh' --help | sed -n '1,100p'"

run_check "help_mp_evidence" "CLI help: Phase 6 multiplayer evidence-pack" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase6-multiplayer-runtime-evidence-pack.sh' --help | sed -n '1,120p'"

run_check "logic_smoke_phase56" "Executable logic smoke: Phase 5/6 stores and multiplayer policy" \
  bash -lc "'${ROOT}/gs-server/deploy/scripts/phase5-6-local-logic-smoke.sh'"

run_check "node_bonus" "Node syntax: bonus-frb-service" \
  bash -lc "node --check '${ROOT}/gs-server/refactor-services/bonus-frb-service/src/server.js'"

run_check "node_history" "Node syntax: history-service" \
  bash -lc "node --check '${ROOT}/gs-server/refactor-services/history-service/src/server.js'"

run_check "node_mp" "Node syntax: multiplayer-service" \
  bash -lc "node --check '${ROOT}/gs-server/refactor-services/multiplayer-service/src/server.js' && node --check '${ROOT}/gs-server/refactor-services/multiplayer-service/src/store.js'"

run_check "checklist_json" "JSON parse: modernization checklist" \
  bash -lc "node -e 'JSON.parse(require(\"fs\").readFileSync(\"${ROOT}/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json\",\"utf8\")); console.log(\"OK\")'"

if [[ "${CHECK_GIT_DIFF}" == "true" ]]; then
  run_check "git_diff_check" "Git whitespace check" \
    bash -lc "git -C '${ROOT}' diff --check"
else
  skip_check "git_diff_check" "Git whitespace check" "Disabled by --check-git-diff=false"
fi

if [[ "${CHECK_DOCKER_COMPOSE}" == "true" ]]; then
  run_check "compose_services" "Compose config services (refactor stack)" \
    bash -lc "docker compose -f '${ROOT}/gs-server/deploy/docker/refactor/docker-compose.yml' --env-file '${ROOT}/gs-server/deploy/docker/refactor/.env' config --services | tr '\\n' ' ' | sed -E 's/[[:space:]]+$//'"
else
  skip_check "compose_services" "Compose config services (refactor stack)" "Disabled by --check-docker-compose=false"
fi

{
  echo "# Phase 5/6 Local Verification Suite (${TS} UTC)"
  echo
  echo "- scope: offline/local validation for recently implemented refactor services and tooling"
  echo "- pass: ${PASS_COUNT}"
  echo "- fail: ${FAIL_COUNT}"
  echo "- skip: ${SKIP_COUNT}"
  echo
  echo "## Summary"
  for key in "${CHECK_KEYS[@]}"; do
    [[ -f "${TMP_DIR}/${key}.status" ]] || continue
    status="$(cat "${TMP_DIR}/${key}.status")"
    title="$(cat "${TMP_DIR}/${key}.title")"
    echo "- [${status}] ${title}"
  done
  echo
  echo "## Outputs"
  for key in "${CHECK_KEYS[@]}"; do
    [[ -f "${TMP_DIR}/${key}.status" ]] || continue
    status="$(cat "${TMP_DIR}/${key}.status")"
    title="$(cat "${TMP_DIR}/${key}.title")"
    echo "### ${title}"
    echo "- status: ${status}"
    echo '```text'
    sed -n '1,220p' "${TMP_DIR}/${key}.out" | sed -E 's/[[:space:]]+$//'
    printf '\n'
    echo '```'
    echo
  done
} > "${REPORT}"

# Normalize trailing blank lines at EOF so generated reports pass git diff whitespace checks.
awk '{
  lines[++n]=$0
}
END {
  while (n > 0 && lines[n] == "") {
    n--
  }
  for (i = 1; i <= n; i++) {
    print lines[i]
  }
}' "${REPORT}" > "${REPORT}.tmp"
mv "${REPORT}.tmp" "${REPORT}"

echo "report=${REPORT}"

if [[ ${FAIL_COUNT} -gt 0 ]]; then
  exit 2
fi
