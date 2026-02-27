#!/usr/bin/env bash
set -u
ROOT="/Users/alexb/Documents/Dev/Dev_new"
OUT="/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260227-210136-hardcut-m2-wave212ab-wave213-parallel-batches"

utc_now(){ date -u +"%Y-%m-%dT%H:%M:%SZ"; }

run_step(){
  local label="$1" dir="$2" cmd="$3" logfile="$4" runner="$5"
  {
    echo "[$(utc_now)] START $label"
    echo "[$(utc_now)] DIR $dir"
    echo "[$(utc_now)] CMD $cmd"
  } | tee -a "$runner" >> "$logfile"
  (
    cd "$ROOT/$dir" && eval "$cmd"
  ) >> "$logfile" 2>&1
  local rc=$?
  if [ $rc -eq 0 ]; then
    echo "[$(utc_now)] PASS $label rc=$rc" | tee -a "$runner" >> "$logfile"
  else
    echo "[$(utc_now)] FAIL $label rc=$rc" | tee -a "$runner" >> "$logfile"
  fi
  return $rc
}

write_status(){
  local file="$1" status="$2" failed_step="$3" failed_cmd="$4"
  {
    echo "status=$status"
    echo "first_failed_step=$failed_step"
    echo "failed_command=$failed_cmd"
  } > "$file"
}

# 1) Stabilization prewarm first
STAB_LOG="$OUT/stabilization-prewarm-rerun5.log"
: > "$STAB_LOG"
{
  echo "[$(utc_now)] START stabilization-prewarm"
  echo "[$(utc_now)] DIR gs-server/sb-utils"
  echo "[$(utc_now)] CMD mvn -DskipTests install"
} >> "$STAB_LOG"
(
  cd "$ROOT/gs-server/sb-utils" && mvn -DskipTests install
) >> "$STAB_LOG" 2>&1
stab_rc=$?
if [ $stab_rc -eq 0 ]; then
  echo "[$(utc_now)] PASS stabilization-prewarm rc=$stab_rc" >> "$STAB_LOG"
else
  echo "[$(utc_now)] FAIL stabilization-prewarm rc=$stab_rc" >> "$STAB_LOG"
fi

# Shared steps arrays
step_dir(){
  case "$1" in
    STEP01) echo "gs-server/common" ;;
    STEP02) echo "gs-server/common-wallet" ;;
    STEP03) echo "gs-server/sb-utils" ;;
    STEP04) echo "gs-server/common-promo" ;;
    STEP05) echo "gs-server/cassandra-cache/common-persisters" ;;
    STEP06) echo "gs-server/game-server/common-gs" ;;
    STEP07) echo "gs-server/game-server/web-gs" ;;
    STEP08) echo "mp-server/persistance" ;;
    STEP09) echo "." ;;
  esac
}
step_cmd(){
  case "$1" in
    STEP01) echo "mvn -DskipTests install" ;;
    STEP02) echo "mvn -DskipTests install" ;;
    STEP03) echo "mvn -DskipTests install" ;;
    STEP04) echo "mvn -DskipTests install" ;;
    STEP05) echo "mvn -DskipTests install" ;;
    STEP06) echo "mvn -Dcluster.properties=local/local-machine.properties -DskipTests install" ;;
    STEP07) echo "mvn -Dcluster.properties=local/local-machine.properties -DskipTests package" ;;
    STEP08) echo "mvn -DskipTests install" ;;
    STEP09) echo "node gs-server/deploy/scripts/refactor-onboard.mjs smoke" ;;
  esac
}

run_fast_gate(){
  local batch="$1" runner="$2" statusf="$3"
  : > "$runner"
  local first_failed="NONE" failed_cmd="NONE" status="PASS"
  for s in STEP01 STEP02 STEP03 STEP04 STEP05 STEP06 STEP07 STEP08 STEP09; do
    d="$(step_dir "$s")"
    c="$(step_cmd "$s")"
    if ! run_step "$s" "$d" "$c" "$runner" "$runner"; then
      first_failed="$s"
      failed_cmd="$c"
      status="FAIL"
      break
    fi
  done
  write_status "$statusf" "$status" "$first_failed" "$failed_cmd"
}

