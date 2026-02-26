# Phase 8 Precision Remediation Buckets and Wave Plan (20260226-090309 UTC)

- scope: GS-only (`game-server` + `refactor-services`)
- purpose: convert raw Phase 8 audit findings into safe remediation waves with rollback-friendly sequencing
- compatibility rule: no protocol or runtime contract changes during these waves

## Recommended Wave Order (safe-first)
1. Wave 1: reporting/statistics cent-based assumptions (non-financial outputs first)
2. Wave 2: game settings / dynamic coin derivation / FRB fallback precision assumptions
3. Wave 3: config/template coin minima and bank/game defaults (with canary bank validation)
4. Wave 4: core financial settlement and wallet/gameplay precision paths (highest risk; idempotency/parity gated)

## Bucket Counts
- wave1_reporting_stats: 0
- wave2_settings_coin_rules: 0
- wave3_config_templates: 10
- wave4_core_financial_paths: 47

## Acceptance Gates Per Wave
- Add/extend deterministic precision vectors before code changes in the target bucket.
- Run `${REPO_ROOT}/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` after each batch.
- Preserve backward-compatible request/response and bank routing behavior.
- Canary on selected banks (start with bank `6275` where applicable).
- No cutover of financial paths without parity evidence and rollback path.

## wave1_reporting_stats
- description: Wave 1 candidate (lower-risk first): reporting/display cent conversions and score rounding (explicitly excludes core session/update paths)
- hits: 0
- top files:
```text
(none)
```
- sample matches:
```text
(none)
```

## wave2_settings_coin_rules
- description: Wave 2 candidate: game settings / dynamic coin derivation / FRB coin fallback assumptions
- hits: 0
- top files:
```text
(none)
```
- sample matches:
```text
(none)
```

## wave3_config_templates
- description: Wave 3 candidate: config/template defaults and bank/game coin value constraints (requires bank-by-bank compatibility review)
- hits: 10
- top files:
```text
4 /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml
4 /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml
2 /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml
```
- sample matches:
```text
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml:11:        <minValue>100</minValue>
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml:57:          <value>100</value>
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml:761:        <minValue>100</minValue>
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml:807:          <value>100</value>
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml:11:        <minValue>100</minValue>
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml:25:                <value>100</value>
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml:11:        <minValue>100</minValue>
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml:57:          <value>100</value>
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml:708:        <minValue>100</minValue>
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml:754:          <value>100</value>
```

## wave4_core_financial_paths
- description: Wave 4 candidate (highest risk): core financial and settlement precision/rounding paths
- hits: 47
- top files:
```text
11 /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/currency/CurrencyRatesManager.java
9 /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/dblink/BonusDBLink.java
7 /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/GamesLevelHelper.java
6 /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/singlegames/tools/util/NumberUtils.java
5 /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/tracker/ExternalSourceCurrencyRateExtractor.java
5 /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/promo/feed/tournament/TournamentFeedWriter.java
4 /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/tool/DoubleContextualSerializer.java
```
- sample matches:
```text
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/tracker/ExternalSourceCurrencyRateExtractor.java:16:import java.math.BigDecimal;
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/tracker/ExternalSourceCurrencyRateExtractor.java:126:            rate = new BigDecimal(rate).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/tracker/ExternalSourceCurrencyRateExtractor.java:205:            return new BigDecimal(rateToEur).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/tracker/ExternalSourceCurrencyRateExtractor.java:217:            return new BigDecimal(rateToEur).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/tracker/ExternalSourceCurrencyRateExtractor.java:232:            return new BigDecimal(eurRate).divide(new BigDecimal(fromRate), 8, BigDecimal.ROUND_HALF_UP).doubleValue();
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/promo/feed/tournament/TournamentFeedWriter.java:18:import java.math.BigDecimal;
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/promo/feed/tournament/TournamentFeedWriter.java:19:import java.math.RoundingMode;
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/promo/feed/tournament/TournamentFeedWriter.java:53:            BigDecimal longScore = BigDecimal.valueOf(rank.getScore());
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/promo/feed/tournament/TournamentFeedWriter.java:54:            BigDecimal score = longScore.divide(TournamentObjective.BD_HIGHEST_WIN_MULTIPLIER, 2,
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/promo/feed/tournament/TournamentFeedWriter.java:55:                    RoundingMode.DOWN);
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/singlegames/tools/util/NumberUtils.java:3:import java.math.BigDecimal;
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/singlegames/tools/util/NumberUtils.java:4:import java.math.RoundingMode;
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/singlegames/tools/util/NumberUtils.java:21:        //    return (new BigDecimal(d).setScale(2,4).doubleValue());//ROUND_HALF_UP
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/singlegames/tools/util/NumberUtils.java:34:        return BigDecimal.valueOf(minorUnits, scale).doubleValue();
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/singlegames/tools/util/NumberUtils.java:39:        return new BigDecimal(value)
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/singlegames/tools/util/NumberUtils.java:41:                .setScale(0, RoundingMode.HALF_UP)
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/GamesLevelHelper.java:9:import java.math.BigDecimal;
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/GamesLevelHelper.java:11:import java.math.RoundingMode;
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/GamesLevelHelper.java:33:    private static final MathContext DIVIDE_CONTEXT = new MathContext(5, RoundingMode.DOWN);
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/GamesLevelHelper.java:254:            BigDecimal inclusionFrequency = new BigDecimal(availableCoins.size())
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/GamesLevelHelper.java:255:                    .divide(new BigDecimal(coinsNumber - 1), DIVIDE_CONTEXT);
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/GamesLevelHelper.java:256:            BigDecimal inclusionThreshold = inclusionFrequency.subtract(new BigDecimal(1));
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/settings/GamesLevelHelper.java:258:                BigDecimal position = new BigDecimal(i);
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/dblink/BonusDBLink.java:29:import java.math.BigDecimal;
/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/dblink/BonusDBLink.java:31:import java.math.RoundingMode;
```

## Immediate Next Actions
1. Add bucket-specific vector smoke for Wave 1 reporting/statistics conversions (cent vs thousandth formatting/parsing boundaries).
2. Inventory exact call sites in `MQServiceHandler` and `GameSettingsManager` for behavior-preserving wrappers/adapters.
3. Define config compatibility rules for bank/game/currency minima before changing template values.
4. Prepare parity assertions for wallet/gameplay amounts before any Wave 4 code edits.
