# Localization System (i18n)

Multi-language support for all games in the Igaming platform.

---

## 🏗 Architecture

| Component | Location | Purpose |
|---|---|---|
| `@gamesv1/i18n` | `/packages/i18n` | Lightweight i18n loader with namespace + fallback support |
| Locale files | `games/<game>/locales/<lang>/` | Per-game, per-language translation JSON files |
| `i18n-check` | `/tools/i18n-check` | CI tool to validate translation completeness |

---

## 📂 Folder Structure

```
games/
  premium-slot/
    locales/
      en/                    ← Reference language (always complete)
        common.json          ← UI strings (Spin, Stop, Settings, etc.)
        paytable.json        ← Paytable strings (Symbol names, pay rules)
        rules.json           ← Game rules (RTP, Max Win, legal text)
      es/
        common.json
        paytable.json
        rules.json
      de/
        common.json
        paytable.json
        rules.json
```

---

## 📐 Namespaces

| Namespace | Content |
|---|---|
| `common` | Core UI: buttons, labels, error messages |
| `paytable` | Symbol descriptions, pay rules, payline labels |
| `rules` | RTP, volatility, legal disclaimers, bet limits |

---

## 🔑 Translation Keys

### Conventions
- **UPPER_SNAKE_CASE** for all keys
- **Interpolation** uses `{{variable}}` syntax: `"Welcome to {{BRAND_NAME}}"`
- **Built-in variables**: `{{BRAND_NAME}}` is auto-injected from config

### Example
```json
{
    "SPIN": "SPIN",
    "BRAND_WELCOME": "Welcome to {{BRAND_NAME}}",
    "MAX_WIN_VALUE": "{{multiplier}}× your bet"
}
```

### Usage in code
```typescript
import { i18n } from "@gamesv1/i18n";

// Simple key (defaults to 'common' namespace)
i18n.t("SPIN");              // → "SPIN"

// Namespaced key
i18n.t("rules:RTP_VALUE", { rtp: "96.54" });  // → "96.54%"

// Brand override (auto-injected)
i18n.t("common:BRAND_WELCOME");  // → "Welcome to Casino"
```

---

## 🌍 Language Resolution

### Development Mode
Language is set via URL parameter:
```
http://localhost:8080/?lang=es
http://localhost:8080/?lang=de&brand=MyCasino
```

### Production Mode
Language is provided by the server via launch parameters (the `LANGUAGE` field in the ENTER response). The code in `main.ts` should read this from the resolved config.

### Fallback Chain
1. **Current language** (e.g., `es`)
2. **Fallback language** (`en`)
3. **Raw key** returned as-is (never crashes)

---

## 🏷 Brand Overrides

Operators can customize text via brand variables. These are injected as interpolation defaults:

```typescript
await i18n.init({
    language: "en",
    localesPath: "./locales",
    brandOverrides: {
        BRAND_NAME: "SuperCasino",
        SUPPORT_EMAIL: "help@supercasino.com",
    },
});
```

Any `{{BRAND_NAME}}` or `{{SUPPORT_EMAIL}}` in translation values will be replaced automatically. If a variable is missing, the `{{placeholder}}` is left intact (never crashes).

---

## 🧪 Validation (CI)

### Running the check
```bash
# Check all games
npm run i18n:check

# Check a specific game
npx tsx tools/i18n-check/src/index.ts --game premium-slot
```

### What it validates
- Every key in `en/*.json` must exist in all other language files
- Extra keys (in non-en languages but not in en) are flagged as warnings
- Missing namespace files are reported
- Exit code 1 on any missing keys → CI gate

### Example output
```
🌐 i18n Check: validating translations...

  📦 premium-slot: 3 languages, 3 namespaces

✅ All translations are complete. No missing keys found.
```

---

## ✅ Best Practices

- **English is the reference**: Always add keys to `en/` first.
- **Keep namespaces focused**: Don't put game rules in `common.json`.
- **Never hardcode text**: All user-visible strings must go through `i18n.t()`.
- **Use interpolation** for dynamic values — don't concatenate strings.
- **Run `i18n:check` before merge** to catch missing translations early.


