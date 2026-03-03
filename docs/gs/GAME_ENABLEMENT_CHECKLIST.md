# GS Game Enablement Checklist

- Scope: Operational checklist for enabling a productized slot release in GS
- Non-goals: No architecture or contract modifications
- Related:
  - `docs/gs/release-registration-contract.md`
  - `docs/gs/enable-disable-canary-rollback.md`
  - `docs/gs/PRODUCTIZED_GAME_ROLLOUT_RUNBOOK.md`

## 1) Release Registration

- [ ] `gameId`/`releaseId` registered in GS config store.
- [ ] Registration integrity/signature validated.
- [ ] `clientPackageVersion`/asset hash values verified.
- [ ] `mathPackageVersion` verified against approved package set.
- [ ] RTP model set/default rules verified.
- [ ] Capability profile reference/hash verified.
- [ ] Rollout state initialized to `DISABLED`.

## 2) Canary Preflight

- [ ] Canary scope defined (allowlist/traffic).
- [ ] Rollback target release verified and available.
- [ ] Monitoring and alert channels confirmed.
- [ ] Change window and on-call coverage confirmed.

## 3) Canary Functional Validation

- [ ] Launch/bootstrap success.
- [ ] `openGame` success.
- [ ] `playRound` success under normal and duplicate retry.
- [ ] `featureAction` success for enabled feature paths.
- [ ] `getHistory` returns correct latest rounds.
- [ ] `closeGame` success.

## 4) Session Restore Validation

- [ ] Mid-round disconnect scenario executed.
- [ ] `resumeGame` successful using persisted snapshot.
- [ ] Restored round continuation deterministic and consistent.
- [ ] `requestCounter` continuity verified.
- [ ] `stateVersion` continuity verified.

## 5) History Validation

- [ ] History entries reflect latest settled rounds.
- [ ] Round ordering/pagination validated.
- [ ] Bet/win/net values match expected settlement.
- [ ] Feature rounds represented correctly.
- [ ] No server-only audit fields exposed to browser history payloads.

## 6) Feature-Flag / Capability Verification

- [ ] Effective capability hash equals registered profile hash.
- [ ] Animation policy resolved correctly.
- [ ] Sound policy resolved correctly.
- [ ] Localization policy and content path resolved correctly.
- [ ] History policy resolved correctly.
- [ ] Wallet display policy resolved correctly.
- [ ] Feature policy restrictions verified (including buy-feature restrictions where applicable).
- [ ] Session UI policy resolved correctly.

## 7) Artifact Version Pin Verification

- [ ] `clientPackageVersion` pinned at session open.
- [ ] `mathPackageVersion` pinned at session open.
- [ ] Selected RTP model pinned for session.
- [ ] Capability/config hash pinned for session.
- [ ] Pin values remain unchanged through session lifecycle.

## 8) Promotion Decision (Canary -> Enabled)

- [ ] Canary KPIs within approved thresholds.
- [ ] No critical incidents open.
- [ ] Rollback path revalidated and ready.
- [ ] Ops + Product + Engineering sign-off recorded.

## 9) Post-Enable Verification

- [ ] New sessions route to enabled release.
- [ ] Existing sessions remain stable on original pins.
- [ ] Error distribution remains within baseline corridor.
- [ ] Financial settlement metrics stable.

## 10) Record of Approval

- Game ID:
- Release ID:
- Canary Window:
- Promotion Time (UTC):
- Approved By (Ops):
- Approved By (Product):
- Approved By (Engineering):
- Rollback Target Release ID:
