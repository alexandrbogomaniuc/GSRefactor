# AAA Quality Gate - Release Checklist

No build should be promoted to production without full pass.

## 1. Runtime Correctness

- [ ] Idempotency: retries preserve identical request key.
- [ ] Sequencing: requestCounter/order behavior matches GS contract.
- [ ] Restore: reload/reconnect resumes from GS-provided restore payload.
- [ ] Spin profiling payloads are emitted where required.

## 2. Compliance & UX

- [ ] Turbo only affects presentation timing.
- [ ] Min spin timing policy is enforced.
- [ ] Autoplay/turbo flags follow resolved runtime config.

## 3. Performance & Stability

- [ ] No scene graph growth leaks in soak test.
- [ ] Texture/GPU memory remains stable.
- [ ] Stable 60 FPS on target mobile device class.
- [ ] Low-perf mode degrades heavy effects correctly.

## 4. Security

- [ ] No secrets/tokens in logs.
- [ ] Sensitive payload fields are redacted in diagnostics.
