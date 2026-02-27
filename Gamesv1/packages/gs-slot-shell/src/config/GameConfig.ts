export const GameConfig = {
  // Grid settings
  numReels: 5,
  numRows: 3,
  symbolWidth: 160,
  symbolHeight: 160,
  reelSpacing: 10,
  rowSpacing: 10,

  // Extrabuffer for infinite spinning
  extraSymbols: 2,

  // Timings
  spinStagger: 0.2, // Seconds between each reel stopping
  minSpinDuration: 1.0, // Minimum spin seconds before stop can be resolved
  stopEasingDuration: 0.5, // Time for the bounce-back stop effect

  // Styling (temporary colors for ID)
  symbolColors: [
    0xff3333, // 0 - Red
    0x33ff33, // 1 - Green
    0x3333ff, // 2 - Blue
    0xffff33, // 3 - Yellow
    0xff33ff, // 4 - Magenta
    0x33ffff, // 5 - Cyan
    0xff9933, // 6 - Orange
  ],
};
