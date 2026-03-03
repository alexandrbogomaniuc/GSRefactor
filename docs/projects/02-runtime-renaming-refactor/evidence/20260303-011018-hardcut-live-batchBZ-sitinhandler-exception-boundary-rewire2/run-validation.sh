#!/usr/bin/env bash
set -u
ROOT="/Users/alexb/Documents/Dev/Dev_new"
OUT="/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-011018-hardcut-live-batchBZ-sitinhandler-exception-boundary-rewire2"

utc_now(){ date -u +"%Y-%m-%dT%H:%M:%SZ"; }

run_step(){
  local label="$1" dir="$2" cmd="$3" logfile="$4"
  {
    echo "[$(utc_now)] START $label"
    echo "[$(utc_now)] DIR $dir"
    echo "[$(utc_now)] CMD $cmd"
  } >> "$logfile"
  (
    cd "$ROOT/$dir" && eval "$cmd"
  ) >> "$logfile" 2>&1
  local rc=$?
  echo "[$(utc_now)] $([ $rc -eq 0 ] && echo PASS || echo FAIL) $label rc=$rc" >> "$logfile"
  return $rc
}

run_step_timeout(){
  local label="$1" dir="$2" cmd="$3" logfile="$4" timeout_s="$5"
  {
    echo "[$(utc_now)] START $label"
    echo "[$(utc_now)] DIR $dir"
    echo "[$(utc_now)] CMD $cmd (timeout=${timeout_s}s)"
  } >> "$logfile"
  (
    cd "$ROOT/$dir" || exit 127
    bash -lc "$cmd" >> "$logfile" 2>&1 &
    local cpid=$!
    local elapsed=0
    while kill -0 "$cpid" 2>/dev/null; do
      if [ $elapsed -ge "$timeout_s" ]; then
        kill -TERM "$cpid" 2>/dev/null || true
        sleep 2
        kill -KILL "$cpid" 2>/dev/null || true
        wait "$cpid" 2>/dev/null || true
        exit 124
      fi
      sleep 1
      elapsed=$((elapsed + 1))
    done
    wait "$cpid"
  ) >> "$logfile" 2>&1
  local rc=$?
  echo "[$(utc_now)] $([ $rc -eq 0 ] && echo PASS || echo FAIL) $label rc=$rc" >> "$logfile"
  return $rc
}

COMMON_LOG="$OUT/fast-gate-common-games.log"
BOTS_LOG="$OUT/fast-gate-bots.log"
WEB_LOG="$OUT/fast-gate-web.log"
COTG_LOG="$OUT/fast-gate-cotg-consumer.log"
: > "$COMMON_LOG"; : > "$BOTS_LOG"; : > "$WEB_LOG"; : > "$COTG_LOG"

run_step "common-games" "mp-server/games/common-games" "mvn -DskipTests install" "$COMMON_LOG"; rc1=$?
run_step "bots" "mp-server" "mvn -f pom.xml -pl bots -am -DskipTests compile" "$BOTS_LOG"; rcb=$?
run_step "web" "mp-server" "mvn -DskipTests -pl web -am compile" "$WEB_LOG"; rc2=$?
run_step "cotg-consumer" "mp-server/games/clashofthegods" "mvn -DskipTests install" "$COTG_LOG"; rc3=$?

{
  echo "common_games_compile=$rc1"
  echo "bots_compile=$rcb"
  echo "web_compile=$rc2"
  echo "cotg_compile=$rc3"
} > "$OUT/fast-gate-status.txt"

grep -n "^\[ERROR\]" "$BOTS_LOG" | head -n 40 > "$OUT/fast-gate-bots-first-fail.txt" || true
grep -n "^\[ERROR\]" "$WEB_LOG" | head -n 40 > "$OUT/fast-gate-web-first-fail.txt" || true
grep -n "^\[ERROR\]" "$COTG_LOG" | head -n 40 > "$OUT/fast-gate-cotg-first-fail.txt" || true

write_status(){
  local file="$1" status="$2" failed_step="$3" failed_cmd="$4"
  {
    echo "status=$status"
    echo "first_failed_step=$failed_step"
    echo "failed_command=$failed_cmd"
  } > "$file"
}

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
    STEP01|STEP02|STEP03|STEP04|STEP05|STEP08) echo "mvn -DskipTests install" ;;
    STEP06|STEP07) echo "mvn -DskipTests -Dcluster.properties=local/local-machine.properties install" ;;
    STEP09) echo "node gs-server/deploy/scripts/refactor-onboard.mjs smoke" ;;
  esac
}

