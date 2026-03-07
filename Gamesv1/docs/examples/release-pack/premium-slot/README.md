# Premium Slot Release-Pack Examples

This folder keeps intentionally committed examples of generated release-pack metadata and review-safe shell token seeds.

Canonical behavior:

- Real release-pack outputs live under `games/<gameId>/release-packs/<releaseId>/`.
- Those generated outputs are ignored by git.

Included example files:

- `release-packs.index.example.json`
- `RELEASE_PACK_SUMMARY.example.json`
- `brand-betonline-token-seed.example.json`

Brand seed constraints:

- Placeholder values only until brand assets are approved.
- Do not commit logos or scraped images here.
- Commit hex colors only when approved or explicitly marked as placeholders for review.
