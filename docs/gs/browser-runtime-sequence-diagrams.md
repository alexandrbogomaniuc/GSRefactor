# GS Browser Runtime Sequence Diagrams (New Slots)

- Status: Draft for implementation (Phase 1)
- Date: 2026-02-28
- Contract version: `slot-browser-v1`
- Related:
  - `docs/gs/browser-runtime-api-contract.md`
  - `docs/gs/internal-slot-runtime-contract.md`

## 1) Launch + Bootstrap + OpenGame

```mermaid
sequenceDiagram
    participant Browser
    participant Launch as GS Launch Route
    participant API as GS Browser Runtime API
    participant Core as GS Core

    Browser->>Launch: GET /cwstartgamev2.do?bankId&subCasinoId&gameId&mode&token&lang
    Launch->>Core: Validate launch/auth/context
    Core-->>Launch: SID + route + bootstrap refs
    Launch-->>Browser: 302 redirect to client shell with SID and runtime refs

    Browser->>API: POST /slot/v1/bootstrap (sessionId + bootstrapRef; no requestCounter/idempotencyKey)
    API->>Core: Resolve config/policies/versions (read-only)
    Core-->>API: Bootstrap payload
    API-->>Browser: bootstrap(configId, policies, math/client versions; state unchanged)

    Browser->>API: POST /slot/v1/opengame
    API->>Core: Validate session/counter/idempotency/state
    Core->>Core: Load persisted unfinished state marker
    Core-->>API: Open response payload
    API-->>Browser: stateVersion, balance, presentationPayload, restore block
```

## 2) PlayRound (Reserve -> Engine -> Settle)

```mermaid
sequenceDiagram
    participant Browser
    participant API as GS Browser Runtime API
    participant Core as GS Core
    participant Wallet as Wallet Adapter
    participant Engine as Slot Engine Host
    participant DB as GS Persistence

    Browser->>API: POST /slot/v1/playround (sessionId,counter,idempotencyKey,selectedBet)
    API->>Core: Validate session + counter + idempotency + bootstrapRef
    Core->>Wallet: Reserve bet (authoritative)
    Wallet-->>Core: reserve ok + walletOperationId
    Core->>Engine: playRound(resolved financial intent + reserveContext)
    Engine-->>Core: round outcome + settle summary + server audit
    Core->>Wallet: Settle win/net effect
    Wallet-->>Core: settle ok + updated balance
    Core->>DB: Persist round/session/wallet/history/snapshot
    Core-->>API: Browser response (no serverAudit/rngTraceRef)
    API-->>Browser: balance + stateVersion + round/feature + presentationPayload
```

## 3) FeatureAction (Priced and Non-Priced)

```mermaid
sequenceDiagram
    participant Browser
    participant API as GS Browser Runtime API
    participant Core as GS Core
    participant Wallet as Wallet Adapter
    participant Engine as Slot Engine Host
    participant DB as GS Persistence

    Browser->>API: POST /slot/v1/featureaction (selectedFeatureChoice)
    API->>Core: Validate session/counter/idempotency/state
    alt Priced feature action
        Core->>Wallet: Reserve priced action debit
        Wallet-->>Core: reserve ok
    end
    Core->>Engine: featureAction(...)
    Engine-->>Core: updated feature state + outcome + settle summary + server audit
    Core->>Wallet: Settle (if needed)
    Wallet-->>Core: settled balance
    Core->>DB: Persist updates
    Core-->>API: Browser-safe payload
    API-->>Browser: feature progression + presentationPayload
```

## 4) ResumeGame (Reconnect / Unfinished Round)

```mermaid
sequenceDiagram
    participant Browser
    participant API as GS Browser Runtime API
    participant Core as GS Core
    participant DB as GS Persistence
    participant Engine as Slot Engine Host

    Browser->>API: POST /slot/v1/resumegame
    API->>Core: Validate session + idempotency + state preconditions
    Core->>DB: Load persisted unfinished snapshot + latest stateVersion/counter
    alt Unfinished round exists
        Core->>Engine: resumeGame(snapshot + selected package/version/model)
        Engine-->>Core: deterministic reconstructed state + server audit
        Core-->>API: restore payload + stateVersion + balances
    else No unfinished round
        Core-->>API: clean resume state + stateVersion
    end
    API-->>Browser: restore block + presentationPayload
```

## 5) GetHistory (Browser-Pulled from GS)

```mermaid
sequenceDiagram
    participant Browser
    participant API as GS Browser Runtime API
    participant Core as GS Core
    participant DB as GS Persistence

    Browser->>API: POST /slot/v1/gethistory (sessionId + requestCounter; no idempotencyKey)
    API->>Core: Validate session + correlation counter (read-only)
    Core->>DB: Query history rows by session/player scope
    DB-->>Core: History records
    Core-->>API: Normalized history payload
    API-->>Browser: history list/page cursor
```

## 6) CloseGame

```mermaid
sequenceDiagram
    participant Browser
    participant API as GS Browser Runtime API
    participant Core as GS Core
    participant Engine as Slot Engine Host
    participant DB as GS Persistence

    Browser->>API: POST /slot/v1/closegame
    API->>Core: Validate session/counter/idempotency
    Core->>Engine: closeGame(reason)
    Engine-->>Core: final feature/session state + server audit
    Core->>DB: Persist close markers and final snapshot
    Core-->>API: close response
    API-->>Browser: final stateVersion + balance + close status
```
