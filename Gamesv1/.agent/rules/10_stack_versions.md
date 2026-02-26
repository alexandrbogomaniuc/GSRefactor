# Tech Stack & Version Discipline

Default stack:
- TypeScript
- PixiJS v8.x (do not use older Pixi APIs)
- Vite build
- AssetPack for asset pipeline
- Pixi Layout + Pixi UI for responsive UI inside canvas
- GSAP for choreography/timelines
- Spine runtime (Pixi v8 compatible) for premium animation
- Effekseer for authored particle FX (optional, high-WOW)

Rules:
- Do not mix Pixi major versions or deprecated APIs.
- WebGL is the production default; WebGPU only behind feature detection.
- If you are unsure about a Pixi API, require adding Pixi “LLM docs” files into docs/refs/pixijs and consult them.
