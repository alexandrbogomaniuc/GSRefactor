# 🔒 Anti-Vibecoding Rules

These rules are mandatory to prevent context degradation and ensure verifiable software delivery.

## 1. Architectural Invariants
*   **Rule 1: Strict Boundaries:** Components must only adhere to defined boundaries and interfaces. Do not bypass established modules for "local optimization".
*   **Rule 2: Predictability Over Cleverness:** Solutions must be easily readable, tested, and conform to the project's established style. 

## 2. The Implementation Definition of Done (DoD)
A feature or code stage is ONLY complete when:
1.  **Code Output:** Only modified or directly relevant files are yielded.
2.  **No Stubs:** No `// TODO` or placeholder implementations exist unless explicitly requested.
3.  **Mandatory Verification:** An `AI_NOTES.md` file MUST be generated/updated containing:
    *   **What was done:** Summary of changes.
    *   **Why:** Architectural or logical reasoning.
    *   **Risks/Limitations:** Edge cases not handled.
    *   **How to Verify:** Exact terminal commands to test the code (e.g., `npm run test`, `docker-compose up`).
4.  **Tests pass:** Code must include appropriate tests (unit or integration) covering primary logic paths.

## 3. Mandatory Review Skills
Code generation must be subjected to peer-review via dedicated skills *before* finalization:
*   Use `strict-code-review.md` on new pull requests or large diffs.
*   Use `docs-sync.md` to ensure `AI_NOTES.md` and actual code match.
*   Use `architecture-reviewer.md` before starting heavy, new feature work.
