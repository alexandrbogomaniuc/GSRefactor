# PixiJS v8 Known Issues & Architectual Decisions

This file contains important, verified constraints and findings about PixiJS v8 sourced directly from the core maintainers/GitHub issues.

## 1. Texture Compression Support Drops
* **Context**: `v8` aggressively trimmed legacy code paths.
* **Issue**: DDS DX10 (BC7/BC6H) and legacy `.ktx` parsing have been officially dropped from the codebase.
* **Resolution**: If we need compressed textures, we **MUST natively target KTX2 or Basis (.basis) formats.** Standard fallback remains WebP/PNG.

## 2. Basis Alpha-Mode Bug 
* **Context**: When rendering transparent sprites from Basis files, Pixi doesn't immediately default to standard alpha blending assumptions.
* **Issue**: `alphaMode` can default to `0` internally on Basis loading in `v8`, leading to black/opaque boxes where transparency should exist on mobile devices.
* **Resolution**: When configuring the `AssetPack` or loading `.basis` textures, intercept and ensure `alphaMode: 1` is explicitly defined so that standard transparency blending functions evaluate correctly.

## 3. Tree-Shaking Overload
* **Context**: The `pixi.js` umbrella package includes massive render systems for TilingSprites, TextHTML, Meshes across both WebGPU and WebGL backends.
* **Issue**: Because of how the renderer orchestrates plugins, importing `pixi.js` indiscriminately includes rendering code for systems we don't use in 2D slot games (like basic 3D / Tiling).
* **Resolution**: Constantly enforce tree-shaking rules in the Vite bundler configuration, and ideally migrate towards explicit sub-package imports (`import { Container } from '@pixi/display'`) in later optimization stages instead of importing from `'pixi.js'` directly.
