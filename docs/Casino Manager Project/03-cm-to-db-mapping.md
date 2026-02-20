# CM to DB Mapping (Initial)

## Mapping Principles
- Use provider CM report semantics (`reportId`, filter keys, table columns) as the functional contract.
- Map to Cassandra source-of-truth entities and CM read models materialized from Cassandra.
- Avoid direct decode-heavy reads from blobs on every request; pre-materialize where needed.

## Domain Mapping

## 1) Subcasino / Bank / Game Management
- CM areas:
  - `bankList`, `bankSearch`, `invoiceCompany`
  - `gameConfiguration`, `gameSettings`, `frbGameParameters`
- Primary tables:
  - `rcasinoscks.subcasinocf`
  - `rcasinoscks.bankinfocf`
  - `rcasinoscks.gameinfocf`

## 2) Player Search / Player Statistics
- CM areas:
  - `playerSearch`, `playerCount`, `playerOnlineStatistics`
  - player reports (`playerActivityReport`, `perPlayerSummary`, etc.)
- Primary tables:
  - `rcasinoscks.accountcf`
  - `rcasinoscks.accountcf_ext`
  - `rcasinoks.gamesessioncf`, `rcasinoks.playersessionhistorycf`
  - CM read models materialized from Cassandra.

## 3) Financial / Transaction Reports
- CM areas:
  - `accounting`, `transactions`, `transactionSearch`
  - jackpot contribution and revenue reports
- Primary tables:
  - `rcasinoscks.paymenttransactioncf2`
  - `rcasinoks.betcf`
  - `rcasinoks.httpcall*`, `rcasinoks.metrics*` (report-dependent)
  - CM transaction read models materialized from Cassandra.

## 4) Bonus / FR Bonus / Promotion
- CM areas:
  - `bonus*`, `frbonus*`, `mass*`, `promotion*`
- Primary tables:
  - `rcasinoscks.bonuscf`, `bonuscf_acc`, `frbonuscf`, `frbonuscf_acc`
  - `rcasinoscks.promocampaign*`, `massaward*`
  - `rcasinoks.frbwincf`, `frbonusarchcf`, `bonusarchcf`
  - CM bonus read models materialized from Cassandra.

## 5) Jackpot and Universal Jackpot
- CM areas:
  - `jackpot*`, `jackpots3`, `combinedJackpot3*`, `unj*`
- Primary tables:
  - `rcasinoscks.gameinfocf` (game/bank linkage)
  - `rcasinoscks.mqdata`, `rccf` and jackpot-specific persisted entities
  - `rcasinoks` report/stat tables for historical and calculated views

## Gaps to Close Next
- Build exact table/field matrix per `reportId` (including filter IDs and query predicates).
- Define decoded read-model tables for blob-heavy entities (`jcn`/`scn`) used by CM.
