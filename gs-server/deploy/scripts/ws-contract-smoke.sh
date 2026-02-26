#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ROOT_DIR="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
SAMPLES_DIR="${1:-$ROOT_DIR/docs/contracts/ws-v1/examples}"

if [[ ! -d "$SAMPLES_DIR" ]]; then
  echo "Samples directory not found: $SAMPLES_DIR" >&2
  exit 1
fi

python3 - "$SAMPLES_DIR" <<'PY'
import json
import os
import re
import sys

samples_dir = sys.argv[1]
files = sorted(
    f for f in os.listdir(samples_dir)
    if f.endswith(".json")
)

if not files:
    print(f"No sample files found in {samples_dir}", file=sys.stderr)
    sys.exit(1)

required_base = [
    "version",
    "type",
    "traceId",
    "sessionId",
    "bankId",
    "gameId",
    "timestamp",
    "seq",
    "payload",
]

amount_re = re.compile(r"^-?\d+(\.\d{1,6})?$")
error_categories = {"validation", "state", "dependency", "auth", "rate_limit", "internal"}

def assert_keys(obj, keys, scope, errors):
    for key in keys:
        if key not in obj:
            errors.append(f"{scope}: missing key '{key}'")

def validate_base(data, errors):
    if not isinstance(data, dict):
        errors.append("root: expected object")
        return
    assert_keys(data, required_base, "root", errors)
    if data.get("version") != "1.0":
        errors.append("root.version: expected '1.0'")
    if not isinstance(data.get("seq"), int) or data.get("seq", -1) < 0:
        errors.append("root.seq: expected non-negative integer")
    if not isinstance(data.get("payload"), dict):
        errors.append("root.payload: expected object")

def validate_amount(value, scope, errors):
    if not isinstance(value, str) or not amount_re.fullmatch(value):
        errors.append(f"{scope}: expected decimal string up to 6 fraction digits")

def validate_type_specific(data, errors):
    message_type = data.get("type")
    payload = data.get("payload")
    if not isinstance(payload, dict):
        return

    if message_type == "BET_REQUEST":
        assert_keys(data, ["operationId"], "root", errors)
        assert_keys(payload, ["roundId", "amount", "currency"], "payload", errors)
        validate_amount(payload.get("amount"), "payload.amount", errors)
    elif message_type == "SETTLE_REQUEST":
        assert_keys(data, ["operationId"], "root", errors)
        assert_keys(payload, ["roundId", "amount", "currency", "resultType"], "payload", errors)
        validate_amount(payload.get("amount"), "payload.amount", errors)
        if payload.get("resultType") not in {"WIN", "LOSE", "PUSH", "CANCEL"}:
            errors.append("payload.resultType: expected WIN|LOSE|PUSH|CANCEL")
    elif message_type == "RECONNECT_REQUEST":
        assert_keys(payload, ["lastAckSeq"], "payload", errors)
        if not isinstance(payload.get("lastAckSeq"), int) or payload.get("lastAckSeq", -1) < 0:
            errors.append("payload.lastAckSeq: expected non-negative integer")
    elif message_type == "ERROR":
        assert_keys(payload, ["code", "category", "message", "retryable"], "payload", errors)
        if payload.get("category") not in error_categories:
            errors.append("payload.category: unknown category")
        if not isinstance(payload.get("retryable"), bool):
            errors.append("payload.retryable: expected boolean")
    elif message_type == "SESSION_SYNC":
        assert_keys(payload, ["lastAppliedSeq", "balance"], "payload", errors)
        if not isinstance(payload.get("lastAppliedSeq"), int) or payload.get("lastAppliedSeq", -1) < 0:
            errors.append("payload.lastAppliedSeq: expected non-negative integer")
        balance = payload.get("balance")
        if not isinstance(balance, dict):
            errors.append("payload.balance: expected object")
        else:
            assert_keys(balance, ["amount", "currency"], "payload.balance", errors)
            validate_amount(balance.get("amount"), "payload.balance.amount", errors)
    else:
        errors.append(f"root.type: unsupported sample type '{message_type}'")

failed = 0
for file_name in files:
    full_path = os.path.join(samples_dir, file_name)
    errors = []
    try:
        with open(full_path, "r", encoding="utf-8") as fh:
            data = json.load(fh)
    except Exception as exc:
        print(f"[FAIL] {file_name}: invalid json ({exc})")
        failed += 1
        continue

    validate_base(data, errors)
    validate_type_specific(data, errors)

    if errors:
        failed += 1
        print(f"[FAIL] {file_name}")
        for error in errors:
            print(f"  - {error}")
    else:
        print(f"[PASS] {file_name}")

if failed:
    print(f"Conformance smoke failed: {failed} file(s)", file=sys.stderr)
    sys.exit(1)

print(f"Conformance smoke passed: {len(files)} file(s)")
PY
