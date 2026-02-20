# Complete Game Build and Deployment Procedure

## 🎯 Overview
This document provides the complete step-by-step procedure for building HTML5 game clients and deploying them to the game server.

---

## ⚠️ Prerequisites

### Required Software
- **Node.js v16.20.2** (critical - v24 won't work with webpack 4)
- **Git Bash** (for running build scripts on Windows)
- **Maven** (for building WAR files)
- **Docker** (for container deployment)

### Verify Node Version
```powershell
node --version
# Must show v16.20.2
```

---

## 🔧 Part 1: Fix Webpack Configuration (One-Time Setup)

### Problem
The original webpack configuration can't handle PIXI.js ESM modules (`.mjs` files), causing build errors like:
```
Can't import the named export 'settings' from non EcmaScript module
```

### Solution: Update Webpack Configs

For **each game** you want to build, update two webpack config files:

#### File 1: `game/webpack.config.build.js`

**Location**: `mq-client-clean-version/<game-name>/game/webpack.config.build.js`

**Change 1** - Add `.mjs` to resolve.extensions (around line 44):
```javascript
resolve: {
    extensions: ['.js', '.json', '.mjs'],  // Add .mjs here
    alias: {
        "P2M": PIXI_SRC
    }
},
```

**Change 2** - Add module rule for `.mjs` files (around line 29):
```javascript
module: {
    rules: [
        {
            test: /\.mjs$/,
            include: /node_modules/,
            type: 'javascript/auto'
        },
        {
            test: /\.js$/,
            include: [PIXI_SRC, PROJECT_SRC, SHARED_SRC],
            exclude: [],
            loader: 'babel-loader',
            query: { presets: ["es2015", "stage-0"], compact: false }
        }
    ],
```

#### File 2: `lobby/webpack.config.build.js`

**Location**: `mq-client-clean-version/<game-name>/lobby/webpack.config.build.js`

Apply the **same two changes** as above.

---

## 📦 Part 2: Build the Game

### Step 1: Navigate to Client Directory
```powershell
cd "d:\OneDrive - Roan Solutions, Inc\DOCUMENTS\Igaming Project\Game provider side\Source code\mq-client-clean-version"
```

### Step 2: Install Dependencies (First Time Only)
```powershell
& "C:\Program Files\Git\bin\bash.exe" project_build.sh install dragonstone
```

**What this does**:
- Installs PIXI library dependencies
- Installs game-specific dependencies
- Takes 2-3 minutes

### Step 3: Build the Game
```powershell
& "C:\Program Files\Git\bin\bash.exe" project_build.sh build dragonstone
```

**What this does**:
- Compiles `game/src/` → `game/dist/build/game.js`
- Compiles `lobby/src/` → `lobby/dist/build/game.js`
- Copies all assets to `dist/build/`
- Takes 5-10 minutes

**Expected output**:
```
Building module: dragonstone
Building dragonstone/game
...
Building dragonstone/lobby
...
Completed build for dragonstone
```

### Step 4: Verify Build Output

Check lobby build:
```powershell
Get-ChildItem -Path "dragonstone\lobby\dist\build\" | Select-Object Name, @{Name="SizeKB";Expression={[math]::Round($_.Length/1KB,2)}}
```

**Expected files**:
- `game.js` (~15 MB - this is the bundled game)
- `validator.js`
- `version.json`
- `common_ue.js`
- `index.html`
- `assets/` directory

Check game build:
```powershell
Get-ChildItem -Path "dragonstone\game\dist\build\" | Select-Object Name, @{Name="SizeKB";Expression={[math]::Round($_.Length/1KB,2)}}
```

Should have similar files.

---

## 🚀 Part 3: Deploy to Game Server

### Step 1: Copy Built Files to Webapp

Navigate back to project root:
```powershell
cd "d:\OneDrive - Roan Solutions, Inc\DOCUMENTS\Igaming Project"
```

Copy lobby files:
```powershell
Copy-Item -Path "Game provider side\Source code\mq-client-clean-version\dragonstone\lobby\dist\build\*" `
  -Destination "Game provider side\Source code\mq-gs-clean-version\game-server\web-gs\src\main\webapp\html5pc\actiongames\dragonstone\lobby\" `
  -Recurse -Force
```

Copy game files:
```powershell
Copy-Item -Path "Game provider side\Source code\mq-client-clean-version\dragonstone\game\dist\build\*" `
  -Destination "Game provider side\Source code\mq-gs-clean-version\game-server\web-gs\src\main\webapp\html5pc\actiongames\dragonstone\game\" `
  -Recurse -Force
```

### Step 2: Rebuild WAR File

```powershell
cd "Game provider side\Source code\mq-gs-clean-version\game-server\web-gs"
mvn clean package -DskipTests
```

**This takes**: 5-10 minutes

**Success indicator**: 
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

**Output file**: `target/ROOT.war` (should be ~100-200 MB)

### Step 3: Deploy WAR to Docker

```powershell
docker cp "target\ROOT.war" gp3-gs-1:/var/lib/jetty/webapps/ROOT.war
```

### Step 4: Clean Jetty Temp Files

```powershell
docker exec gp3-gs-1 rm -rf /tmp/jetty-*
```

### Step 5: Restart Game Server

```powershell
docker restart gp3-gs-1
```

**Wait**: 30-60 seconds for the server to start.

### Step 6: Verify Deployment

Check if Jetty extracted the WAR:
```powershell
docker exec gp3-gs-1 ls -la /tmp/jetty-0.0.0.0-8080-ROOT.war-_-any-*/webapp/html5pc/actiongames/dragonstone/lobby/ | Select-Object -First 10
```

Should see `game.js`, `validator.js`, etc.

---

## ✅ Part 4: Test the Game

### Test 1: Check version.json

Open in browser:
```
http://localhost:8081/html5pc/actiongames/dragonstone/lobby/version.json
```

**Expected**: JSON response with version info

### Test 2: Check game.js

```
http://localhost:8081/html5pc/actiongames/dragonstone/lobby/game.js
```

**Expected**: JavaScript file downloads (15MB)

### Test 3: Launch the Game

```
http://localhost:8081/cwstartgamev2.do?bankId=6274&gameId=838&mode=real&token=bav_game_session_001&lang=en
```

**Expected**: 
- Game loads without 404 errors
- Browser console shows no "failed to load" errors
- Game interface renders

---

## 🐛 Troubleshooting

### Build Errors

**Error**: `Can't import the named export from non EcmaScript module`
- **Cause**: Webpack config not updated
- **Fix**: Apply webpack config changes from Part 1

**Error**: `error:0308010C:digital envelope routines::unsupported`
- **Cause**: Node.js version too new
- **Fix**: Downgrade to Node.js v16.20.2

**Error**: `bash: command not found`
- **Cause**: Git Bash not in PATH
- **Fix**: Use full path: `& "C:\Program Files\Git\bin\bash.exe"`

### Deployment Errors

**404 for game.js**
- **Cause**: Files not copied to webapp or WAR not deployed
- **Fix**: Repeat Part 3 steps carefully

**Jetty not extracting WAR**
- **Cause**: Old temp files interfering
- **Fix**: `docker exec gp3-gs-1 rm -rf /tmp/jetty-*` then restart

**Game loads but assets missing**
- **Cause**: Assets not copied from build
- **Fix**: Check `dist/build/assets/` exists, rebuild if needed

---

## 📊 Build Times Reference

| Step | Duration |
|------|----------|
| Install dependencies | 2-3 minutes |
| Build game | 3-5 minutes |
| Build lobby | 3-5 minutes |
| Maven package WAR | 5-10 minutes |
| Docker restart | 30-60 seconds |
| **Total** | **15-25 minutes** |

---

## 🔄 Building Other Games

To build a different game (e.g., `missionamazon`, `sectorx`):

1. Apply webpack config changes (Part 1) to that game's configs
2. Replace `dragonstone` with the game name in all commands:
   ```powershell
   & "C:\Program Files\Git\bin\bash.exe" project_build.sh build missionamazon
   ```
3. Follow same deployment steps

---

## 📝 Quick Reference Commands

### Full Build & Deploy Sequence
```powershell
# 1. Build
cd "d:\OneDrive - Roan Solutions, Inc\DOCUMENTS\Igaming Project\Game provider side\Source code\mq-client-clean-version"
& "C:\Program Files\Git\bin\bash.exe" project_build.sh build dragonstone

# 2. Copy to webapp
cd "d:\OneDrive - Roan Solutions, Inc\DOCUMENTS\Igaming Project"
Copy-Item -Path "Game provider side\Source code\mq-client-clean-version\dragonstone\lobby\dist\build\*" -Destination "Game provider side\Source code\mq-gs-clean-version\game-server\web-gs\src\main\webapp\html5pc\actiongames\dragonstone\lobby\" -Recurse -Force
Copy-Item -Path "Game provider side\Source code\mq-client-clean-version\dragonstone\game\dist\build\*" -Destination "Game provider side\Source code\mq-gs-clean-version\game-server\web-gs\src\main\webapp\html5pc\actiongames\dragonstone\game\" -Recurse -Force

# 3. Rebuild WAR
cd "Game provider side\Source code\mq-gs-clean-version\game-server\web-gs"
mvn clean package -DskipTests

# 4. Deploy
docker cp "target\ROOT.war" gp3-gs-1:/var/lib/jetty/webapps/ROOT.war
docker exec gp3-gs-1 rm -rf /tmp/jetty-*
docker restart gp3-gs-1

# 5. Test
Start-Process "http://localhost:8081/cwstartgamev2.do?bankId=6274&gameId=838&mode=real&token=bav_game_session_001&lang=en"
```

---

## 🎓 Notes

- **Webpack Config Changes**: Only needed once per game, persist in git
- **Node.js Version**: Critical - must be v16, not v24
- **Build Output Size**: ~15MB for game.js is normal (includes all game code, PIXI, Vue)
- **WAR File Size**: 100-200MB is normal (includes all game assets)
- **Deployment Time**: Server takes 30-60s to restart and extract WAR

---

**Last Updated**: 2026-01-14  
**Tested With**: Node.js v16.20.2, Webpack 4, PIXI.js Legacy
