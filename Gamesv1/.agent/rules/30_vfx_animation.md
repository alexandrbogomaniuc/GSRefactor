# VFX & Animation Rules (WOW without killing FPS)

Premium look comes from STACKING:
- authored animation (Spine / sprite sequences / Effekseer)
- light sweeps, glow accents, impact flashes
- particles with controlled timing
- camera punch/shake/zoom used sparingly
- strong easing (no linear movement)

Performance rules:
- Filters are expensive. Use them only on small areas or hero moments.
- Prefer baked glow/emissive layers over full-screen filters.
- Do not apply multiple filters to large containers in hot paths.
- Use ParticleContainer (or equivalent cheap particles) for high counts.

Gameplay rules:
- Every win state must have a choreography timeline (small win / medium / big win).
- Always synchronize key animation beats with sound triggers.
