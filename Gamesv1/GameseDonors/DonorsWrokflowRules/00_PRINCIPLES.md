# 00 Principles

## Truth contract

- Do not claim a mechanic, control, payout rule, animation state, or UI behavior unless there is saved evidence for it.
- Every factual statement in synthesis docs must point to a file path, and to a timestamp when the source is video.
- If a claim cannot be proven from saved artifacts, mark it `INFERENCE` or `UNOBSERVED`.
- Do not backfill confidence with memory, expectation, or genre convention.

## `INFERENCE` vs `UNOBSERVED`

- Use `INFERENCE` only when multiple saved artifacts strongly support a conclusion that is not directly spelled out.
- Keep `INFERENCE` statements narrow and reversible.
- Use `UNOBSERVED` when the state, rule, or asset was not actually seen in committed evidence.
- Never rewrite `UNOBSERVED` into a positive claim just because it is likely.

## Video validity rule

- A video is invalid if the donor UI is mostly black, blank, frozen, or otherwise unusable.
- Invalid videos do not count toward coverage minimums.
- If the first recording method fails, try another real method only when it remains practical and truthful.
- If video capture cannot be made valid, say so explicitly and compensate with screenshots only where the phase allows it.

## Reconciliation rule

- First-pass labels are provisional.
- Reconcile committed media against what the files actually show.
- If an artifact is good but mislabeled, fix metadata.
- If an artifact is misleading for its claimed purpose, replace it or mark it non-conforming.

## Sanitization rules

- Scan donor folders for `authToken=`, `token=`, `session=`, and `cookie` before commit.
- Redact sensitive values before they are committed.
- Never commit raw cookies, session identifiers, auth tokens, or donor asset dumps.
- In public repos, keep any donor binaries local-only and ignored through `.git/info/exclude`, not `.gitignore`.

## Architecture and contract safety

- Donor investigation docs must not change GS contracts.
- Donor packs describe observed mechanics and implementation guidance only; they do not alter runtime ownership rules.
- Browser remains presentation-only for wallet and session truth in canonical build handoff guidance.
