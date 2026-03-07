# 09 Asset Generation Master Brief

## Scope

- This brief converts the committed ChickenGame donor pack into an original asset production handoff for a separate asset-generation agent. Evidence: `Gamesv1/GameseDonors/ChickenGame/README.md`, `Gamesv1/GameseDonors/ChickenGame/01_EXECUTIVE_SUMMARY.md`
- Use only committed donor evidence and canonical ChickenGame docs as truth; do not add donor binaries, donor payload dumps, or new donor capture in this asset phase. Evidence: `Gamesv1/GameseDonors/ChickenGame/LOCAL_ONLY_DONOR_ASSETS.md`, `Gamesv1/GameseDonors/DonorsWrokflowRules/05_LOCAL_ONLY_ASSETS_POLICY.md`
- For any video-derived claim, treat Phase 1D reconciliation as the source of truth over raw Phase 1C labels. Evidence: `Gamesv1/GameseDonors/ChickenGame/01_EXECUTIVE_SUMMARY.md`, `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`

## Verified mechanic and screen baseline

- Board planning baseline: `3` reels by `4` rows, with fixed `8` active lines. Evidence: `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`
- Verified mechanic stack to preserve at the system level only: `Collect`, `Chicken Boost`, `Bonus Game`, jackpots, `Buy Bonus`, and autoplay-supported spin flow. Evidence: `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`
- Verified control and surface set includes intro gate, idle HUD, spin/result loop, menu overlay, rules modal, how-to modal, buy-bonus modal, autoplay running state, stop-state control, and settings panel. Evidence: `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`
- Verified Buy Bonus concept is a three-tier modal flow; carry that concept forward without reusing donor styling. Evidence: `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`
- UNOBSERVED and therefore not a donor truth source: exact RTP, hidden probability weights, full payout table for every symbol count, and hidden implementation logic. Evidence: `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`

## Originality rules

- Preserve mechanic-level inspiration only; do not copy donor art style, character art, board geometry styling, colors, typography, lighting style, icon silhouettes, or animation curves. Evidence: `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`
- Keep the new game world brand-distinct and avoid chicken or farm silhouette language in production art direction. Evidence: `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`
- Never trace, repaint, recolor, or paint over donor screenshots or donor video frames into deliverables. Evidence: `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`, `Gamesv1/GameseDonors/ChickenGame/LOCAL_ONLY_DONOR_ASSETS.md`

## Delivery specifications

- Landscape master delivery is required at `1920x1080`; mobile portrait adaptation is required at `1080x1920`. Evidence: `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md`, `Gamesv1/GameseDonors/ChickenGame/11_GSREFACTOR_BUILD_HANDOFF.md`
- Every surface pack must include safe-crop notes or exports for `16:9`, `19.5:9`, and `9:16`. Evidence: `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md`, `Gamesv1/GameseDonors/ChickenGame/11_GSREFACTOR_BUILD_HANDOFF.md`
- Preserve the existing atlas grouping approach so the build handoff remains stable across gameplay HUD, symbols, popups, preload surfaces, and VFX. Evidence: `Gamesv1/GameseDonors/ChickenGame/11_GSREFACTOR_BUILD_HANDOFF.md`, `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`
- Every interactive UI control must define `normal`, `hover`, `pressed`, `disabled`, and `selected` states. Evidence: `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`, `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`
- Include accessibility-ready contrast variants for controls, critical counters, and modal typography where the final visual language needs them. Evidence: `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`
- Provide numeric or readout atlas coverage for balance, bet, win, modal prices, spin counters, and jackpot values because those readouts are persistent parts of observed gameplay states. Evidence: `Gamesv1/GameseDonors/ChickenGame/01_EXECUTIVE_SUMMARY.md`, `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`, `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`

## Motion and timing baseline

- Motion packs must cover idle loops, spin start, reel stop, result emphasis, collect pulse, boost escalation, modal reveal, and feature-entry energy beats because those transitions are visibly present in the donor flow. Evidence: `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`
- Use the reconciled autoplay, buy-bonus, special-state, and fixed 20-spin clips as timing references only; do not match their animation curves or timing frame-for-frame. Evidence: `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md`
- Exact donor audio design is UNOBSERVED in the canonical pack, so audio deliverables must stay original while mapping to verified mechanic beats only. Evidence: `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`, `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`

