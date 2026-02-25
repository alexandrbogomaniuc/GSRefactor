# Architecture & Workflow Visual Pack (Milestone 4)

Last updated: 2026-02-25 (UTC)
Audience: non-technical stakeholders, project owners, support leads

## Executive Summary (Simple English)
This document shows what the Game Server system looked like before the modernization work, what it looks like now, and what the target cutover-ready shape is supposed to be.

Important: the project has many completed deliverables, but the system is still **not approved for production cutover**. The current blocker is not "missing architecture ideas". The blocker is mainly runtime canary validation and security dependency audit work.

Current factual status (from the latest readiness report):
- Checklist completion: `41/41`
- Cutover readiness: `NO_GO_CUTOVER_PENDING_VALIDATION`
- Main blockers: Phase 4 runtime canary, Phase 5/6 runtime canary, security dependency audit/lockfiles

## 1. Before Project: Legacy GS Architecture (Baseline)
```mermaid
flowchart LR
    Browser["Browser / Partner / Legacy Client"] --> LegacyGS["Legacy GS Monolith (Java)"]
    LegacyMP["Legacy MP / Lobby Service"] --> Browser
    LegacyGS --> LegacyMP
    LegacyGS --> Wallet["Casino Side Wallet / Auth"]
    LegacyGS --> LegacyCassandra["Cassandra 2.x Data Store"]
    LegacyGS --> LegacyConfig["Support Pages + Bank/Game Config Editors"]
    LegacyGS --> LegacyCache["Config Caches + Invalidation Logic"]
    LegacyGS --> LegacyHistory["History / Reporting Paths"]
    LegacyGS --> LegacyHooks["Legacy Hooks / Extensions"]
```

How to read this:
This is the starting point. Most important work happens inside one large Java application (the legacy GS monolith). It talks directly to wallet/auth systems, Cassandra, multiplayer paths, config logic, and support tools.

## 2. After Project (Current State): Mixed Modernization Runtime (Today)
```mermaid
flowchart LR
    Browser["Browser"] --> StaticFacade["Refactor Static Facade (/startgame)"]
    StaticFacade --> RefactorGS["Refactor GS (legacy-compatible Java entry)"]

    RefactorGS --> LegacyMPClient["Legacy MP / Legacy Client Paths (still used in mixed-topology validation)"]
    RefactorGS --> ProtocolAdapter["Protocol Adapter Service (Phase 4)"]
    RefactorGS --> SessionSvc["Session Service"]
    RefactorGS --> GameplaySvc["Gameplay Orchestrator"]
    RefactorGS --> WalletSvc["Wallet Adapter"]
    RefactorGS --> BonusSvc["Bonus / FRB Service"]
    RefactorGS --> HistorySvc["History Service"]
    RefactorGS --> MultiplayerSvc["Multiplayer Service"]
    RefactorGS --> ConfigSvc["Config Service / Portal Bridge"]

    GameplaySvc --> Redis["Redis 7.2 (ephemeral state cache only)"]
    SessionSvc --> Kafka["Kafka 7.3.2"]
    RefactorGS --> Kafka
    Kafka --> ZK["ZooKeeper 3.8"]

    RefactorGS --> CassandraLegacy["Cassandra 2.1.20 (legacy/refactor source path still present)"]
    RefactorGS --> CassandraTarget["Cassandra 4.1 target (migration rehearsal target)"]

    CanaryFlags["Route flags + canary bank settings"] --> ProtocolAdapter
    CanaryFlags --> GameplaySvc
    CanaryFlags --> WalletSvc
    CanaryFlags --> BonusSvc
    CanaryFlags --> HistorySvc

    Blocker["Current blockers: Phase 4/5 canary runtime + security audit"]:::blocker

    ProtocolAdapter -. "runtime canary failing" .-> Blocker
    GameplaySvc -. "canary routing not approved" .-> Blocker
    WalletSvc -. "canary routing not approved" .-> Blocker
    BonusSvc -. "canary routing not approved" .-> Blocker
    HistorySvc -. "canary routing not approved" .-> Blocker

    classDef blocker fill:#fdecec,stroke:#d9534f,color:#7f1d1d;
```

How to read this:
The architecture is now split into multiple services, but the cutover is still partial. The refactor GS is acting as the compatibility entry point while some new services are running but not yet approved for traffic ownership (because canary checks are failing or not enabled).

