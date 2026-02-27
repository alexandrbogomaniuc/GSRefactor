# VFX Library

## Reusable Effects (Top 20)
1.  **Light Sweep** (Win highlight) - Sprite mask/blend
2.  **Gold Coin Shower** (Big Win) - Particles
3.  **Lens Flare** (Reel stop flash) - Additive Sprite
4.  **Symbol Glow Pulse** (Anticipation) - Emissive Sprite
5.  **Reel Motion Blur** (Spinning) - Filter (use sparingly) or pre-baked edge blurred asset
6.  **Symbol Smash** (Feature trigger) - Sprite sequence / Spine
7.  **Laser Beam** (Connecting lines) - Spine / Sprite stretching
8.  **Electrical Spark** (Scatter hit) - Particle / Effekseer
9.  **Camera Shake** (Impact) - Engine Tween
10. **Screen Flash** (Bonus trigger) - Additive full-screen rect
11. **Gems Exploding** (Cascades) - Sprite sequence + Particles
12. **Floating Dust** (Atmosphere) - Particles
13. **Wild Expansion** (Wild feature) - Spine animation
14. **Multiplier Pop** (Score increase) - Tweened Text + Glow
15. **Anticipation Slow Down** (Reel 5 scatter) - Time-scale manipulation + sound pitch
16. **Magic Trail** (Symbol morphing) - Ribbon/Trail mesh
17. **Flame Burst** (Hot wins) - Sprite sequence
18. **Border Highlight** (Reel landing) - Animated 9-slice / Sprite
19. **Confetti Pop** (Small win) - Particles
20. **Shadow Drop** (Depth focus) - Black 9-slice overlay fade

## Standard Win Choreography
- **Small Win**: Quick light sweep over winning symbols. Coin sound tick, small scale pulse. (Duration: 0.5s - 1.0s)
- **Medium Win**: Brighter glow, symbols pop off background, short coin particle burst, medium fanfare sound. (Duration: 1.5s - 2.5s)
- **Big Win**: Full sequence. Screen darkens, large text elements fly in, intense camera shake, heavy sustained coin/gem particle shower, epic fanfare. Expects skipped or full timeline payout. (Duration: 5s - 10s)
