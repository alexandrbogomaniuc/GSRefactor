# ASSET_MANIFEST_SPEC

This spec defines the per-game art manifest file used to track production readiness.

## Location

Per game file location:
- `games/<gameId>/docs/asset-manifest.sample.json`

## Required Top-Level Fields

- `manifestVersion` (string): spec version, start with `1.0.0`.
- `gameId` (string): folder name under `games/`.
- `gameName` (string): display name.
- `themeId` (string): theme classification.
- `updatedAt` (ISO datetime string).
- `owner` (string): art lead or producer.
- `deliverables` (object): required categories.
- `qualityBudgets` (object): atlas, texture, and GPU budgets.
- `bundleMap` (object): runtime bundle mapping by category.

## Deliverables Object (Required Categories)

- `icons`: array
- `banners`: array
- `promoVideos`: array
- `preloaders`: array
- `bigWinVideos`: array
- `symbolAnimations`: array
- `uiKit`: array

Each item in a deliverables array must include:
- `id` (string)
- `status` (`todo` | `in_progress` | `review` | `approved` | `integrated`)
- `sourcePath` (string)
- `runtimePath` (string)
- `format` (string)
- `width` (number, pixels)
- `height` (number, pixels)
- `fps` (number, use `0` for static assets)
- `codec` (string, use `none` for static assets)
- `compression` (string)
- `bundle` (`preload` | `main` | `promo`)
- `notes` (string)

## Spine-Specific Item Fields

For `symbolAnimations` items, include extra fields:
- `symbolId` (string)
- `spineSkeleton` (string)
- `spineAtlas` (string)
- `staticFirstFrame` (string)
- `hasSpritesheetFallback` (boolean)

## qualityBudgets Object

Required fields:
- `maxAtlasSizeMobile` (number)
- `maxAtlasSizeDesktop` (number)
- `maxStartupTextureMBMobile` (number)
- `maxStartupTextureMBDesktop` (number)
- `maxInitialGpuUploadMB` (number)
- `maxEventGpuUploadMB` (number)

## bundleMap Object

Required keys:
- `preload`
- `main`
- `promo`

Each key value is an array of deliverable category names mapped to that bundle.

## Validation Rules

1. Every required category must contain at least one item.
2. Every item must have `status` and `bundle` populated.
3. `runtimePath` must match intended AssetPack output naming.
4. `symbolAnimations` must include both spine and static-first-frame fields.
5. Any `approved` item missing `runtimePath` fails validation.
