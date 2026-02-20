#!/usr/bin/env bash
set -euo pipefail

MODE=""
BONUS_PASS_KEY=""

EXT_BONUS_ID=""
EXTERNAL_BANK_ID=""
BONUS_ID=""

USER_ID=""
TYPE=""
AMOUNT=""
MULTIPLIER=""
MAX_WIN_MULTIPLIER=""
GAMES=""
GAME_IDS=""
EXP_DATE=""
COMMENT=""
DESCRIPTION=""
AUTO_RELEASE=""

usage() {
  cat <<USAGE
Usage:
  $(basename "$0") --mode check --ext-bonus-id ID --external-bank-id BANK --bonus-pass-key KEY
  $(basename "$0") --mode cancel --bonus-id ID --bonus-pass-key KEY
  $(basename "$0") --mode award --user-id U --external-bank-id BANK --type TYPE --amount AMOUNT --multiplier M --games MODE --exp-date DATE --ext-bonus-id ID --bonus-pass-key KEY [optional fields]

Required options:
  --mode check|award|cancel
  --bonus-pass-key VALUE

Common options:
  --external-bank-id VALUE
  --ext-bonus-id VALUE
  --bonus-id VALUE

Award mode options:
  --user-id VALUE
  --type VALUE
  --amount VALUE
  --multiplier VALUE
  --games VALUE
  --exp-date VALUE
  --game-ids VALUE              Optional
  --max-win-multiplier VALUE    Optional
  --comment VALUE               Optional
  --description VALUE           Optional
  --auto-release true|false     Optional
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --mode) MODE="$2"; shift 2 ;;
    --bonus-pass-key) BONUS_PASS_KEY="$2"; shift 2 ;;
    --ext-bonus-id) EXT_BONUS_ID="$2"; shift 2 ;;
    --external-bank-id) EXTERNAL_BANK_ID="$2"; shift 2 ;;
    --bonus-id) BONUS_ID="$2"; shift 2 ;;
    --user-id) USER_ID="$2"; shift 2 ;;
    --type) TYPE="$2"; shift 2 ;;
    --amount) AMOUNT="$2"; shift 2 ;;
    --multiplier) MULTIPLIER="$2"; shift 2 ;;
    --max-win-multiplier) MAX_WIN_MULTIPLIER="$2"; shift 2 ;;
    --games) GAMES="$2"; shift 2 ;;
    --game-ids) GAME_IDS="$2"; shift 2 ;;
    --exp-date) EXP_DATE="$2"; shift 2 ;;
    --comment) COMMENT="$2"; shift 2 ;;
    --description) DESCRIPTION="$2"; shift 2 ;;
    --auto-release) AUTO_RELEASE="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "$MODE" || -z "$BONUS_PASS_KEY" ]]; then
  usage
  exit 1
fi

build_concat() {
  case "$MODE" in
    check)
      if [[ -z "$EXT_BONUS_ID" || -z "$EXTERNAL_BANK_ID" ]]; then
        echo "check mode requires --ext-bonus-id and --external-bank-id" >&2
        exit 1
      fi
      printf "%s%s%s" "$EXT_BONUS_ID" "$EXTERNAL_BANK_ID" "$BONUS_PASS_KEY"
      ;;
    cancel)
      if [[ -z "$BONUS_ID" ]]; then
        echo "cancel mode requires --bonus-id" >&2
        exit 1
      fi
      printf "%s%s" "$BONUS_ID" "$BONUS_PASS_KEY"
      ;;
    award)
      if [[ -z "$USER_ID" || -z "$EXTERNAL_BANK_ID" || -z "$TYPE" || -z "$AMOUNT" || -z "$MULTIPLIER" || -z "$GAMES" || -z "$EXP_DATE" || -z "$EXT_BONUS_ID" ]]; then
        echo "award mode missing required options" >&2
        exit 1
      fi
      local concat="${USER_ID}${EXTERNAL_BANK_ID}${TYPE}${AMOUNT}${MULTIPLIER}"
      if [[ -n "$MAX_WIN_MULTIPLIER" ]]; then concat="${concat}${MAX_WIN_MULTIPLIER}"; fi
      concat="${concat}${GAMES}"
      if [[ -n "$GAME_IDS" ]]; then concat="${concat}${GAME_IDS}"; fi
      concat="${concat}${EXP_DATE}"
      if [[ -n "$COMMENT" ]]; then concat="${concat}${COMMENT}"; fi
      if [[ -n "$DESCRIPTION" ]]; then concat="${concat}${DESCRIPTION}"; fi
      concat="${concat}${EXT_BONUS_ID}"
      if [[ -n "$AUTO_RELEASE" ]]; then concat="${concat}${AUTO_RELEASE}"; fi
      concat="${concat}${BONUS_PASS_KEY}"
      printf "%s" "$concat"
      ;;
    *)
      echo "Unsupported mode: $MODE" >&2
      exit 1
      ;;
  esac
}

CONCAT="$(build_concat)"
HASH="$(printf "%s" "$CONCAT" | md5)"
HASH="${HASH##*= }"

echo "MODE=${MODE}"
echo "HASH=${HASH}"
