# JSON Protocol Security Baseline v1 (Casino Side)

Last updated: 2026-02-20 UTC
Scope: JSON-over-HTTPS integration mode as backward-compatible alternative to legacy XML.

## 1) Core rule set
1. Transport is HTTPS only.
2. `Content-Type: application/json` is mandatory for POST.
3. `Hash` request header carries HMAC-SHA256 signature (hex lowercase).
4. Secret key is shared per bank integration profile (not global hardcoded key).
5. Internal GS business model stays canonical; JSON/XML conversion remains in adapters only.

## 2) Hash input rules
1. POST: hash input is request body string in compact JSON form (no extra spaces/newlines).
2. GET: hash input is endpoint-specific ordered concatenation of parameter values.
3. Optional missing parameter contributes empty string (no `null` literals inserted).
4. Header key name default is `Hash`.

## 3) Verification behavior (backward-compatible rollout)
1. `shadow` mode first:
   - verify hash and log pass/fail,
   - do not reject request yet.
2. canary enforce mode by selected banks:
   - reject on missing/invalid hash (`401`/`403` by policy),
   - keep legacy XML paths unchanged.
3. full enforce only after parity and canary gates pass.

## 4) Security hardening beyond base tip (best practice)
1. Add replay protection for JSON mode with:
   - `X-Timestamp` (max skew window, e.g. 300s),
   - `X-Nonce` (single-use cache check, Redis recommended).
2. Use constant-time hash compare.
3. Rotate secrets safely via config versions and dual-key overlap window.
4. Never log raw secret/hash inputs in plaintext logs.

## 5) Bank-level config contract (target)
Per bank profile in Config Service/portal:
- `protocolMode`: `XML` | `JSON`
- `jsonSecurity.hash.enabled`: bool
- `jsonSecurity.hash.headerName`: string (default `Hash`)
- `jsonSecurity.hash.algorithm`: `HMAC-SHA256`
- `jsonSecurity.hash.enforcementMode`: `OFF` | `SHADOW` | `ENFORCE`
- `jsonSecurity.hash.exemptEndpoints`: string[] (no hash required for selected endpoints)
- `jsonSecurity.hash.getHashRules`: map endpoint -> ordered field list for GET hash input concatenation
- `jsonSecurity.replay.enabled`: bool
- `jsonSecurity.replay.windowSeconds`: int
- `jsonSecurity.replay.nonceTtlSeconds`: int
- `jsonSecurity.secretRef`: vault reference (never raw secret in source)

## 6) Rollout plan
1. Add adapter-layer verifier with SHADOW mode metrics.
2. Enable JSON+hash for one canary bank only.
3. Compare parity against XML responses and latency impact.
4. Expand banks in waves, then enforce globally per approved bank.

## 7) Why Redis is useful here
Redis is useful for replay defense and short-lived protocol state:
- nonce cache (`bankId + nonce`),
- timestamp window checks,
- temporary state blobs for deterministic math workflows.
Keep source of truth/audit in durable storage; Redis is fast anti-replay + transient state only.
