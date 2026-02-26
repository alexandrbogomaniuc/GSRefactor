# Compliance Configuration Resolution

Slot games are bound by heavy regulatory requirements. The configurations supplied by the server must be strictly enforced.

The `ComplianceConfig.ts` system relies on a layered precedence object-merge architecture.

## 1. The Precedence Flow

`Base Features` -> `Certification Config` -> `Licensee/Operator Overrides` -> `Launch Arguments` -> `Dynamic Bank Overrides`

### Example Sequence
1. **Base Defaults**: `minReelsSpinningTimeSecs` equals 1.5 seconds.
2. **Certification**: Game deployed to strictly regulated market (GER), `minReelsSpinningTimeSecs` equals 3.0 seconds.
3. **Licensee**: The Operator sets their global `autoplayAllowed` property to false.
4. **Launch Params**: The player launches the iframe explicitly turning on `turboplayAllowed`.
5. **Bank Overrides**: The user's active wallet uses `JPY` (Japanese Yen). It overrides the formatter with `fractionDigits: 0` and sets `truncateCents: true`.

## 2. Dynamic Assertions

A single `assertCompliance(resolvedConfig)` function acts as the final gatekeeper.
If the Operator accidentally sends a conflicting matrix config, the game **Must Crash Immediatly**.

### Crash Examples
* "Compliance Error: `truncateCents` is true, but `fractionDigits` is > 0." (A math bug risks stripping pennies from players)
* "Compliance Error: `turboplay` is allowed but speed multiplier is strictly non-accelerated at 1.0x" (Turboplay acts as a false toggle doing nothing visually to save time)