## Asset requirement record schema

- Every asset request row, ticket, or spreadsheet entry must include the fields below so the asset-generation agent can trace each requirement back to donor truth or clearly marked inference. Evidence: `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md`, `Gamesv1/GameseDonors/ChickenGame/11_GSREFACTOR_BUILD_HANDOFF.md`

| Field | Requirement |
|---|---|
| `asset_id` | Stable unique ID for the request item. |
| `category` | Family such as preload, gameplay shell, symbol, HUD, overlay, VFX, audio, or promo. |
| `asset_name` | Human-readable deliverable name. |
| `purpose` | What the asset does in the product. |
| `screen_state` | State or surface where the asset appears. |
| `mechanic_or_control` | Related mechanic or control affordance. |
| `confidence` | Use only `VERIFIED`, `INFERENCE`, or `UNOBSERVED`. |
| `evidence_paths` | One or more committed repo paths that justify the requirement. |
| `originality_note` | Explicit rule for what must stay original. |
| `source_file_expected` | Working master format expected from the asset creator. |
| `export_file_expected` | Runtime export format expected by implementation. |
| `target_atlas_or_group` | Atlas or package grouping target. |
| `state_variants` | Required interactive or visual variants. |
| `animation_need` | Static, loop, burst, transition, or cinematic note. |
| `mobile_notes` | Portrait or small-screen considerations. |
| `accessibility_notes` | Contrast, readability, or icon-clarity note. |
| `status` | Suggested values: `TODO`, `IN_PROGRESS`, `READY_FOR_REVIEW`, `ACCEPTED`, `BLOCKED`. |

## Atlas groups to preserve

| Atlas or group | Required coverage | Evidence anchor | Notes |
|---|---|---|---|
| `atlas_preload_ui` | logo lockup, loading states, intro gate art, optional legal/footer marks | `Gamesv1/GameseDonors/ChickenGame/01_EXECUTIVE_SUMMARY.md`, `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md` | Legal/footer marks are `INFERENCE` unless product requirements introduce them. |
| `atlas_symbols_main` | standard and premium symbol families, shared frame treatments | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md` | Build an original symbol set; do not reuse donor silhouettes. |
| `atlas_symbols_feature` | collect, boost, bonus, coin, multiplier, jackpot badge families | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md` | Separate feature symbols from base symbols for readability and tuning. |
| `atlas_hud_controls` | spin, bet minus, bet plus, buy bonus, autoplay, stop, menu, settings, sound/music, history | `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md` | Keep autoplay, turbo, and stop labeling explicit and unambiguous. |
| `atlas_popups` | menu overlay, rules modal, how-to modal, buy-bonus modal, settings panel, history placeholder, tabs, close controls | `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md` | Rules and how-to must be visually distinct. |
| `atlas_vfx` | spin/reel effects, collect pulse, boost escalation, counter pops, trail sweeps, jackpot bursts, modal reveals, win overlays | `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md` | Exact jackpot burst behavior is `INFERENCE` unless later captured for our own product. |
| `audio_event_pack` | original cues for button, spin, stop, collect, boost, bonus, jackpot, big-win, ambience | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md` | Audio mapping is mechanic-driven because donor audio specifics are `UNOBSERVED`. |
| `promo_surface_pack` | original trailer key art, hook shot, spin-to-result, collect, boost, buy-bonus, climax, end card | `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md` | Promo assets must remain product-original and cannot embed donor captures. |

## Required asset families

### Preload and boot surfaces

| Asset family | Minimum deliverables | Confidence | Evidence paths | Originality note |
|---|---|---|---|---|
| Boot/logo package | logo lockup, loading logo mark, loader progress states | INFERENCE | `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md` | Build a new brand system; donor gate proves a pre-game entry surface exists but not the new brand. |
| Intro gate surface | splash art, CTA composition, supporting depth layers | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md` | Preserve the presence of an intro gate, not donor composition or theme. |
| Footer/legal marks | footer-safe variant if required by product/legal | UNOBSERVED | `Gamesv1/GameseDonors/ChickenGame/01_EXECUTIVE_SUMMARY.md` | Add only if our product/legal requirements need them. |

