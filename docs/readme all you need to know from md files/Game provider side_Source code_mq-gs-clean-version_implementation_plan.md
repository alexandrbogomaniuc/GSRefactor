# Implementation Plan - Refactor CommonExecutorService to use Constructor Injection

## Problem
`CommonExecutorService` relies on static state (`UtilsApplicationContextHelper.getApplicationContext()`) in its constructor, which leads to `NullPointerException` if accessed before the context helper is fully initialized. `@DependsOn` has proven insufficient.

## Proposed Changes

### 1. Refactor `CommonExecutorService`
**File**: `d:\OneDrive - Roan Solutions, Inc\DOCUMENTS\Igaming Project\Game provider side\Source code\mq-gs-clean-version\utils\src\main\java\com\dgphoenix\casino\common\util\CommonExecutorService.java`

**Change**:
- Add a new constructor that accepts `IGameServerConfiguration`.
- Retain the default constructor (marked deprecated if possible, or just used for backward compatibility if needed) but update it to use the new logic if feasible, OR just rely on the new one.
- Since I cannot easily search/replace all usages, I should keep the default constructor but maybe make it safer or just rely on the new one bean definition.
- Actually, since I control the bean definition in `GameServerComponentsConfiguration`, I will use the *new* constructor there.

```java
    // New constructor
    public CommonExecutorService(IGameServerConfiguration gameServerConfiguration) {
        String poolSizeParam = gameServerConfiguration.getStringPropertySilent("COMMON_EXECUTOR_SERVICE_POOL_SIZE");
        poolSize = poolSizeParam != null ? Integer.parseInt(poolSizeParam) : DEFAULT_COMMON_EXECUTOR_SERVICE_POOL_SIZE;
    }
```

### 2. Update Configuration
**File**: `d:\OneDrive - Roan Solutions, Inc\DOCUMENTS\Igaming Project\Game provider side\Source code\mq-gs-clean-version\game-server\common-gs\src\main\java\com\dgphoenix\casino\gs\GameServerComponentsConfiguration.java`

**Change**:
- Inject `GameServerConfiguration` into `commonExecutorService` bean method.
- Call the new constructor.

```java
    @Bean
    public CommonExecutorService commonExecutorService(GameServerConfiguration gameServerConfiguration) {
        return new CommonExecutorService(gameServerConfiguration);
    }
```

## Verification Plan

### Automated Build & Deploy
1.  Update `rebuild_and_deploy_v4.ps1` (to v5/v6) to:
    *   Update `CommonExecutorService.java` in container.
    *   Update `GameServerComponentsConfiguration.java` in container.
    *   Rebuild `utils` (mapped to `utils-restricted`), then `common-gs` (which depends on utils), then `web-gs`.
    *   Deploy `ROOT.war`.
    *   Restart server.

### Manual Verification
1.  Monitor `startup_monitor_v6.log` for successful startup.
2.  `curl http://localhost:8081/gs/systemdiagnosis.servlet`.

## Rollback Plan
Revert changes to both Java files.
