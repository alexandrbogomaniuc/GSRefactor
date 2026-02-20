# Phase 4 JSON Protocol Conformance Profile (2026-02-20 17:30:58 UTC)

## Goal
Capture JSON protocol behavior from the provided production example and map it to the ABS protocol-adapter implementation using neutral naming only.

## Conformance rules applied
1. Transport/protocol
- JSON over HTTPS.
- `Content-Type: application/json` for POST.

2. Hash header
- Header name default: `Hash`.
- Algorithm: `HMAC-SHA256`.
- POST hash input: compact request body string.
- GET hash input: ordered concatenation of configured query fields.

3. Hash-exempt endpoints (configurable)
- Account info
- Balance
- Cash bonus check
- FRB check
- Viewer feeds/endpoints
- Generic feeds endpoints

4. Enforcement and rollout
- `OFF`, `SHADOW`, `ENFORCE` modes.
- Default remains non-breaking (`hash.enabled=false`, route flag off).

## Implemented mapping (ABS)
- Service:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/protocol-adapter/src/store.js`
- New config keys:
  - `PROTOCOL_ADAPTER_JSON_HASH_EXEMPT_ENDPOINTS`
  - `PROTOCOL_ADAPTER_JSON_GET_HASH_RULES`
- Default profile in cluster config:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties`

## Quick validation evidence
```bash
# GET hash rule validation
STORE_FILE=/tmp/protocol-adapter-conformance-test-1.json \
PROTOCOL_ADAPTER_BANK_MODES=6275:JSON \
PROTOCOL_ADAPTER_JSON_HASH_ENABLED=true \
PROTOCOL_ADAPTER_JSON_HASH_ENFORCEMENT_MODE=ENFORCE \
PROTOCOL_ADAPTER_JSON_HMAC_SECRETS=6275:SecretKey001 \
PROTOCOL_ADAPTER_JSON_GET_HASH_RULES='/api/v1/bonus/cash/history:bankId+userId+timeZone' \
node <inline-test>
# Result: get_hash_status=200, hashReason=ok, verified=true

# Exempt endpoint validation
STORE_FILE=/tmp/protocol-adapter-conformance-test-2.json \
PROTOCOL_ADAPTER_BANK_MODES=6275:JSON \
PROTOCOL_ADAPTER_JSON_HASH_ENABLED=true \
PROTOCOL_ADAPTER_JSON_HASH_ENFORCEMENT_MODE=ENFORCE \
PROTOCOL_ADAPTER_JSON_HMAC_SECRETS=6275:SecretKey001 \
PROTOCOL_ADAPTER_JSON_HASH_EXEMPT_ENDPOINTS='/api/v1/account/info,/api/v1/balance' \
node <inline-test>
# Result: exempt_status=200, hashReason=hash_exempt_endpoint, required=false
```

## Naming policy
- No vendor naming is used in new implementation docs/contracts.
- Only neutral names are used: `ABS`, `casino side`, `integrator`, `protocol-adapter`.
