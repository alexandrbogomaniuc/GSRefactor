# Premium Slot Client: Quality Bar (Mobile-first)

We are building a premium, mobile-first HTML5 slot game client.

Non-negotiable:
- Always mobile-first (Safari iOS + Chrome Android). Desktop is supported but never at the expense of mobile.
- “Provider-grade” feel: authored animation, layered VFX, clean typography, strong easing, strong sound sync.
- Server-authoritative outcomes (client is presentation + state recovery). Never suggest client-side RNG for real outcomes.

Agent behavior:
- For any non-trivial change: produce a short plan first, then implement.
- Keep architecture reusable across multiple games/themes.
- Prefer data-driven configs over hard-coded values.
- Provide acceptance criteria for each deliverable.
