# 09B Asset Acceptance Checklist

## Purpose

- Use this checklist to review asset deliveries against the canonical ChickenGame donor pack without reopening donor capture or inventing new donor truth. Evidence: `Gamesv1/GameseDonors/ChickenGame/README.md`, `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`

## Truth and evidence gates

- [ ] Every asset requirement row includes `asset_id`, `category`, `asset_name`, `purpose`, `screen_state`, `mechanic_or_control`, `confidence`, `evidence_paths`, `originality_note`, `source_file_expected`, `export_file_expected`, `target_atlas_or_group`, `state_variants`, `animation_need`, `mobile_notes`, `accessibility_notes`, and `status`. Evidence: `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] Every `VERIFIED` claim points to a committed ChickenGame doc or evidence path. Evidence: `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md`, `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] Any item not directly proven by evidence is labeled `INFERENCE` or `UNOBSERVED`; nothing is silently promoted to donor truth. Evidence: `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] Video-derived requirements use reconciled Phase 1D truth, not raw Phase 1C labels. Evidence: `Gamesv1/GameseDonors/ChickenGame/01_EXECUTIVE_SUMMARY.md`, `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`

## Originality and legal gates

- [ ] No asset traces, repaints, recolors, or paint-overs donor screenshots or donor video frames. Evidence: `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`
- [ ] Final theme, iconography, and world language are original and avoid chicken or farm silhouette language. Evidence: `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`
- [ ] Donor binaries, packaged donor builds, and raw donor asset dumps are not committed. Evidence: `Gamesv1/GameseDonors/ChickenGame/LOCAL_ONLY_DONOR_ASSETS.md`, `Gamesv1/GameseDonors/DonorsWrokflowRules/05_LOCAL_ONLY_ASSETS_POLICY.md`
- [ ] Any private donor-only material stays under `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/` and remains ignored through `.git/info/exclude`. Evidence: `Gamesv1/GameseDonors/ChickenGame/LOCAL_ONLY_DONOR_ASSETS.md`

## Screen-state coverage gates

- [ ] Boot/loading assets are covered. Evidence: `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`
- [ ] Intro gate assets are covered. Evidence: `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md`
- [ ] Idle HUD assets are covered. Evidence: `Gamesv1/GameseDonors/ChickenGame/01_EXECUTIVE_SUMMARY.md`, `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`
- [ ] Spin-in-progress assets are covered. Evidence: `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`
- [ ] Settled result assets are covered. Evidence: `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md`
- [ ] Collect or boost highlighted result assets are covered. Evidence: `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md`
- [ ] Menu overlay, rules modal, and how-to modal assets are all covered as separate surfaces. Evidence: `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`
- [ ] Buy Bonus modal assets cover all three tiers and visible bet math. Evidence: `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`
- [ ] Autoplay running state and stop-state assets are covered with reconciled truth. Evidence: `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`
- [ ] Settings panel assets are covered. Evidence: `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`
- [ ] My Bet History is either implemented as a product-owned placeholder or explicitly marked `UNOBSERVED`. Evidence: `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`
- [ ] Promo and trailer-specific surfaces are covered for both landscape and portrait output. Evidence: `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md`

## Mechanics and control gates

- [ ] Board planning follows the verified `3x4` baseline and `8` active lines. Evidence: `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`
- [ ] The asset set supports `Collect`, `Boost`, `Bonus Game`, jackpots, and a three-tier `Buy Bonus` concept. Evidence: `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`
- [ ] The asset set includes spin, bet minus, bet plus, menu, settings, autoplay, stop, and buy-bonus affordances. Evidence: `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`
- [ ] Autoplay, turbo, and stop labels are explicit and not visually ambiguous. Evidence: `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`, `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`
- [ ] Rules and how-to entry points are easy to distinguish. Evidence: `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`, `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`

## Asset completeness gates

- [ ] Preload and boot package exists. Evidence: `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] Gameplay shell package exists. Evidence: `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] Symbol package exists for standard, premium, feature, coin or multiplier, and jackpot badges. Evidence: `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] HUD and control package exists. Evidence: `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`, `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] Overlay and modal package exists. Evidence: `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] VFX package exists for spin, stop, collect, boost, modal reveal, and win escalation. Evidence: `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`, `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] Audio package exists and is marked original where donor audio remains `UNOBSERVED`. Evidence: `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`, `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] Promo package exists for hook shot, mechanic showcase, climax, and end card. Evidence: `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md`, `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`

## UI state, mobile, and accessibility gates

- [ ] Every interactive control ships with `normal`, `hover`, `pressed`, `disabled`, and `selected` states. Evidence: `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] Landscape master is authored at `1920x1080`. Evidence: `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`, `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md`
- [ ] Portrait adaptation is authored at `1080x1920`. Evidence: `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`, `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md`
- [ ] Safe-crop notes or exports exist for `16:9`, `19.5:9`, and `9:16`. Evidence: `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] Readouts remain legible on mobile for balance, bet, win, jackpots, and buy-bonus prices. Evidence: `Gamesv1/GameseDonors/ChickenGame/01_EXECUTIVE_SUMMARY.md`, `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`
- [ ] Accessibility-friendly contrast variants exist where final UI tone or background depth would otherwise reduce readability. Evidence: `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`, `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`

## Motion and performance gates

- [ ] Idle-loop expectations are defined for symbols, HUD surfaces, and backgrounds where needed. Evidence: `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`, `Gamesv1/GameseDonors/ChickenGame/09_ASSET_GENERATION_MASTER_BRIEF.md`
- [ ] Impact-burst expectations are defined for spin press, collect, boost, and modal reveal. Evidence: `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`
- [ ] Big-win or full-screen overlay expectations are clearly marked `INFERENCE` if donor evidence does not fully prove them. Evidence: `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`, `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md`
- [ ] Motion packages are designed for mobile-safe performance and layered fallback when needed. Evidence: `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`, `Gamesv1/GameseDonors/ChickenGame/11_GSREFACTOR_BUILD_HANDOFF.md`

## Final review gate

- [ ] The asset pack reads as an original product handoff that is mechanically compatible with the donor pack but visually and audiovisually distinct from it. Evidence: `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`, `Gamesv1/GameseDonors/ChickenGame/11_GSREFACTOR_BUILD_HANDOFF.md`