# 2) fast gate batchA rerun5
run_fast_gate "A" "$OUT/fast-gate-runner-batchA-rerun5.log" "$OUT/fast-gate-status-batchA-rerun5.txt"
# 3) fast gate batchB rerun5
run_fast_gate "B" "$OUT/fast-gate-runner-batchB-rerun5.log" "$OUT/fast-gate-status-batchB-rerun5.txt"

# 4) Full matrix rerun5
VAL_RUNNER="$OUT/validation-runner-rerun5.log"
PRE_STATUS="$OUT/prewarm-status-rerun5.txt"
VAL_STATUS="$OUT/validation-status-rerun5.txt"
: > "$VAL_RUNNER"

pre_failed="NONE"
pre_failed_cmd="NONE"
pre_status="PASS"

for p in PRE01 PRE02 PRE03; do
  case "$p" in
    PRE01) pd="gs-server/utils"; pc="mvn -DskipTests install" ;;
    PRE02) pd="gs-server/sb-utils"; pc="mvn -DskipTests install" ;;
    PRE03) pd="gs-server/common-promo"; pc="mvn -DskipTests install" ;;
  esac
  if ! run_step "$p" "$pd" "$pc" "$VAL_RUNNER" "$VAL_RUNNER"; then
    pre_failed="$p"
    pre_failed_cmd="$pc"
    pre_status="FAIL"
    break
  fi
done
write_status "$PRE_STATUS" "$pre_status" "$pre_failed" "$pre_failed_cmd"

val_failed="NONE"
val_failed_cmd="NONE"
val_status="PASS"

if [ "$pre_status" = "PASS" ]; then
  for s in STEP01 STEP02 STEP03 STEP04 STEP05 STEP06 STEP07 STEP08 STEP09; do
    d="$(step_dir "$s")"
    c="$(step_cmd "$s")"
    if ! run_step "$s" "$d" "$c" "$VAL_RUNNER" "$VAL_RUNNER"; then
      val_failed="$s"
      val_failed_cmd="$c"
      val_status="FAIL"
      if [ "$s" = "STEP09" ]; then
        RETRY_LOG="$OUT/STEP09-rerun5-retry1.log"
        : > "$RETRY_LOG"
        run_step "STEP09-retry1" "." "node gs-server/deploy/scripts/refactor-onboard.mjs smoke" "$RETRY_LOG" "$VAL_RUNNER" || true
      fi
      break
    fi
  done
else
  val_failed="$pre_failed"
  val_failed_cmd="$pre_failed_cmd"
  val_status="FAIL"
fi
write_status "$VAL_STATUS" "$val_status" "$val_failed" "$val_failed_cmd"

# summary artifact
{
  echo "stabilization_rc=$stab_rc"
  echo "fast_gate_batchA_status=$(cut -d= -f2 "$OUT/fast-gate-status-batchA-rerun5.txt" | sed -n '1p')"
  echo "fast_gate_batchA_first_failed_step=$(cut -d= -f2 "$OUT/fast-gate-status-batchA-rerun5.txt" | sed -n '2p')"
  echo "fast_gate_batchB_status=$(cut -d= -f2 "$OUT/fast-gate-status-batchB-rerun5.txt" | sed -n '1p')"
  echo "fast_gate_batchB_first_failed_step=$(cut -d= -f2 "$OUT/fast-gate-status-batchB-rerun5.txt" | sed -n '2p')"
  echo "prewarm_status=$(cut -d= -f2 "$OUT/prewarm-status-rerun5.txt" | sed -n '1p')"
  echo "prewarm_first_failed_step=$(cut -d= -f2 "$OUT/prewarm-status-rerun5.txt" | sed -n '2p')"
  echo "validation_status=$(cut -d= -f2 "$OUT/validation-status-rerun5.txt" | sed -n '1p')"
  echo "validation_first_failed_step=$(cut -d= -f2 "$OUT/validation-status-rerun5.txt" | sed -n '2p')"
} > "$OUT/rerun5-summary.txt"

exit 0
