# VIDEO VALIDATION

## Pre-flight test (mandatory)
1. Recorded `assets/video/preflight_test.mp4` from the live donor window.
2. Extracted thumbnails:
- `assets/video-thumbs/preflight_test_first.jpg`
- `assets/video-thumbs/preflight_test_mid.jpg`
- `assets/video-thumbs/preflight_test_last.jpg`
3. Validation result:
- Not black: thumbnail brightness checks were above black-screen threshold.
- Not frozen: first/mid/last frame differences were non-zero.
- Donor UI visible in extracted frames.

## Capture methods attempted
- Method 1: Chrome DevTools screencast (`screencast_start/stop`) -> SUCCESS.
- Method 2: Not required.
- Method 3: Not required.

## Validation status by clip
- All clips listed in `VIDEO_INDEX.csv` are currently marked `VALID`.
- No clip is marked `INVALID_BLACK`, `INVALID_FROZEN`, or `INVALID_UNUSABLE` in the final index.

## Sanitization
- Scanned this Phase 1C folder for `authToken=`, `token=`, `session=`, and `cookie`.
- Redacted sensitive query values in saved snapshot text.
- Verification scan found no non-redacted token/session/cookie values.