## 3. Target Cutover-Ready Architecture (What the project is trying to reach)
```mermaid
flowchart LR
    Browser["Browser / Partners / Clients"] --> Facade["Compatibility Facade (GS entry)"]
    Facade --> SessionSvc["Session Service"]
    Facade --> Policy["Protocol / Policy Layer (JSON/XML by bank)"]
    Facade --> ConfigSvc["Config Service + Safe Portal Workflow"]

    Policy --> GameplaySvc["Gameplay Orchestrator"]
    GameplaySvc --> WalletSvc["Wallet Adapter"]
    GameplaySvc --> BonusSvc["Bonus / FRB Service"]
    GameplaySvc --> HistorySvc["History Service"]
    Facade --> MultiplayerSvc["Multiplayer Service (only when bank flag allows)"]

    SessionSvc --> Kafka["Kafka Backbone"]
    GameplaySvc --> Kafka
    ConfigSvc --> Kafka
    Kafka --> Consumers["History / Metrics / Audit Consumers"]

    GameplaySvc --> Redis["Redis (ephemeral cache / reconnect / idempotency acceleration)"]
    Facade --> Cassandra4["Cassandra 4.x (validated target)"]
    ConfigSvc --> Cassandra4
    HistorySvc --> Cassandra4

    Portal["Operator Config Portal"] --> ConfigSvc
    Portal --> Guardrails["Draft -> Validate -> Approve -> Publish -> Rollback"]
```

How to read this:
This is the intended end-state design direction. The key idea is not "replace everything at once". The key idea is controlled routing and service ownership with rollback options, while keeping compatibility at the entry layer.

## 4. What Changed vs What Stayed Legacy (Simple Comparison)

| Area | Before Project | Current State (Today) | Target Cutover-Ready State |
|---|---|---|---|
| Entry point for game launch | Legacy GS route (`/cwstartgamev2.do`) | Legacy route still works; browser-facing alias `/startgame` added via refactor static facade | Compatibility facade remains, legacy route can stay for compatibility while internal routing is modernized |
| Core execution shape | Mostly one Java monolith path | Mixed: refactor GS entry + extracted services running + legacy fallback/canary routing | Service ownership moved to extracted services behind controlled routing |
| Multiplayer handling | Legacy MP tightly coupled path | Mixed: legacy MP/client still used in validated mixed-topology flow; multiplayer service extraction exists | Multiplayer fully optional by bank (`isMultiplayer`) with clean bypass path |
| Configuration operations | Existing support tools + direct legacy flows | New config portal UI/scaffold and guardrails added (still additive; not replacing all legacy edit flows) | Safe workflow-driven config operations with approvals/publish/rollback |
| Data platform | Legacy Cassandra path (2.1.x line in refactor compose source path) | Cassandra 4 target migration rehearsal and row-count parity proven; overall cutover still blocked by other items | Cassandra 4.x target becomes the approved runtime data target |
| Runtime cache for gameplay state | Mostly in-process / legacy behavior | Redis added for ephemeral deterministic state cache in gameplay orchestrator | Redis continues as optimization only (not money source-of-truth) |
| Cutover decision | Not applicable (legacy already running) | `NO_GO_CUTOVER_PENDING_VALIDATION` | GO only after runtime canary + security blockers are closed |

## 5. Application / Component Version Comparison (Where known)

### 5.1 Infrastructure and Data Components
| Component | Legacy / Baseline (known) | Current Refactor Environment (known) | Notes |
|---|---|---|---|
| Cassandra (legacy path in refactor compose) | `cassandra:2.1.20` | still present as `c1` in refactor compose | Used for legacy/source path and migration support in current mixed environment |
| Cassandra target upgrade path | n/a | `cassandra:4.1` (refactor target service `c1-refactor`) | Current evidence shows migration rehearsal/full-copy parity success, but overall cutover still blocked by other items |
| GS Java baseline target (build metadata) | Java `1.8` in GS Maven poms | still Java `1.8` in audited GS Maven poms | This is a stack-version marker, not a cutover readiness signal |
| Java Cassandra driver (GS code) | DataStax Java driver `3.11.5` | still `3.11.5` | Server moved toward Cassandra 4.x compatibility, but Java driver 4 migration is a separate future refactor |
| Kafka | not specified in early legacy docs | `confluentinc/cp-kafka:7.3.2` | Used as the event/control backbone for extracted services |
| ZooKeeper | not specified in early legacy docs | `zookeeper:3.8` | Supports current Kafka setup in refactor compose |
| Redis | not part of legacy GS core path | `redis:7.2-alpine` | Approved only for ephemeral deterministic state/reconnect/idempotency cache |

