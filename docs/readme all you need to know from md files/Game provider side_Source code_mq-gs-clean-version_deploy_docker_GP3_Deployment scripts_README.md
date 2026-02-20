# Deployment Scripts

Automated deployment scripts for all GP3 services with comprehensive logging and verification.

## Quick Start

```powershell
cd "d:\OneDrive - Roan Solutions, Inc\DOCUMENTS\Igaming Project\Game provider side\Source code\mq-gs-clean-version\deploy\docker\GP3\Deployment scripts"

# Deploy everything
.\deploy-all.ps1

# Or deploy individual services
.\deploy-game-server.ps1
.\deploy-mp-lobby.ps1
.\restart-infrastructure.ps1
```

## Scripts

### 1. `deploy-all.ps1` - Full Stack Deployment

Deploys all services in correct order with dependencies.

```powershell
.\deploy-all.ps1              # Full build + deploy
.\deploy-all.ps1 -SkipBuild   # Deploy only (no Maven build)
```

**Order:**
1. Infrastructure (Cassandra → Zookeeper → Kafka)
2. MP Lobby
3. Game Server

### 2. `deploy-game-server.ps1` - Game Server Only

```powershell
.\deploy-game-server.ps1              # Build + deploy
.\deploy-game-server.ps1 -SkipBuild   # Deploy only
```

### 3. `deploy-mp-lobby.ps1` - MP Lobby Only

```powershell
.\deploy-mp-lobby.ps1              # Build + deploy
.\deploy-mp-lobby.ps1 -SkipBuild   # Deploy only
```

### 4. `restart-infrastructure.ps1` - Infrastructure Services

Restarts Cassandra, Zookeeper, and Kafka.

```powershell
.\restart-infrastructure.ps1
```

## Logs

All logs saved to `logs/` directory with timestamps:
- `deploy-all-YYYYMMDD-HHmmss.log`
- `deploy-gs-YYYYMMDD-HHmmss.log`
- `deploy-mp-lobby-YYYYMMDD-HHmmss.log`
- `restart-infrastructure-YYYYMMDD-HHmmss.log`

## Verification

After deployment, verify:

**Game Server:**
```powershell
# Check container
docker ps | Select-String "gp3-gs-1"

# Test endpoint
curl http://localhost:8081/

# Test game
# http://localhost:8081/cwstartgamev2.do?bankId=6274&gameId=838&mode=real&token=bav_game_session_001&lang=en
```

**MP Lobby:**
```powershell
# Check container
docker ps | Select-String "gp3-mp-lobby-1"

# Check logs for server ID
docker logs gp3-mp-lobby-1 | Select-String "server started with id"
```

**Infrastructure:**
```powershell
docker ps | Select-String "gp3-c1-1|gp3-zookeeper-1|gp3-kafka-1"
```

## Troubleshooting

### Build Fails
- Check Maven output in log file
- Ensure dependencies available
- Try manual build: `cd game-server; mvn clean install`

### Deployment Fails
- Check log file for error details
- Verify containers running: `docker ps -a`
- Check container logs: `docker logs <container-name>`

### WAR Not Extracted
- Check server logs in log file
- Restart container manually
- Verify WAR file integrity

## Service Ports

| Service | Container | Port |
|---------|-----------|------|
| Game Server | gp3-gs-1 | 8081 |
| MP Lobby | gp3-mp-lobby-1 | 8080 |
| Cassandra | gp3-c1-1 | 9142 |
| Zookeeper | gp3-zookeeper-1 | 2181 |
| Kafka | gp3-kafka-1 | 9092 |

## Best Practices

1. **Always review logs** after deployment
2. **Test endpoints** before considering complete
3. **Use -SkipBuild** when only redeploying WAR
4. **Check container logs** if verification fails
5. **Deploy infrastructure first** when starting fresh
