# Phase 9 ABS Rename Patch-Plan Export (W0)

- Source scan report: /Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-candidate-scan-20260226-062924.md
- Manifest: /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json
- Root: /Users/alexb/Documents/Dev/Dev_new/gs-server
- Wave: W0
- Plan mode: review-only (per-file grouped patch plan; no file changes)
- Auto-candidate mappings: 5
- Grouped files: 24
- Review-only mappings with hits (excluded): 1
- Snippet context lines: 0
- Max snippets per file: 10

## File Groups (Review Queue)

| File | Total Hits | Mapping Count | Suggested Action |
|---|---:|---:|---|
| `game-server/web-gs/src/main/resources/ClusterConfiguration.xml` | 68 | 3 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 52 | 2 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/config/mpstress/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | 28 | 2 | review-and-replace-nonruntime-string (wave W0) |
| `bitbucket-pipelines.bck2.yml` | 20 | 1 | review-and-replace-nonruntime-string (wave W0) |
| `bitbucket-pipelines.yml` | 20 | 1 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/web-gs/pom.xml` | 18 | 1 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 13 | 2 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/config/mqb/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | 13 | 1 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/config/common.properties` | 10 | 2 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/config/mpstress/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml` | 5 | 2 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/config/mqb/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml` | 3 | 2 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/web-gs/src/main/webapp/free/mp/template.jsp` | 3 | 1 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/web-gs/src/main/webapp/real/mp/template.jsp` | 3 | 1 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/web-gs/src/main/webapp/standlobby.jsp` | 3 | 1 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml` | 2 | 2 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml` | 2 | 2 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsTemplateCache.xml` | 2 | 2 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/config/mpstress/com.dgphoenix.casino.common.cache.SubCasinoCache.xml` | 2 | 2 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/config/mqb/com.dgphoenix.casino.common.cache.SubCasinoCache.xml` | 2 | 2 | review-and-replace-nonruntime-string (wave W0) |
| `deploy/docker/configs/docker-compose.yml` | 1 | 1 | review-and-replace-nonruntime-string (wave W0) |
| `deploy/docker/refactor/docker-compose.yml` | 1 | 1 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/web-gs/src/main/webapp/support/configPortal.jsp` | 1 | 1 | review-and-replace-nonruntime-string (wave W0) |
| `game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml` | 1 | 1 | review-and-replace-nonruntime-string (wave W0) |
| `k8s/development_maxduel/deployment/mq-gs.yaml` | 1 | 1 | review-and-replace-nonruntime-string (wave W0) |

## Excluded Review-Only Mappings With Hits

| Legacy | Hits | Files | Disposition |
|---|---:|---:|---|
| `mq` | 291 | 63 | REVIEW_ONLY_HIT |

## File Plan: game-server/web-gs/src/main/resources/ClusterConfiguration.xml

- total hits (summed across mappings): 68
- mapping count: 3
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 38 |
| `nucleus` | `abs` / `ABS` | 19 |
| `discreetgaming` | `abs` / `ABS` | 11 |

### Snippet Preview

```text
[betsoft] 188:        <endpoint>http://gs1.sb.betsoftgaming.com/services/gameserverconfigurationservice.ws</endpoint>
[betsoft] 189:        <transferservice>http://gs1.sb.betsoftgaming.com/services/sbmigrationservice.ws</transferservice>
[betsoft] 254:        <endpoint>http://gs1.188bet.betsoftgaming.com/services/gameserverconfigurationservice.ws</endpoint>
[betsoft] 255:        <transferservice>http://gs1.188bet.betsoftgaming.com/services/sbmigrationservice.ws</transferservice>
[betsoft] 422:        <endpoint>http://gs1-democluster.betsoftgaming.com/services/gameserverconfigurationservice.ws</endpoint>
[betsoft] 423:        <transferservice>http://gs1-democluster.betsoftgaming.com/services/sbmigrationservice.ws</transferservice>
[betsoft] 446:        <endpoint>http://gs1-sis.betsoftgaming.com/services/gameserverconfigurationservice.ws</endpoint>
[betsoft] 447:        <transferservice>http://gs1-sis.betsoftgaming.com/services/clustertransferservice.ws</transferservice>
[betsoft] 484:        <endpoint>http://gs1-gp3.betsoftgaming.com/services/gameserverconfigurationservice.ws</endpoint>
[betsoft] 485:        <transferservice>http://gs1-gp3.betsoftgaming.com/services/clustertransferservice.ws</transferservice>
```

## File Plan: game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml

- total hits (summed across mappings): 52
- mapping count: 2
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 32 |
| `maxquest` | `abs` / `ABS` | 20 |

### Snippet Preview

```text
[betsoft] 71:              <string>https://txs.maxquest.com/omegatron/spr/betsoftWallet/authenticate</string>
[betsoft] 75:              <string>https://txs.maxquest.com/omegatron/spr/betsoftWallet/getBalance</string>
[betsoft] 79:              <string>https://txs.maxquest.com/omegatron/spr/betsoftWallet/betResult</string>
[betsoft] 83:              <string>https://txs.maxquest.com/omegatron/spr/betsoftWallet/refundBet</string>
[betsoft] 87:              <string>https://txs.maxquest.com/omegatron/spr/betsoftWallet/bonusRelease</string>
[betsoft] 91:              <string>https://txs.maxquest.com/omegatron/spr/betsoftWallet/authenticate</string>
[betsoft] 95:              <string>https://txs.maxquest.com/omegatron/spr/betsoftWallet/getAccountInfo</string>
[betsoft] 108:              <string>http://gs1-stress.betsoftgaming.com/config/stub/auth.jsp</string>
[betsoft] 112:              <string>http://gs1-stress.betsoftgaming.com/config/stub/balance.jsp</string>
[betsoft] 116:              <string>http://gs1-stress.betsoftgaming.com/config/stub/wager_sleep.jsp</string>
```

## File Plan: game-server/config/mpstress/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml

- total hits (summed across mappings): 28
- mapping count: 2
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 22 |
| `maxquest` | `abs` / `ABS` | 6 |

### Snippet Preview

```text
[betsoft] 7:      <host>games-stress.betsoftgaming.com</host>
[betsoft] 8:      <gsDomain>-stress.betsoftgaming.com</gsDomain>
[betsoft] 41:      <fromSupportEmail>support@report-stress.betsoftgaming.com</fromSupportEmail>
[betsoft] 54:      <domain>-stress.betsoftgaming.com</domain>
[betsoft] 184:            <string>{clusters:{&apos;games-mp-stress.betsoftgaming.com&apos;: {&apos;1&apos;: {&apos;host&apos;: &apos;gs1&apos;, port: 6300}, &apos;2&apos;: {&apos;host&apos;: &apos;gs2&apos;, port: 6300}, &apos;3&apos;: {&apos;host&apos;: &apos;gs3&apos;, port: 6300}, &apos;4&apos;: {&apos;host&apos;: &apos;gs4&apos;, port: 6300}}}}</string>
[betsoft] 220:          <string>games-mp-stress.betsoftgaming.com</string>
[betsoft] 242:      <host>games-stress.betsoftgaming.com</host>
[betsoft] 243:      <gsDomain>-stress.betsoftgaming.com</gsDomain>
[betsoft] 276:      <fromSupportEmail>support@report-stress.betsoftgaming.comm</fromSupportEmail>
[betsoft] 289:      <domain>-stress.betsoftgaming.com</domain>
```

## File Plan: bitbucket-pipelines.bck2.yml

- total hits (summed across mappings): 20
- mapping count: 1
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `maxduel` | `abs` / `ABS` | 20 |

### Snippet Preview

```text
[maxduel] 47:            export GCP_PROJECT_ID=${GCP_PROJECT_ID_DEV_MAXDUEL}
[maxduel] 48:            export GCP_PROJECT_NUMBER=${GCP_PROJECT_NUMBER_DEV_MAXDUEL}
[maxduel] 49:            export GCP_REGION=${GCP_REGION_DEV_MAXDUEL}
[maxduel] 50:            export PROJECT_NAME=${PROJECT_NAME_DEV_MAXDUEL}
[maxduel] 51:            export K8S_NAMESPACE=${K8S_NAMESPACE_DEV_MAXDUEL}
[maxduel] 52:            export GCP_USERNAME_PREFIX=${GCP_USERNAME_PREFIX_DEV_MAXDUEL}
[maxduel] 53:            export RUNNER_ID=${RUNNER_ID_DEV_MAXDUEL}
[maxduel] 54:            export SERVER_IP_1=${GS1_IP_ADDRESS_DEV_MAXDUEL}
[maxduel] 55:            export SERVER_IP_2=${GS2_IP_ADDRESS_DEV_MAXDUEL}
[maxduel] 56:            export SERVER_IP_3=${GS3_IP_ADDRESS_DEV_MAXDUEL}
```

## File Plan: bitbucket-pipelines.yml

- total hits (summed across mappings): 20
- mapping count: 1
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `maxduel` | `abs` / `ABS` | 20 |

### Snippet Preview

```text
[maxduel] 15:            export GCP_PROJECT_ID=${GCP_PROJECT_ID_DEV_MAXDUEL}
[maxduel] 16:            export GCP_PROJECT_NUMBER=${GCP_PROJECT_NUMBER_DEV_MAXDUEL}
[maxduel] 17:            export GCP_REGION=${GCP_REGION_DEV_MAXDUEL}
[maxduel] 18:            export PROJECT_NAME=${PROJECT_NAME_DEV_MAXDUEL}
[maxduel] 19:            export K8S_NAMESPACE=${K8S_NAMESPACE_DEV_MAXDUEL}
[maxduel] 20:            export RUNNER_ID=${RUNNER_ID_DEV_MAXDUEL}
[maxduel] 27:          export GCP_PROJECT_ID=${GCP_PROJECT_ID_STAGE_MAXDUEL}
[maxduel] 28:          export GCP_PROJECT_NUMBER=${GCP_PROJECT_NUMBER_STAGE_MAXDUEL}
[maxduel] 29:          export GCP_REGION=${GCP_REGION_STAGE_MAXDUEL}
[maxduel] 30:          export PROJECT_NAME=${PROJECT_NAME_STAGE_MAXDUEL}
```

## File Plan: game-server/web-gs/pom.xml

- total hits (summed across mappings): 18
- mapping count: 1
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `nucleus` | `abs` / `ABS` | 18 |

### Snippet Preview

```text
[nucleus] 33:            src/main/webapp/common/nucleus/vabs/**/*.js
[nucleus] 195:                                    <directory>${basedir}/src/main/webapp/css_nucleus</directory>
[nucleus] 196:                                    <targetPath>${basedir}/target/static-nucleus/css</targetPath>
[nucleus] 207:                                    <directory>${basedir}/src/main/webapp/images_nucleus</directory>
[nucleus] 208:                                    <targetPath>${basedir}/target/static-nucleus/images</targetPath>
[nucleus] 231:                        <id>vabs-nucleus</id>
[nucleus] 236:                                    <directory>${basedir}/src/main/webapp/common/nucleus/vabs</directory>
[nucleus] 237:                                    <targetPath>${basedir}/target/static-nucleus/common/vabs</targetPath>
[nucleus] 246:                                    <targetPath>${basedir}/target/static-nucleus/common/vabs/VabEngine</targetPath>
[nucleus] 286:                        <id>vabs-nucleus</id>
```

## File Plan: game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml

- total hits (summed across mappings): 13
- mapping count: 2
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 9 |
| `maxquest` | `abs` / `ABS` | 4 |

### Snippet Preview

```text
[betsoft] 98:              <!-- <string>http://gs1-stress.betsoftgaming.com/config/stub/wager_sleep.jsp</string> -->
[betsoft] 103:              <!-- <string>http://gs1-stress.betsoftgaming.com/config/stub/refund.jsp</string> -->
[betsoft] 108:              <!-- <string>default-stress.betsoftgaming.com</string> -->
[betsoft] 686:          <string>https://txs.maxquest.com/omegatron/spr/betsoftWallet/bonusWin</string>
[betsoft] 804:              <!-- <string>http://gs1-stress.betsoftgaming.com/config/stub/auth.jsp</string> -->
[betsoft] 833:              <!-- <string>http://gs1-stress.betsoftgaming.com/config/stub/wager_sleep.jsp</string> -->
[betsoft] 838:              <!-- <string>http://gs1-stress.betsoftgaming.com/config/stub/refund.jsp</string> -->
[betsoft] 843:              <!-- <string>default-stress.betsoftgaming.com</string> -->
[betsoft] 1424:          <string>https://txs.maxquest.com/omegatron/spr/betsoftWallet/bonusWin</string>
[maxquest] 7:      <externalBankIdDescription>www.maxquest.com_MaxCoins</externalBankIdDescription>
```

## File Plan: game-server/config/mqb/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml

- total hits (summed across mappings): 13
- mapping count: 1
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `maxquest` | `abs` / `ABS` | 13 |

### Snippet Preview

```text
[maxquest] 7:      <!-- <host>games-mqb.maxquest.com</host> -->
[maxquest] 9:      <!-- <gsDomain>-mqb.maxquest.com</gsDomain> -->
[maxquest] 44:      <!-- <fromSupportEmail>support@report-mqb.maxquest.com</fromSupportEmail> -->
[maxquest] 45:	  <fromSupportEmail>support@maxquest.com</fromSupportEmail>
[maxquest] 59:      <!-- <domain>-mqb.maxquest.com</domain> -->
[maxquest] 130:          <string>maxquestpass</string>
[maxquest] 193:            <!-- <string>{clusters:{&apos;games-mp-mqb.maxquest.com&apos;: {&apos;1&apos;: {&apos;host&apos;: &apos;gs1&apos;, port: 6300}, &apos;2&apos;: {&apos;host&apos;: &apos;gs2&apos;, port: 6300}, &apos;3&apos;: {&apos;host&apos;: &apos;gs3&apos;, port: 6300}, &apos;4&apos;: {&apos;host&apos;: &apos;gs4&apos;, port: 6300}}}}</string> -->
[maxquest] 231:          <!-- <string>games-mp-mqb.maxquest.com</string> -->
[maxquest] 362:          <string>maxquestpass</string>
[maxquest] 495:            <fromSupportEmail>support@report-gp3.maxquest.com</fromSupportEmail>
```

## File Plan: game-server/config/common.properties

- total hits (summed across mappings): 10
- mapping count: 2
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 7 |
| `nucleus` | `abs` / `ABS` | 3 |

### Snippet Preview

```text
[betsoft] 7:brandName=Betsoft
[betsoft] 8:brandNameUpperCase=BETSOFT
[betsoft] 9:brandNameLowCase=betsoft
[betsoft] 10:fullBrandName=BetsoftGaming
[betsoft] 11:fullBrandNameUpperCase=BETSOFTGAMING
[betsoft] 12:fullBrandNameLowCase=betsoftgaming
[betsoft] 13:brandLiveDomainSuffix=betsoftgaming.com
[nucleus] 4:nucleus-integration-clients-goldbet20-excludes=nop
[nucleus] 5:nucleus-integration-clients-smartlive-excludes=nop
[nucleus] 6:nucleus-web-resources-excludes=
```

## File Plan: game-server/config/mpstress/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml

- total hits (summed across mappings): 5
- mapping count: 2
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 4 |
| `maxquest` | `abs` / `ABS` | 1 |

### Snippet Preview

```text
[betsoft] 7:            <host>games-stress.betsoftgaming.com</host>
[betsoft] 20:            <host>games-stress.betsoftgaming.com</host>
[betsoft] 33:            <host>games-stress.betsoftgaming.com</host>
[betsoft] 46:            <host>games-stress.betsoftgaming.com</host>
[maxquest] 6:<!--            <host>games-gp3.maxquest.com</host>-->
```

## File Plan: game-server/config/mqb/com.dgphoenix.casino.common.cache.LoadBalancerCache.xml

- total hits (summed across mappings): 3
- mapping count: 2
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 2 |
| `maxquest` | `abs` / `ABS` | 1 |

### Snippet Preview

```text
[betsoft] 7:            <!-- <host>games-stress.betsoftgaming.com</host> -->
[betsoft] 47:            <host>games-stress.betsoftgaming.com</host>
[maxquest] 6:<!--            <host>games-gp3.maxquest.com</host> -->
```

## File Plan: game-server/web-gs/src/main/webapp/free/mp/template.jsp

- total hits (summed across mappings): 3
- mapping count: 1
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `maxquest` | `abs` / `ABS` | 3 |

### Snippet Preview

```text
[maxquest] 95:                bankInfo.getMaxQuestWeaponMode().name(),
[maxquest] 238:            'MQ_WEAPONS_MODE': '<%=bankInfo.getMaxQuestWeaponMode().name()%>',
[maxquest] 239:            'ABS_WEAPONS_MODE': '<%=bankInfo.getMaxQuestWeaponMode().name()%>',
```

## File Plan: game-server/web-gs/src/main/webapp/real/mp/template.jsp

- total hits (summed across mappings): 3
- mapping count: 1
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `maxquest` | `abs` / `ABS` | 3 |

### Snippet Preview

```text
[maxquest] 177:                bankInfo.getMaxQuestWeaponMode().name(),
[maxquest] 350:            'MQ_WEAPONS_MODE': '<%=bankInfo.getMaxQuestWeaponMode().name()%>',
[maxquest] 351:            'ABS_WEAPONS_MODE': '<%=bankInfo.getMaxQuestWeaponMode().name()%>',
```

## File Plan: game-server/web-gs/src/main/webapp/standlobby.jsp

- total hits (summed across mappings): 3
- mapping count: 1
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `maxquest` | `abs` / `ABS` | 3 |

### Snippet Preview

```text
[maxquest] 27:                String maxquestPass = GameServerConfiguration.getInstance().getStringPropertySilent("maxquestpass");
[maxquest] 28:                if (!StringUtils.isTrimmedEmpty(maxquestPass)) {
[maxquest] 29:                    openGameLink = openGameLink.replace("gameId=", "pass=" + maxquestPass + "&gameId=");
```

## File Plan: game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml

- total hits (summed across mappings): 2
- mapping count: 2
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 1 |
| `maxquest` | `abs` / `ABS` | 1 |

### Snippet Preview

```text
[betsoft] 580:          <string>https://txs.maxquest.com/omegatron/spr/betsoftWallet/bonusWin</string>
[maxquest] 580:          <string>https://txs.maxquest.com/omegatron/spr/betsoftWallet/bonusWin</string>
```

## File Plan: game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml

- total hits (summed across mappings): 2
- mapping count: 2
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 1 |
| `maxquest` | `abs` / `ABS` | 1 |

### Snippet Preview

```text
[betsoft] 38:      <fromSupportEmail>support@report-stress.betsoftgaming.com</fromSupportEmail>
[maxquest] 113:          <string>maxquestpass</string>
```

## File Plan: game-server/config/local-machine/com.dgphoenix.casino.common.cache.ServerConfigsTemplateCache.xml

- total hits (summed across mappings): 2
- mapping count: 2
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 1 |
| `maxquest` | `abs` / `ABS` | 1 |

### Snippet Preview

```text
[betsoft] 38:      <fromSupportEmail>support@report-stress.betsoftgaming.com</fromSupportEmail>
[maxquest] 113:          <string>maxquestpass</string>
```

## File Plan: game-server/config/mpstress/com.dgphoenix.casino.common.cache.SubCasinoCache.xml

- total hits (summed across mappings): 2
- mapping count: 2
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 1 |
| `maxquest` | `abs` / `ABS` | 1 |

### Snippet Preview

```text
[betsoft] 14:          <string>default-stress.betsoftgaming.com</string>
[maxquest] 13:<!--        <string>lobby-gp3.maxquest.com</string>-->
```

## File Plan: game-server/config/mqb/com.dgphoenix.casino.common.cache.SubCasinoCache.xml

- total hits (summed across mappings): 2
- mapping count: 2
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 1 |
| `maxquest` | `abs` / `ABS` | 1 |

### Snippet Preview

```text
[betsoft] 14:          <!-- <string>default-stress.betsoftgaming.com</string> -->
[maxquest] 13:<!--        <string>lobby-gp3.maxquest.com</string>-->
```

## File Plan: deploy/docker/configs/docker-compose.yml

- total hits (summed across mappings): 1
- mapping count: 1
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 1 |

### Snippet Preview

```text
[betsoft] 57:      com.betsoft.casino.mp.web.NettyServer"
```

## File Plan: deploy/docker/refactor/docker-compose.yml

- total hits (summed across mappings): 1
- mapping count: 1
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `betsoft` | `abs` / `ABS` | 1 |

### Snippet Preview

```text
[betsoft] 71:      com.betsoft.casino.mp.web.NettyServer"
```

## File Plan: game-server/web-gs/src/main/webapp/support/configPortal.jsp

- total hits (summed across mappings): 1
- mapping count: 1
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `maxquest` | `abs` / `ABS` | 1 |

### Snippet Preview

```text
[maxquest] 62:        if (key.startsWith("MQ_") || key.contains("MAXQUEST")) {
```

## File Plan: game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml

- total hits (summed across mappings): 1
- mapping count: 1
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `maxquest` | `abs` / `ABS` | 1 |

### Snippet Preview

```text
[maxquest] 124:        <!-- MaxQuest leaderboards -->
```

## File Plan: k8s/development_maxduel/deployment/mq-gs.yaml

- total hits (summed across mappings): 1
- mapping count: 1
- suggested action: review-and-replace-nonruntime-string

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `maxduel` | `abs` / `ABS` | 1 |

### Snippet Preview

```text
[maxduel] 66:        image: europe-west1-docker.pkg.dev/maxduel-dev01/md-dev01/mq-gs-test:latest
```
