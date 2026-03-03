# VIDEO_OVERLAY_GUIDELINES

## Purpose

Standardize how video overlays are integrated for premium win/feature presentation while preserving performance and fallback behavior.

## Overlay Types

- big win video overlay
- huge win video overlay
- mega win video overlay
- optional feature intro overlays (scoped per game)

## Source Paths

- `games/<gameId>/raw-assets/main/win-videos/`
- tracked in `games/<gameId>/docs/asset-manifest.sample.json`

## Runtime Rules

1. Overlay activation must be driven by mapped browser-visible presentation signals + resolved runtime config.
2. Overlay playback must not mutate financial/session truth.
3. If low-performance mode is enabled, video overlays must downgrade to lightweight FX/counter fallback.

## Technical Specs

- codec: H.264 MP4 primary
- fps: 30 (60 only with approved budget)
- budget guidance:
  - big/huge/mega: <= 8 MB each (from pipeline baseline)

## Fallback Policy

When any condition fails (unsupported codec, budget exceed, low-performance mode):

- disable heavy video overlays
- keep text counter + lightweight highlight cues active

## QA Checklist

- overlay trigger matches tier
- skip/forced-skip timing remains valid
- no frame hitch spikes above guardrail during start/stop
- fallback path validated on low-perf profile
