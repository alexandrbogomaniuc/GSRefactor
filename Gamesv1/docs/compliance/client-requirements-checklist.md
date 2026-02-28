# Client Requirements Checklist (Canonical)

Use this checklist together with `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`.

## 1. Financial/State Ownership

- [ ] Client treats GS as source of truth for wallet/session/restore state.
- [ ] Client does not own or persist authoritative financial state.
- [ ] Idempotency keys are stable across retries.
- [ ] requestCounter/ordering requirements from GS are respected.

## 2. Runtime Compliance

- [ ] `minSpinTime` policy is enforced.
- [ ] turbo/autoplay behavior respects resolved runtime config.
- [ ] currency formatting/truncation follows resolved config.

## 3. Transport Behavior

- [ ] Canonical runtime path uses GS HTTP init + transaction flow.
- [ ] reconnect/reload path resumes using GS restore payload.
- [ ] WebSocket-specific behavior is not required for production readiness.

## 4. Packaging and Assets

- [ ] Assets load through manifest bundles from CDN/static origin.
- [ ] No hardcoded runtime asset paths in gameplay logic.

## 5. Observability and Safety

- [ ] Spin profiling/telemetry hooks are wired where required.
- [ ] No secrets/tokens are exposed in production logs.
