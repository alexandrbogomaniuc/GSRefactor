# CM Crawl Inventory

## Crawl Session
- Date: `2026-02-16` (UTC)
- Method: MCP Chrome DevTools authenticated crawl, read-only.
- Entry page: `https://cm.discreetgaming.com/reports/playerSearch/layout`

## Coverage Summary
- Total configured menu endpoints: `94`
- `navigate` endpoints: `83`
- `dialog` endpoints: `11`
- Crawl result: `94/94` endpoints responded with HTTP `200` during the session.

## Top-Level Menu Groups
- `Alerts` (`3`)
- `Management` (`28`)
- `Monitoring & Statistics` (`5`)
- `Reports & Statistics` (`58`)

## Endpoint Logic Pattern
- Most report endpoints return JSON with:
  - `layout.reportId`
  - `layout.title`
  - `layout.filters[]`
  - `layout.buttons[]` (usually `Search`)
  - `layout.content[]` with one or more table blocks and columns.
- Dialog endpoints return form-like `layout` payloads with action buttons such as:
  - `Create`
  - `Cancel`
  - `Create promo`
  - `Create Mass Award`

## Highest-Complexity Screens (by filters/columns)
- `kpiReport`: `6` filters, `195` columns.
- `winnersLosers`: `7` filters, `76` columns.
- `promotionMonitoring`: `9` filters, `75` columns.
- `gameLaunchStatistics`: `7` filters, `68` columns.

## Common Data Keys Across Reports
- Frequent column identifiers:
  - `subcasinoName`
  - `subcasinoId`
  - `bankId`
  - `bankName`
  - `accountId`
  - `accountName`
  - `accountExternalId`
  - `gameId`
  - `gameName` / `gameTitle`
  - `currencyId` / `currencyCode`

## Special Endpoints
- `.../complete` endpoints discovered:
  - `roleList/complete`
  - `notificationList/complete`
  - `currencyExchangeRates/complete`

## Notes
- This inventory is sufficient to start report-to-table mapping and CM clone scaffolding.
- Next pass should capture per-report filter IDs/parameter contracts into a structured matrix.
