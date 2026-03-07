# Phase 1: Contact Sheet Review & Art Direction Matrix

## Overview
This document serves as the contact sheet review for the Nanobanana Crazy Rooster game (Game 7000). Our objective is to perfectly match the warm barn, brass, and rooster styling of the original OpenAI provider while maintaining premium 3D vertical slice quality. No neon, no cyberpunk.

## Contact Sheet Specs
**Target Resolution**: 1920x3000 (Backgrounds), 2048x1536 (Symbols), 1920x2048 (UI)
**Model**: gemini-3.1-flash-image
**Export/Compression Plan**: Lossless PNGs for the review sheets. Final assets will be processed according to asset pipeline guidelines.
**Negative Prompt**: `neon, cyberpunk, futuristic, lab, sci-fi, glass UI, blue glowing lights, holographic, alien, robot, text, words, watermark, blurry, low resolution, flat colors, deformed anatomy`

---

## 1. Background Directions (`contact-sheet-backgrounds.png`)

### Concept A: Golden Barn Interior Hero Scene
*   **Role**: Main ambiance
*   **Contract Key**: `background-desktop-1920x1080`
*   **Prompt**: `A warm, sunlit interior of a classic red wooden slot background. Dust motes dancing in the air, golden straw on the floor, golden hour lighting pouring through gaps in the wood. Highly detailed, premium 3D rendering, cozy rustic atmosphere, no futuristic elements.`
*   **Why it fits**: Immediately grounds the game in a relatable, warm rustic environment matching the core theme.
*   **Changes vs Cyberpunk**: Removed laboratory walls, cold metal, and neon. Replaced with organic wood, straw, and sunlight.

### Concept B: Premium Rooster Coop / Stage / Showpiece
*   **Role**: Main ambiance
*   **Contract Key**: `background-desktop-1920x1080`
*   **Prompt**: `A whimsical premium wooden chicken coop interior slot background, built with polished mahogany and shiny brass fittings. Warm ambient lantern lighting, soft glowing hay nests. High-end casino presentation, vibrant colors, detailed 3D artwork.`
*   **Why it fits**: Blends the rustic theme with a "premium casino" feel using polished woods and brass accents instead of raw utility.
*   **Changes vs Cyberpunk**: Shifted the "premium" material from chrome/neon to mahogany/brass.

### Concept C: Harvest Machine / Brass Contraption
*   **Role**: Main ambiance
*   **Contract Key**: `background-desktop-1920x1080`
*   **Prompt**: `A stylized farmyard feature scene at sunrise, focusing on a complex but warm brass mechanical harvesting contraption. Wooden wheels, golden wheat, morning dew, beautiful sunny sky. High-end 3D slot background art.`
*   **Why it fits**: Adds a mechanical element (matching the "Crazy" or "Hold&Win" layered feature feel) but keeps it strictly aligned with a rustic setting (brass, not neon).
*   **Changes vs Cyberpunk**: The technology is mechanical/steampunk-farm rather than digital sci-fi.

---

## 2. Key Symbol Directions (`contact-sheet-symbols.png`)
*Selected strictly based on the OpenAI contract keys and roles.*

### Concept 1: Brass Weather Vane
*   **Role**: Wild
*   **Contract Key**: `symbol-8-bolt`
*   **Prompt**: `A shiny polished brass weather vane shaped like a rooster slot symbol, catching the sunlight, rustic but premium 3D game icon, isolated on a dark background.`
*   **Why it fits**: Strong brass reflection reads clearly as a premium wild symbol.

### Concept 2: Barn Door
*   **Role**: Scatter/Collector
*   **Contract Key**: `collector-symbol`
*   **Prompt**: `A miniature, classic red wooden barn door with heavy polished brass hinges slot symbol, stylized, cute but highly detailed, 3D premium game icon, isolated on a dark background.`
*   **Why it fits**: Wooden material with a strong silhouette reads as an opening to a bonus.

### Concept 3: Brass Egg
*   **Role**: Bonus/Coin
*   **Contract Key**: `symbol-7-coin`
*   **Prompt**: `A glowing, perfectly polished brass egg sitting on a velvet cushion slot symbol, high-end reflection, premium 3D game art, isolated on a dark background.`
*   **Why it fits**: Translates the concept of a valuable hold & win coin into the narrative of the game.

### Concept 4: Golden Rooster
*   **Role**: Top Premium
*   **Contract Key**: `symbol-9-rooster`
*   **Prompt**: `A proud, slightly wild-looking rooster head in profile slot symbol. Vibrant red, orange, and gold feathers, premium 3D casino icon, highly detailed rendering, isolated on a dark background.`
*   **Why it fits**: The namesake premium character. Red/Gold feathers contrast the wood UI.

### Concept 5: Wood/Brass Ingot Stack
*   **Role**: Mid Value 3
*   **Contract Key**: `symbol-5-bar`
*   **Prompt**: `A stack of three polished mahogany and brass ingots slot symbol, glowing warm light reflecting off the metal, premium 3D game icon, isolated on a dark background.`
*   **Why it fits**: A literal interpretation of the casino "bar" matched to the farm materials.

### Concept 6: Painted Farm Egg
*   **Role**: Low Value 1
*   **Contract Key**: `symbol-0-egg`
*   **Prompt**: `A clean, perfectly spherical cream-colored chicken egg slot symbol with subtle rustic speckling, highly detailed 3D render, smooth lighting, isolated on a dark background.`
*   **Why it fits**: Clean, simple low-value geometry.

---

## 3. UI Directions (`contact-sheet-ui.png`)

### Concept A: Polished Mahogany & Amber
*   **Role**: Main CTA
*   **Contract Key**: `button-spin`
*   **Prompt**: `A shiny polished mahogany wood rounded rectangular template for a slot UI element, warm glowing amber inner edge, premium casino UI element, 3D render, blank center, on a dark background.`
*   **Why it fits**: Highest-end "luxury farm" feel.

### Concept B: Rustic Wood & Copper
*   **Role**: Main CTA
*   **Contract Key**: `button-spin`
*   **Prompt**: `A rustic wooden plank template clamped with shiny rounded copper brackets, warm glowing center, high-end 3D UI design, slot game component, blank center, on a dark background.`
*   **Why it fits**: Natural fit for the interior barn environment.

### Concept C: Painted Fairground & Brass
*   **Role**: Main CTA
*   **Contract Key**: `button-spin`
*   **Prompt**: `A thick, glossy painted red and cream carnival-style button template with a polished brass rim, vibrant warm colors, premium slot game UI element, blank center, on a dark background.`
*   **Why it fits**: Playful high-stakes energy.

---

## Recommendations

- **Strongest Background Direction**: Concept A (Golden Barn Interior Hero Scene). It provides a beautiful framing without distracting from the complex symbols.
- **Strongest Symbol Family**: The mix of brass specials, organic premiums, and wooden/painted lowers creates clear visual hierarchy.
- **Strongest UI Family**: Polished Mahogany & Amber feels the most expensive and cleanly separates from the detailed background.
