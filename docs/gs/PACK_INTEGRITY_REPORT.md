# Pack Integrity Report

## Canonical Source Path Used
- /Users/alexb/Documents/Dev/Dev_new/docs/gs/

## File Counts By Category
- canonical docs: 10
- fixtures: 20
- schemas: 16
- obsolete files: 9
- supplemental files: 1

## Proof: No stale /Users/alexb/Documents/Dev/ references in canonical docs
```txt
<no matches>
```

## Proof: /slot/v1/history appears only in obsolete markers
Canonical docs:
```txt
<no matches>
```
Obsolete markers:
```txt
6:- Legacy endpoint alias `/slot/v1/history` is obsolete; canonical endpoint is `POST /slot/v1/gethistory`.
```

## Proof: No /v1/* browser endpoint naming in canonical docs
```txt
<no matches>
```

## Proof: JSON parse checks
```txt
json-parse: OK
```

## Proof: Contract-lock verification
```txt
contract-lock-verify: OK (markdown=7, fixtures=20, schemas=16)
```

## Export Proof
- archive filename: gs_pack_upload.zip
- archive filename: gs_pack_upload.tar.gz
- statement: these are the exact upload artifacts Gamesv1 should mirror from this canonical source
