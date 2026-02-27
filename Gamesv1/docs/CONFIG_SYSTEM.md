# Igaming Configuration System

A tiered initialization system designed to balance local game design requirements with dynamic server-side regulatory and bank constraints.

## 🏗 Precedence Layers

The configuration is resolved in the following order (bottom is lowest precedence):

| Layer | Source | Description |
| :--- | :--- | :--- |
| **Dev Overrides** | URL Params / `dev-bank.json` | Used by QA/Devs to simulate environments. |
| **Customer Config** | Server Payload | Feature flags (Autoplay, Turboplay, Min Spin Time). |
| **Bank Config** | Server Payload | Commercial limits (Currency, Bet Levels, Max Win). |
| **Game Settings** | `game.settings.json` | Static game identity (ID, Reels, Features). |

---

## 📐 Data Schemas

### 1. Game Settings (`GameSettingsSchema`)
Stored in the game repository. Defines the fixed structure of the game.
- `reels`: Rows and columns.
- `features`: Enabled/disabled core mechanics (e.g., Free Spins).

### 2. Bank Config (`RuntimeBankConfigSchema`)
Determined by the currency and operator bank.
- `truncateCents`: If `true`, all bets and wins must be integers.
- `fractionDigits`: UI formatting (e.g., 2 for $1.00).
- `minBet` / `maxBet`: Dynamic limits applied to the Bet Selector.

### 3. Customer Config (`CustomerConfigSchema`)
Determined by the operator/jurisdiction requirements.
- `minSpinTime`: Mandatory delay before reels can stop (e.g., 2.5s for UK compliance).
- `autoplayEnabled`: Hard kill switch for autoplay functionality.

---

## ⚖️ Validation Rules

During `resolveRuntimeConfig()`, the system enforces specific consistency checks:
1. **Truncation Sync**: If `truncateCents` is true, an error is thrown if `minBet`, `maxBet`, or `defaultBet` have decimal values.
2. **Bet Boundaries**: `minBet` <= `defaultBet` <= `maxBet`.
3. **Contradiction Management**: Ensures that if a game feature requires the "Buy Feature" but it's disabled globally for the customer, the UI reacts appropriately.

---

## 🛠 Usage in Game

```typescript
import { RuntimeStore } from "@app/utils/RuntimeStore";

// Accessing the resolved config
const { minBet, currency } = RuntimeStore.bank;
const { rows, cols } = RuntimeStore.game.reels;
```

## 🧪 Simulation
You can simulate a "No Cents" bank by appending:
`?devBank=USD_TRUNC` to the game URL.
