# Cleanup Prompt

Use this prompt when a donor run already exists and the task is cleanup, reconciliation, or packaging rather than first-pass capture.

## Prompt

Use Git as the audit workflow. Do NOT push to main directly.

This is a donor follow-up cleanup run.

Rules:
- Work from the specified base branch and base commit.
- Do not broaden scope beyond the requested cleanup target.
- Keep changes limited to evidence metadata, documentation, indexing, sanitization, and truthful artifact replacement.
- Do not change GS contracts or runtime architecture.
- Never commit donor binaries to this public repo.

Required:
- Re-read the current donor evidence and canonical docs.
- Patch any mismatched metadata, labels, or evidence indexes.
- Re-run sanitization scans on the affected folder.
- Commit only cleanup changes and push the branch.

Output:
- repo path
- branch
- latest pushed commit hash
- latest pushed commit title
- compare URL
- changed file count
- diffstat
- sanitization result
- clean working tree YES/NO
