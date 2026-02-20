# AI Assistant Instructions for iGaming Project

## 📚 **Key Documentation Files**

### Architecture & Configuration
- **[Game Client Architecture](file:///d:/OneDrive%20-%20Roan%20Solutions,%20Inc/DOCUMENTS/Igaming%20Project/GAME_CLIENT_ARCHITECTURE.md)** - Complete guide to mq-client-clean-version HTML5 game client structure, build system, dependencies, and how to create new games
- **[Game Build & Deploy Procedure](file:///d:/OneDrive%20-%20Roan%20Solutions,%20Inc/DOCUMENTS/Igaming%20Project/GAME_BUILD_DEPLOY_PROCEDURE.md)** - Step-by-step procedure for building games with webpack and deploying to game server
- **[Cassandra Schema](file:///d:/OneDrive%20-%20Roan%20Solutions,%20Inc/DOCUMENTS/Igaming%20Project/CASSANDRA_SCHEMA.md)** - Database schema documentation
- **[BAV Integration Walkthrough](file:///C:/Users/Alex/.gemini/antigravity/brain/185eb388-0b36-4a0f-9a5a-fa3972f40dbd/walkthrough.md)** - Successful BAV authentication integration

### Build & Deployment
- **[Game Build Instructions](file:///d:/OneDrive%20-%20Roan%20Solutions,%20Inc/DOCUMENTS/Igaming%20Project/GAME_BUILD_INSTRUCTIONS.md)** - How to build HTML5 games with webpack
- **[Game Asset Deployment](file:///d:/OneDrive%20-%20Roan%20Solutions,%20Inc/DOCUMENTS/Igaming%20Project/GAME_ASSET_DEPLOYMENT.md)** - Static file deployment guide

## 🎯 **Project Context**

### System Components
1. **Game Provider (Java/Jetty)** - Port 8081, handles game sessions, authentication
2. **Casino Side (Python/FastAPI)** - Port 8000, BAV authentication endpoint
3. **Cassandra Database** - Port 9142, stores configuration and game data
4. **Game Clients (HTML5)** - PIXI.js + Vue.js games in `mq-client-clean-version/`

### Current Status
- ✅ **BAV Integration**: Complete and functional
- ✅ **Currency Conversion**: USD↔EUR working (0.92 rate)
- ✅ **Session Management**: Working correctly
- ⏳ **Game Assets**: Need webpack build before deployment

## 🔑 **Important Paths**

```
Project Root: E:\Dev\Igaming Project\

├── Casino side/
│   └── inst_app/igw/app/providers/bsg_bav/  # BAV implementation
│
└── Game provider side/
    ├── Source code/
    │   ├── mq-gs-clean-version/             # Game server (Java)
    │   └── mq-client-clean-version/         # Game clients (HTML5)
    │       ├── dragonstone/
    │       ├── missionamazon/
    │       └── ...
    └── Docker containers/
```

## 📋 **Common Tasks**

### Building Games
```bash
cd "Game provider side/Source code/mq-client-clean-version"
./project_build.sh build <game-name>
# Output: <game-name>/lobby/dist/build/ and <game-name>/game/dist/build/
```

### Deploying to Game Server
```bash
# Copy built files to webapp
cp -r dragonstone/lobby/dist/build/* \
  ../mq-gs-clean-version/game-server/web-gs/src/main/webapp/html5pc/actiongames/dragonstone/lobby/

# Rebuild WAR
cd ../mq-gs-clean-version/game-server/web-gs
mvn clean package -DskipTests

# Deploy to Docker
docker cp target/ROOT.war gp3-gs-1:/var/lib/jetty/webapps/ROOT.war
docker restart gp3-gs-1
```

### Database Access
```bash
docker exec -it gp3-c1-1 cqlsh
USE RCasinoSCKS;
```

## 🚨 **Remember**

1. **Game assets must be built** before deployment (webpack builds `game.js`)
2. **Jetty uses DefaultServlet** for static files (`/html5pc/*` mapping in web.xml)
3. **BAV authentication** uses plain string tokens, not JWTs
4. **Currency rates** stored in `RCasinoSCKS.currencyratescf` table
5. **Bank configurations** in `RCasinoSCKS.bankinfocf` table

## 📖 **Reference Links**

- PIXI.js Docs: https://pixijs.io/guides/
- Vue.js 2: https://v2.vuejs.org/
- Webpack 4: https://v4.webpack.js.org/

---

**Last Updated**: 2026-01-14  
**Project Owner**: Non-professional developer (explain concepts simply)

# ⚠️ BUILD STRATEGY: NO EXTERNAL SERVICE DEPENDENCIES

> **Date**: 2026-01-27
> **Status**: ACTIVE

To ensure local development and deployment reliability, we have removed dependencies on external services like **Google BigQuery** and consolidated the **Slim Build** strategy.

## 🚫 BigQuery Removal
- **Interface stayed**: `IAnalyticsDBClientService` is still present.
- **Implementation Stubbed**: `BigQueryClientService.java` is now a no-op stub.
- **Dependency Removed**: `google-cloud-bigquery` is commented out in `games/common-games/pom.xml`.

## 🛠️ Slim Build & Patches
- **Game Modules**: Many complex game modules are excluded in `web/pom.xml` to speed up builds and avoid broken dependencies.
- **Core Stubs**: Classes that referenced excluded game modules (like `maxcrashgame`) have been patched to remove those dependencies:
    - `RoomPlayersMonitorService.java`: Removed `AbstractCrashGameRoom` usage.
    - `CrashGameInfo.java`: Stubbed out crash-specific round history types.
    - `GsonFactory.java`: Removed registration for crash-specific types.
    - `SendUpdateCrashHistoryTask.java`: Stubbed out history update logic.

## 📋 Ongoing Maintenance
1. **Adding Games**: If adding a new game module, ensure it doesn't re-introduce BigQuery or recursive core dependencies.
2. **Restoration**: To restore full functionality (including BigQuery/Crash), reverse the patches and uncomment dependencies.