### Core gameplay shell

| Asset family | Minimum deliverables | Confidence | Evidence paths | Originality note |
|---|---|---|---|---|
| Reel board shell | board frame, reel masks, win-frame state, line-highlight state | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md` | Keep the `3x4` functional baseline only; do not copy donor board ornament or proportions. |
| Main background | primary background layers for idle/spin/result | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/01_EXECUTIVE_SUMMARY.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md` | Replace the donor world entirely with a new theme direction. |
| Feature background variants | alternate backdrop treatment for collect/boost/bonus emphasis | INFERENCE | `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md` | Use original lighting language; donor evidence proves feature emphasis, not exact backdrop swaps. |
| Payline visuals | fixed-line highlight kit for the `8` active lines baseline | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md` | Keep the line count baseline, not donor line art or color choices. |

### Symbol families

| Asset family | Minimum deliverables | Confidence | Evidence paths | Originality note |
|---|---|---|---|---|
| Standard symbols | low and standard symbol family with idle and settle states | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md` | Replace donor fruit/bar aesthetics with a new icon family. |
| Premium symbols | premium symbol family with highlight variants | INFERENCE | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md` | Premium tiering is a production need inferred from visible symbol mix, not a donor art target. |
| Feature symbols | collect symbol, boost symbol, bonus trigger symbol family | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md` | Keep mechanic readability, not donor symbol shapes. |
| Coin and multiplier symbols | coin, `xN` multiplier, super-state symbol variants | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md` | Build a new coin/medallion language; do not mirror donor badges. |
| Jackpot badge family | mini, minor, major, grand badge system and counter states | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md` | Preserve tier readability only; use original iconography and typography. |

### HUD and control assets

| Asset family | Minimum deliverables | Confidence | Evidence paths | Originality note |
|---|---|---|---|---|
| Primary action controls | spin, stop, autoplay, turbo/fast-play affordances and labels | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`, `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md` | Make labels explicit; do not copy donor button shapes or glyphs. |
| Bet controls | bet minus, bet plus, bet readout, bet state indicator | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`, `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md` | Readability matters more than donor placement. |
| Buy Bonus control | entry button plus current-bet-aware price readout support | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`, `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md` | Keep the three-tier concept, not donor button styling. |
| Navigation controls | menu, settings, history entry point, sound/music controls | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md` | History downstream behavior is `UNOBSERVED`; provide a clean placeholder route. |
| Numeric HUD kit | balance, bet, win, jackpot, and modal-price readouts | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/01_EXECUTIVE_SUMMARY.md`, `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md` | Use original readout typography and separator treatment. |

### Overlay and modal assets

| Asset family | Minimum deliverables | Confidence | Evidence paths | Originality note |
|---|---|---|---|---|
| Menu overlay | panel shell, entry list states, close control | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md` | Keep menu function only; rebuild layout and styling. |
| Rules modal | title state, section body treatment, close states | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md` | Make rules entry visually distinct from how-to. |
| How-to modal | mechanic explainer shell, section tabs/cards, icon callouts | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md` | Use new diagrams and copywriting style. |
| Buy Bonus modal | modal shell, three tier cards, active bet math, close control | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md` | Preserve tier math clarity, not donor modal composition. |
| Settings panel | settings shell, toggle states, close/back behavior | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md` | Use original settings iconography. |
| History surface | history entry point, empty state, placeholder panel if final product needs it | UNOBSERVED | `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md` | Do not invent donor layout; treat as product-owned UX. |

### VFX and motion assets

