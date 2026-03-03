# Pack Integrity Report

## Canonical Source Path Used For Export
- /Users/alexb/Documents/Dev/Dev_new/docs/gs/

## File Counts By Category
- canonical docs: 10
- fixtures: 20
- schemas: 16
- obsolete files: 9
- supplemental files: 1

## Top-Level Canonical Files
```txt
README.md
bootstrap-config-contract.md
browser-runtime-api-contract.md
browser-error-codes.md
browser-runtime-sequence-diagrams.md
release-registration-contract.md
enable-disable-canary-rollback.md
internal-slot-runtime-contract.md
math-package-spec.md
rng-ownership-decision.md
contract-lock.json
UPSTREAM_PACK_STATUS.md
PACK_INTEGRITY_REPORT.md
FILE_HASH_MANIFEST.sha256
```

## Fixture Files
```txt
fixtures/bootstrap.request.json
fixtures/bootstrap.response.json
fixtures/closegame.request.json
fixtures/closegame.response.json
fixtures/error.BOOTSTRAP_CONFIG_MISMATCH.json
fixtures/error.IDEMPOTENCY_KEY_REUSE.json
fixtures/error.INVALID_REQUEST_COUNTER.json
fixtures/error.STATE_VERSION_MISMATCH.json
fixtures/featureaction.request.json
fixtures/featureaction.response.json
fixtures/gethistory.request.json
fixtures/gethistory.response.json
fixtures/opengame.request.json
fixtures/opengame.response.json
fixtures/playround.duplicate.response.json
fixtures/playround.request.json
fixtures/playround.response.json
fixtures/release-registration.sample.json
fixtures/resumegame.request.json
fixtures/resumegame.response.json
```

## Schema Files
```txt
schemas/bootstrap.request.schema.json
schemas/bootstrap.response.schema.json
schemas/closegame.request.schema.json
schemas/closegame.response.schema.json
schemas/error-envelope.schema.json
schemas/featureaction.request.schema.json
schemas/featureaction.response.schema.json
schemas/gethistory.request.schema.json
schemas/gethistory.response.schema.json
schemas/opengame.request.schema.json
schemas/opengame.response.schema.json
schemas/playround.request.schema.json
schemas/playround.response.schema.json
schemas/release-registration.schema.json
schemas/resumegame.request.schema.json
schemas/resumegame.response.schema.json
```

## Proof: No Stale /Users/alexb/Documents/Dev/ References In Canonical Docs
```txt
<no matches>
```

## Proof: /slot/v1/history Appears Only In Obsolete Markers
Canonical docs:
```txt
<no matches>
```
Obsolete markers:
```txt
6:- Legacy endpoint alias `/slot/v1/history` is obsolete; canonical endpoint is `POST /slot/v1/gethistory`.
```

## Proof: No /v1/* Browser Endpoint Naming In Canonical Docs
```txt
<no matches>
```

## Proof: All JSON Parse Checks Pass
```txt
json-parse: OK
```

## Proof: Contract-Lock Verification Passes
```txt
contract-lock-verify: OK
counts markdown=7 fixtures=20 schemas=16
```

## Export Proof
- archive filename: gs-canonical-pack-clean-final-20260303T080756Z.tar.gz
- archive SHA-256: computed and printed after export command completion
- exact artifact Gamesv1 should mirror: this archive from /Users/alexb/Documents/Dev/Dev_new/docs/gs/
