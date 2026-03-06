# Sanitization Checklist

## Scope

- Run scans only on the donor folder or the current phase folder.
- Keep scans narrow so findings stay actionable.

## Required patterns

- `authToken=`
- `token=`
- `session=`
- `cookie`

## Checklist

- [ ] Scan completed before commit
- [ ] Raw token/session/cookie values redacted
- [ ] README or equivalent notes mention scan result
- [ ] No donor binaries or raw asset dumps staged
- [ ] Local-only donor files are excluded through `.git/info/exclude`

## Result format

- PASS: no non-redacted secret-like values found
- FAIL: one or more values found and must be redacted before commit