run_fast_gate(){
  local runner="$1" statusf="$2"
  : > "$runner"
  local first_failed="NONE" failed_cmd="NONE" status="PASS"
  for s in STEP01 STEP02 STEP03 STEP04 STEP05 STEP06 STEP07 STEP08 STEP09; do
    local d="$(step_dir "$s")"
    local c="$(step_cmd "$s")"
    if [ "$s" = "STEP09" ]; then
      run_step_timeout "$s" "$d" "$c" "$runner" 300
      rc=$?
    else
      run_step "$s" "$d" "$c" "$runner"
      rc=$?
    fi
    if [ $rc -ne 0 ]; then
      first_failed="$s"
      failed_cmd="$c"
      status="FAIL"
      break
    fi
  done
  write_status "$statusf" "$status" "$first_failed" "$failed_cmd"
}

run_fast_gate "$OUT/fast-gate-runner-batchA-rerun1.log" "$OUT/fast-gate-status-batchA-rerun1.txt"
run_fast_gate "$OUT/fast-gate-runner-batchB-rerun1.log" "$OUT/fast-gate-status-batchB-rerun1.txt"

VAL_RUNNER="$OUT/validation-runner-rerun1.log"
PRE_STATUS="$OUT/prewarm-status-rerun1.txt"
VAL_STATUS="$OUT/validation-status-rerun1.txt"
: > "$VAL_RUNNER"

pre_failed="NONE"; pre_failed_cmd="NONE"; pre_status="PASS"
for p in PRE01 PRE02 PRE03; do
  case "$p" in
    PRE01) pd="gs-server/utils"; pc="mvn -DskipTests install" ;;
    PRE02) pd="gs-server/sb-utils"; pc="mvn -DskipTests install" ;;
    PRE03) pd="gs-server/common-promo"; pc="mvn -DskipTests install" ;;
  esac
  if ! run_step "$p" "$pd" "$pc" "$VAL_RUNNER"; then
    pre_failed="$p"; pre_failed_cmd="$pc"; pre_status="FAIL"; break
  fi
done
write_status "$PRE_STATUS" "$pre_status" "$pre_failed" "$pre_failed_cmd"

val_failed="NONE"; val_failed_cmd="NONE"; val_status="PASS"; step09_retry_rc="SKIP"
if [ "$pre_status" = "PASS" ]; then
  for s in STEP01 STEP02 STEP03 STEP04 STEP05 STEP06 STEP07 STEP08 STEP09; do
    d="$(step_dir "$s")"; c="$(step_cmd "$s")"
    if [ "$s" = "STEP09" ]; then
      run_step_timeout "$s" "$d" "$c" "$VAL_RUNNER" 300
      rc=$?
    else
      run_step "$s" "$d" "$c" "$VAL_RUNNER"
      rc=$?
    fi
    if [ $rc -ne 0 ]; then
      val_failed="$s"; val_failed_cmd="$c"; val_status="FAIL"
      if [ "$s" = "STEP09" ]; then
        RETRY_LOG="$OUT/STEP09-rerun1-retry1.log"
        : > "$RETRY_LOG"
        run_step_timeout "STEP09-retry1" "." "node gs-server/deploy/scripts/refactor-onboard.mjs smoke" "$RETRY_LOG" 300
        step09_retry_rc=$?
      fi
      break
    fi
  done
else
  val_failed="$pre_failed"; val_failed_cmd="$pre_failed_cmd"; val_status="FAIL"
fi
write_status "$VAL_STATUS" "$val_status" "$val_failed" "$val_failed_cmd"

{
  echo "fast_gate_batchA=status=$(cut -d= -f2 "$OUT/fast-gate-status-batchA-rerun1.txt" | sed -n '1p');first_failed_step=$(cut -d= -f2 "$OUT/fast-gate-status-batchA-rerun1.txt" | sed -n '2p');failed_command=$(cut -d= -f2- "$OUT/fast-gate-status-batchA-rerun1.txt" | sed -n '3p')"
  echo "fast_gate_batchB=status=$(cut -d= -f2 "$OUT/fast-gate-status-batchB-rerun1.txt" | sed -n '1p');first_failed_step=$(cut -d= -f2 "$OUT/fast-gate-status-batchB-rerun1.txt" | sed -n '2p');failed_command=$(cut -d= -f2- "$OUT/fast-gate-status-batchB-rerun1.txt" | sed -n '3p')"
  echo "prewarm=status=$(cut -d= -f2 "$OUT/prewarm-status-rerun1.txt" | sed -n '1p');first_failed_step=$(cut -d= -f2 "$OUT/prewarm-status-rerun1.txt" | sed -n '2p');failed_command=$(cut -d= -f2- "$OUT/prewarm-status-rerun1.txt" | sed -n '3p')"
  echo "validation=status=$(cut -d= -f2 "$OUT/validation-status-rerun1.txt" | sed -n '1p');first_failed_step=$(cut -d= -f2 "$OUT/validation-status-rerun1.txt" | sed -n '2p');failed_command=$(cut -d= -f2- "$OUT/validation-status-rerun1.txt" | sed -n '3p')"
  echo "step09_retry1=status=$([ "$step09_retry_rc" = "0" ] && echo PASS || echo FAIL);rc=$step09_retry_rc"
} > "$OUT/validation-summary-rerun1.txt"

exit 0