### 5.2 Platform / Runtime Shape Comparison
| Area | Before | Now | Status |
|---|---|---|---|
| GS core runtime style | Legacy monolith-centric Java runtime | Compatibility entry + extracted Node services + legacy fallback/canary controls | Partial (running but not fully cutover-approved) |
| Config portal | Legacy support pages only | Additive config portal baseline + workflow scaffold + guardrail UI | Partial (useful and real, but not full write-path replacement) |
| Protocol adapter (JSON/XML by bank) | Legacy behavior only | Protocol adapter phase exists; runtime canary failing | Blocked for cutover |
| Core extracted services (gameplay/wallet/bonus/history) | Inside GS monolith | Separate services up, canary routing not approved | Blocked for cutover |
| Multiplayer split | Legacy MP path | Multiplayer service extraction and bank-flag routing proof exist | Strong progress / partially cutover-proven |
| Precision / min amount | Legacy hardcoded constraints | Phase 8 closure says ready with 0 blockers | Strong completion evidence |
| Branding rename (ABS) | Legacy names | Controlled wave tooling + pilot wave complete | Partial by design |

### 5.3 Network / Runtime Endpoints (Practical view)
| Function | Before (legacy style) | Current refactor environment |
|---|---|---|
| Browser launch endpoint | `/cwstartgamev2.do` | `/startgame` alias at refactor static (`:18080`) while legacy endpoint remains compatible |
| GS support pages | Legacy GS support path | Refactor GS support path (commonly `:18081/support/...` when runtime is up) |
| Legacy MP websocket (mixed-topology validation) | `ws://localhost:6300/...` | legacy MP still used in mixed-topology validation; refactor facade/rewrites support local testing |

## 6. Workflow Comparison: Launch Flow (Before vs Now)

### 6.1 Before Project (Legacy Launch Flow)
```mermaid
flowchart TD
    B["Browser opens legacy launch URL"] --> GSR["Legacy GS launch action"]
    GSR --> Auth["Validate token/auth with casino side"]
    Auth --> Sess["Open session + create game context"]
    Sess --> Branch["Single-player / Multiplayer / Bonus branches"]
    Branch --> MPorTpl["Redirect to MP template or game template"]
    MPorTpl --> Game["Game starts"]
```

How to read this:
This is the old flow most operators already know. One main GS route handles validation, session creation, branching, and redirect/handoff.

### 6.2 Current State (Refactor Mixed Launch Flow)
```mermaid
flowchart TD
    B["Browser opens /startgame on refactor static facade"] --> Static["Static facade / rewrite layer"]
    Static --> RefGS["Refactor GS compatibility entry"]
    RefGS --> Can["Route decision / canary flags"]
    Can -->|"fallback or disabled"| LegacyPath["Legacy-compatible path still used"]
    Can -->|"enabled bank/path"| NewSvc["Refactor service path (protocol/session/etc.)"]
    LegacyPath --> Handoff["Handoff to legacy MP/client or template"]
    NewSvc --> Handoff
    Handoff --> Game["Game starts if all checks pass"]

    Can -. "Current blockers: some canary routes fail/not enabled" .-> Block["No-Go cutover decision"]
```

How to read this:
The browser sees a cleaner launch URL (`/startgame`), but inside the system there is still a routing decision. Some traffic can still go through legacy-compatible paths because canary routes are not yet fully approved.

## 7. Workflow Comparison: Wager / Settle Ownership (Before vs Now)

### 7.1 Before Project (Legacy Ownership)
```mermaid
flowchart LR
    Client["Game Client"] --> GS["Legacy GS handlers"]
    GS --> Wallet["Wallet manager calls"]
    GS --> Session["Session state logic"]
    GS --> Bonus["Bonus / FRB logic"]
    GS --> Persist["Cassandra persisters"]
    GS --> History["History/reporting path"]
```

How to read this:
In the legacy model, one main GS runtime owns almost everything in the round lifecycle. This is simpler to trace in one place, but hard to change safely and hard to scale by responsibility.

### 7.2 Current State (Partial Service Ownership)
```mermaid
flowchart LR
    Client["Game Client"] --> Facade["GS compatibility entry"]
    Facade --> RouteDecision["Canary route decision"]
    RouteDecision -->|"legacy fallback"| LegacyGSFlow["Legacy GS internal handlers"]
    RouteDecision -->|"canary enabled"| Gameplay["Gameplay Orchestrator"]
    Gameplay --> WalletSvc["Wallet Adapter"]
    Gameplay --> BonusSvc["Bonus / FRB Service"]
    Gameplay --> HistSvc["History Service"]
    Gameplay --> Redis["Redis cache (ephemeral only)"]
    Gameplay --> Persist["Durable persistence / Cassandra + outbox patterns"]

    LegacyGSFlow -. "still needed until canary approvals pass" .-> Note["Mixed ownership is intentional during migration"]
```

How to read this:
The project is in a strangler migration stage. Some ownership has been split into services, but fallback paths still exist. This is intentional for safety, but it also means the final cutover decision depends on canary validation.