| Asset family | Minimum deliverables | Confidence | Evidence paths | Originality note |
|---|---|---|---|---|
| Spin motion pack | spin press burst, reel blur, reel stop, settle glow | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md` | Use donor clips for timing class only. |
| Collect emphasis pack | coin attach, collect pulse, counter pop, total update | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md` | Build an original collect language. |
| Boost escalation pack | multiplier highlight, lightning or energy escalation, board emphasis | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md` | Use a new motion grammar; no donor lightning clone. |
| Bonus and jackpot pack | bonus-entry transition, jackpot attach or burst, tiered win overlays | INFERENCE | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`, `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md` | Required for our product scope even though donor jackpot climax was not fully observed. |
| Modal transition pack | menu/rules/how-to/buy/settings reveal and dismiss transitions | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md` | Make transitions clean and performant on mobile. |

### Audio deliverables

| Asset family | Minimum deliverables | Confidence | Evidence paths | Originality note |
|---|---|---|---|---|
| UI cues | button hover, press, close, toggle, confirm cues | INFERENCE | `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md` | Donor control set is verified, donor sound design is not. |
| Spin and settle cues | spin start, reel stop, result settle | INFERENCE | `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`, `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md` | Time to our motion language, not donor audio. |
| Feature cues | collect hit, boost hit, bonus entry, jackpot event, big-win intensity ladder | INFERENCE | `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md` | Required by mechanic set; donor audio specifics remain `UNOBSERVED`. |
| Ambient layers | idle ambience, feature ambience, promo sting support | UNOBSERVED | `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md` | Treat ambience as product-owned tone design. |

### Promo deliverables

| Asset family | Minimum deliverables | Confidence | Evidence paths | Originality note |
|---|---|---|---|---|
| Hook and branding kit | trailer opener, title card, end card, logo motion | INFERENCE | `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md` | Entirely original to the new product. |
| Mechanic showcase kit | spin-to-result, collect moment, boost moment, buy-bonus moment, climax package | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md` | Showcase verified mechanic beats without donor footage. |
| Format adaptation kit | `16:9` and `9:16` layout-safe art and typography | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md` | Build layouts from original assets only. |

## Explicit UI and labeling requirements

- Make autoplay, turbo, and stop labels visually unambiguous because donor evidence showed control ambiguity during early probe/reconciliation. Evidence: `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`, `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`
- Make rules and how-to entry points easy to distinguish at rest and when active. Evidence: `Gamesv1/GameseDonors/ChickenGame/08_IMPROVEMENTS_AND_ORIGINALITY_GUARDRAILS.md`, `Gamesv1/GameseDonors/ChickenGame/02_SCREEN_STATE_MAP.md`
- Make Buy Bonus tier math explicit in the modal UI because three tiers and current-bet conversion are verified donor truths. Evidence: `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`
- Carry a settings surface and a history entry point, but keep history internals product-owned unless later evidence is added. Evidence: `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`

## Confidence handling

- Mark a requirement `VERIFIED` only when the surface, mechanic, or control is directly supported by the canonical donor docs or evidence. Evidence: `Gamesv1/GameseDonors/ChickenGame/01_EXECUTIVE_SUMMARY.md`, `Gamesv1/GameseDonors/ChickenGame/12_VISUAL_REFERENCE_INDEX.md`
- Mark a requirement `INFERENCE` when the donor evidence proves the mechanic family but not the exact production deliverable shape. Evidence: `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`, `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md`
- Mark a requirement `UNOBSERVED` when the donor pack does not prove the donor surface and the asset is included only because our product needs a complete UX or trailer package. Evidence: `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`, `Gamesv1/GameseDonors/ChickenGame/04_GAMEPLAY_AND_MECHANICS.md`

## Local-only safety rules

- Do not commit donor binaries, packaged donor builds, raw donor asset dumps, or redistributable payloads while fulfilling this brief. Evidence: `Gamesv1/GameseDonors/ChickenGame/LOCAL_ONLY_DONOR_ASSETS.md`, `Gamesv1/GameseDonors/DonorsWrokflowRules/05_LOCAL_ONLY_ASSETS_POLICY.md`
- If local-only donor material is needed for private reference, keep it under `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/` and ignore it via `.git/info/exclude`. Evidence: `Gamesv1/GameseDonors/ChickenGame/LOCAL_ONLY_DONOR_ASSETS.md`, `Gamesv1/GameseDonors/DonorsWrokflowRules/05_LOCAL_ONLY_ASSETS_POLICY.md`
