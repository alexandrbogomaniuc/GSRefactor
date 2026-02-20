# Task: Repair CommonExecutorService Initialization
**Status**: HANDOVER

## Context
The game server startup is failing with `BeanCreationException` -> `NPE` in `CommonExecutorService`.
We attempted to fix this by switching to Constructor Injection and adding debug logs.
Despite updating source code, rebuilding `utils`, `common-gs`, and `web-gs`, and forcing deployment of `ROOT.war`, the logs show NO debug prints and potentially the old error.

## Accomplished
1.  **Code**: `CommonExecutorService` includes new constructor and `System.err.println("!!! DEBUG ...")`.
2.  **Config**: `GameServerComponentsConfiguration` updated to use new constructor.
3.  **Build**:
    - Bumper `common-gs` version to `1.0.2-SNAPSHOT` to force update.
    - Updated `web-gs` dependency.
    - `rebuild_and_deploy_v6.ps1` successfully builds all modules and copies `ROOT.war`.
4.  **Deployment**:
    - `force_deploy.ps1` stops server, deletes `ROOT` folder, copies new WAR, starts server.
5.  **Documentation**:
    - Updated `AI_INSTRUCTIONS.md` with detailed summary of the "loop" and tool issues.

## Current Blocker
- Tools cannot read `docker logs` output (status "No output").
- User reports logs show `BeanInstantiationException` and NO debug prints, implying old code or environment issue.

## Next Steps (for new Agent)
- Verify `ROOT.war` content for `gsn-utils-restricted-*.jar` class timestamp.
- Debug Maven build chain to ensure `common-gs` 1.0.2-SNAPSHOT is actually packaging the new `utils` JAR.
- Read `AI_INSTRUCTIONS.md`.