## 8. Workflow Comparison: Configuration Change (Before vs Now)

### 8.1 Before Project (Legacy Support Editing Flow)
```mermaid
flowchart TD
    Operator["Operator uses legacy support pages"] --> Edit["Edit bank/game settings"]
    Edit --> Save["Save change in legacy flow"]
    Save --> Invalidate["Invalidate / refresh config across nodes"]
    Invalidate --> Runtime["GS nodes reload config"]
```

How to read this:
This is a short direct workflow. It can be fast, but it depends a lot on operator experience and can be risky when changes are made under pressure.

### 8.2 Current State (Portal Scaffold + Guardrails, Additive)
```mermaid
flowchart TD
    Operator["Operator opens /support/configPortal.jsp"] --> Select["Select bank and review effective values"]
    Select --> Draft["Draft metadata / scaffold workflow"]
    Draft --> Validate["Validate checks (portal scaffold)"]
    Validate --> Approve["Approve step (scaffold / local checks)"]
    Approve --> Publish["Publish / Rollback actions with guardrail warnings"]
    Publish --> Bridge["Optional config-service bridge (feature flag)"]
    Bridge --> LegacyFallback["Fallback to safe local scaffold if bridge disabled/unavailable"]
    LegacyFallback --> CompatEditor["Legacy editor remains available for compatibility"]
```

How to read this:
The new portal is currently an additive safety layer and workflow scaffold. It improves visibility and guardrails, but it does not fully replace all legacy editing flows yet.

## 9. Where the Current Blockers Sit (Visual map)
```mermaid
flowchart LR
    Ready["Cutover Readiness Decision"] --> Phase4["Phase 4 Protocol Adapter Runtime"]
    Ready --> Phase56["Phase 5/6 Core Service Runtime"]
    Ready --> Security["Security Dependency Audit / Lockfiles"]
    Ready --> Phase7["Phase 7 Cassandra Rehearsal"]
    Ready --> Mixed["Mixed-topology Validation"]

    Phase4 --> P4State["NO_GO_RUNTIME_FAILURE"]
    Phase56 --> P56State["NO_GO_RUNTIME_FAILURE"]
    Security --> SecState["TESTED_NO_GO_DEPENDENCY_LOCK_AUDIT_PENDING"]
    Phase7 --> P7State["Cleared in latest readiness (no-go flag = NO)"]
    Mixed --> MixState["MANUAL_FULL_FLOW_PASS"]
```

How to read this:
This diagram shows why the project can look "complete" on a checklist and still be blocked. Some important gates are already cleared (Cassandra rehearsal blocker and mixed-topology manual flow), but the cutover decision remains blocked by runtime canary validation and security work.

## 10. What Was Modernized vs What Is Still Legacy (Scope Clarity)

### Modernized / Added (real progress)
- Phase-based program governance with evidence-driven GO/NO-GO reporting
- Refactor service stack (config, session, gameplay, wallet adapter, bonus/FRB, history, multiplayer, protocol adapter)
- Kafka backbone for extracted services and event/control patterns
- Redis ephemeral state cache foundation for deterministic reconnect/idempotency support
- Config portal baseline + workflow scaffold + guardrail UI
- Cassandra 4 target migration rehearsal/full-copy parity evidence
- Precision modernization (0.001 support verification phase closure)

### Still legacy or intentionally kept for compatibility (today)
- Legacy-compatible GS entry behavior at key routes
- Legacy MP/client paths still used in validated mixed-topology flow
- Legacy fallback paths during canary-disabled or failing routes
- Legacy support/editor flows still available while portal workflow remains additive/scaffold-first
- Some broad branding/namespace rename scope intentionally deferred

## 11. Evidence and Source Files Used (for this visual pack)
- `/Users/alexb/Documents/Dev/Dev_new/docs/16-gs-behavior-map-and-runtime-flow-blueprint.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/21-modernization-roadmap-v1.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/program-deploy-readiness-status-20260224-163502.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/158-legacy-parity-status-report-frb-mp-baseline-complete-20260224-124500.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/validation/legacy-mixed-topology/legacy-mixed-topology-manual-full-flow-20260224-162730.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/32-gs-config-portal-all-levels-spec.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/136-phase3-config-portal-publish-rollback-guardrails-visualization-20260224-091500.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/28-redis-state-blob-and-deterministic-math-adr-v1.md`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/cassandra-cache/pom.xml`

## 12. Stakeholder Reading Note
If you only read one thing in this document, read sections **2**, **5**, and **9**. They show:
- what exists now,
- what is still blocked,
- and why "41/41 complete" is not the same as "ready to cut over".
