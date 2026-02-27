export const GameConfig = {
  // Grid settings
  numReels: 5,
  numRows: 3,
  symbolWidth: 160,
  symbolHeight: 160,
  reelSpacing: 10,
  rowSpacing: 10,

  // Extra buffer for infinite spinning
  extraSymbols: 2,

  // Timings
  spinStagger: 0.2, // Seconds between each reel stopping
  minSpinDuration: 1.0, // Minimum spin seconds before stop can be resolved
  stopEasingDuration: 0.5, // Time for the bounce-back stop effect

  // Symbol atlas configuration
  // Each game replaces these textures in their own asset atlas.
  // Textures are resolved as `${symbolAtlasPrefix}${id}` → e.g. "symbol_0", "symbol_1"
  symbolCount: 7, // Total number of unique symbol types
  symbolAtlasPrefix: "symbol_", // Prefix for texture lookups in the asset atlas
};
