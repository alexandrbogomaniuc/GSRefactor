# Mobile Performance & Debug Rules

Budgets (must be measured, not guessed):
- Stable 60 FPS on mid mobile targets.
- Fast startup: load minimal boot bundle first, defer the rest.

Rules:
- Add a simple debug overlay: FPS + draw call estimate + loaded texture count.
- Use profiling tools when optimizing (WebGL frame capture, Pixi inspection).
- Avoid reallocating render textures every frame.
- Avoid per-frame heavy CPU loops for layout; use Layout engine and cache where safe.

Security:
- Never commit secrets (API keys, tokens).
- Do not store tokens inside the repo.
