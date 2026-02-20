package com.betsoft.casino.bots;

import com.betsoft.casino.bots.mqb.ManagedMaxBlastChampionsRoomBot;
import com.betsoft.casino.bots.strategies.*;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.web.GsonFactory;
import com.betsoft.casino.mp.web.GsonMessageSerializer;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Starter {
    private static final Logger LOG = LogManager.getLogger(Starter.class);
    private static final Set<Long> UNIFIED_BOT_GAMES_ID = new HashSet<>(Arrays.asList(GameType.MAXCRASHGAME.getGameId(),
            GameType.TRIPLE_MAX_BLAST.getGameId()));
    private static final Set<Long> BTG_BOT_GAMES_ID = new HashSet<>(Arrays.asList(GameType.BG_DRAGONSTONE.getGameId(),
            GameType.BG_MISSION_AMAZON.getGameId(), GameType.BG_SECTOR_X.getGameId()));

    //LOG_STAT must be use logger for StatisticsManager
    private static final Logger LOG_STAT = LogManager.getLogger(StatisticsManager.class);
    protected AtomicInteger activeBots = new AtomicInteger(0);
    protected Gson gson = GsonFactory.createGson();
    protected List<IBot> bots = new ArrayList<>();
    protected Disposable statisticsUpdater;
    protected Disposable botStatsUpdater;

    public static void main(String[] args) throws InterruptedException {
        new Starter().start(args);
    }

    public void start(String[] args) throws InterruptedException {
        ArgsParser options = new ArgsParser();
        options.parse(args);
        long gameId = options.getGameId();
        boolean unifiedGame = UNIFIED_BOT_GAMES_ID.contains(gameId);
        boolean btgGame = BTG_BOT_GAMES_ID.contains(gameId);

        String launchUrl = options.getUrl() +
                "?bankId=" + options.getBankId() +
                "&gameId=" + gameId +
                "&mode=" + options.getMode() +
                "&pass=" + options.getPass() +
                "&botStrategy=" + options.getBotStrategy() +
                "&specialPaidWeaponId=" + options.getSpecialPaidWeaponId() +
                "&requestedBetLevel=" + options.getRequestedBetLevel() +
                "&buyIn=" + options.getRequestedByInAmount() +
                "&token=";
        StatisticsManager.getInstance().setEnableStatistics(true);
        statisticsUpdater = Flux.interval(Duration.ofSeconds(60)).subscribe(i -> {
            StringBuilder sb = new StringBuilder();
            StatisticsManager.getInstance().printRequestStatistics(sb, true);
            LOG_STAT.info(sb.toString());
        });
        botStatsUpdater = Flux.interval(Duration.ofMinutes(10)).subscribe(i -> {
            for (IBot bot : bots) {
                LOG_STAT.debug("Stats for Bot-{}: {}", bot.getId(), bot.getStats());
                if (bot instanceof IRoomBot) {
                    IRoomBot roomBot = (IRoomBot) bot;
                    LOG_STAT.debug("Current state: {}, lastChangeDate={}", roomBot.getState(), new Date(roomBot.getLastStateChangeDate()));
                }
            }
        });
        StatisticsManager.getInstance().registerStatisticsGetter("Starter",
                () -> "activeBots=" + activeBots.get() + ", bots.size=" + bots.size());
        logAndPrintToConsole("Start: options=" + options + ", launchUrl=" + launchUrl);
        int additionalSleepCounter = 0;
        long optionsRoomId = options.getRoomId();
        List<Long> customRoomIds = options.getCustomRoomIds();
        int idxCustomRoom = customRoomIds.size() - 1;

        for (int i = 1; i <= options.getBotsCount(); i++) {
            String nickname = options.getBotPrefix() + i + options.getNickname();
            String username = options.getEmailPrefix() + i + options.getEmailDomain();
            String token = getToken(username, options.getPass(), options.getGoogleAuthUrl(), options.getTokenGenerationUrl());

            logAndPrintToConsole("Start: Process bot nickname=" + nickname + "; username=" + username + "; token=" + token);

            if (unifiedGame) {
                createUnifiedBot(Integer.toString(i), options.getGameId(), options.getBankId(),
                        launchUrl, token, nickname, null, getCrashBotStrategy(options), options.getRoomId());
            } else if(btgGame) {
                createBtgBot(Integer.toString(i), launchUrl, token, nickname, null, getBotStrategy(options), options,
                        idxCustomRoom > 0 ? customRoomIds.get(idxCustomRoom--) : optionsRoomId);
            } else if(gameId == GameType.BG_MAXCRASHGAME.getGameId()) {
                //TO DO: check how to deal with bot's external id
                // createMaxBlastBot(Integer.toString(i), options.getGameId(), options.getBankId(), nickname, nickname, username, null, getBotStrategy(options), options);
            }
            else {
                createBot(Integer.toString(i), options.getGameId(), options.getBankId(), launchUrl, token, nickname,
                        null, getBotStrategy(options), options.getRoomId());
            }
            sleepSeating(options.getDelayTimeBetweenSeatIn());
            additionalSleepCounter++;
            if (unifiedGame && additionalSleepCounter > 30) {
                sleepSeating(500);
                additionalSleepCounter = 0;
            }
        }
        logAndPrintToConsole("Start: All bots started");
        waitForBotsShutdown();
        printStats();
        statisticsUpdater.dispose();
        botStatsUpdater.dispose();
    }

    private void sleepSeating(long sleepTime) throws InterruptedException {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            LOG.debug("Interrupted seating bots", e);
            throw e;
        }
    }

    private void logAndPrintToConsole(String s) {
        System.out.println(s);
        LOG.info(s);
    }

    private IUnifiedBotStrategy getCrashBotStrategy(ArgsParser options) {
        IUnifiedBotStrategy botStrategy;
        switch (options.getBotStrategy()) {
            case "CrashGameBotStrategyCustom":
                botStrategy = new CrashGameBotStrategyCustom(options.getNumberRoundsBeforeRestart(), options.getAstroParams());
                LOG.debug("CrashGameBotStrategyCustom: {} ", botStrategy);
                break;
            default:
                botStrategy = new CrashGameBotStrategy(options.getNumberRoundsBeforeRestart());
                LOG.debug("CrashGameBotStrategy: {} ", botStrategy);
                break;
        }
        LOG.debug("botStrategy: {}, botStrategyString: {}", botStrategy, options.getBotStrategy());
        return botStrategy;
    }


    private IRoomBotStrategy getBotStrategy(ArgsParser options) {
        IRoomBotStrategy botStrategy;
        int specialPaidWeaponId = options.getSpecialPaidWeaponId();
        int requestedBetLevel = options.getRequestedBetLevel();
        boolean allowedUseDroppedSW = options.isAllowedUseDroppedSW();
        long buyIn = options.getRequestedByInAmount();
        String requestedEnemyIds = options.getRequestedEnemyIds();

        switch (options.getBotStrategy()) {
            case "NaturalBattleGroundMissionAmazonStrategy":
                botStrategy = new NaturalBattleGroundMissionAmazonStrategy(100, requestedBetLevel, options.getRequestedByInAmount());
                break;
            case "NaturalBattleGroundDragonStoneStrategy":
                botStrategy = new NaturalBattleGroundDragonStoneStrategy(100, requestedBetLevel, options.getRequestedByInAmount());
                break;
            case "NaturalBattleGroundSectorXStrategy":
                botStrategy = new NaturalBattleGroundSectorXStrategy(100, requestedBetLevel, options.getRequestedByInAmount(), requestedEnemyIds);
                break;
            case "CogStrategyWithWeaponId":
                botStrategy = new CogStrategyWithWeaponId(200, specialPaidWeaponId, requestedBetLevel);
                break;
            case "DSPaidShotStrategyWithId":
                botStrategy = new DSPaidShotStrategyWithId(100, specialPaidWeaponId, requestedBetLevel, allowedUseDroppedSW);
                break;
            case "CryoGunPaidShots":
                botStrategy = new CryoGunPaidShots(100);
                break;
            case "PiratesWithoutBuyInSpecialWeapons":
                botStrategy = new PiratesWithoutBuyInSpecialWeapons(100);
                break;
            case "PiratesWithBuyInSpecialWeapons":
                botStrategy = new PiratesWithBuyInSpecialWeapons(100);
                break;
            case "MissionAmazonStrategy":
                botStrategy = new MissionAmazonStrategy(100, specialPaidWeaponId, requestedBetLevel, allowedUseDroppedSW);
                break;
            case "MaxBlastChampionsBotStrategy":
                botStrategy =  new MaxBlastChampionsBotStrategy(null, 167/*multiplayer = 1.01*/, 23050/*multiplayer = 3.9999*/, buyIn);
                break;
            case "SectorXStrategy":
                botStrategy = new SectorXStrategy(100, requestedBetLevel, requestedEnemyIds);
                break;
            default:
                botStrategy = new BurstShootingStrategyWithoutBuySW(100);
                break;
        }
        LOG.debug("botStrategy: {}, botStrategyString: {}", botStrategy, options.getBotStrategy());
        return botStrategy;
    }

    /*public void createMaxBlastBot(String id,long gameId, int bankId, String token, String nickname,
                                  String userName, Function<Void, Integer> shutdownCallback, IRoomBotStrategy botStrategy, ArgsParser options) {
        ManagedMaxBlastChampionsRoomBot bot = null;
        try{
            String maxBlastLaunchUrl = options.getUrl() +
                    "?bankId=" + options.getBankId() +
                    "&gameId=" + options.getGameId() +
                    "&buyIn=" +  options.getRequestedByInAmount()+
                    "&lang=" + "en" +
                    "&token=" + token +
                    "&homeUrl=homeUrl" +
                    "&pass=" + options.getPass();

            logAndPrintToConsole("createMaxBlastBot: Sending base request to " + maxBlastLaunchUrl + ", token: " + token);

            String response = new RestTemplate().getForObject(maxBlastLaunchUrl, String.class);

            BotParams params = new BotParams(extractWebSocketUrl(response).replace("mplobby", "mpunified"), extractSessionId(response), extractServerId(response));
            logAndPrintToConsole("createMaxBlastBot: botParams for BTG " + params);

            bot = new ManagedMaxBlastChampionsRoomBot(
                    null,
                    nickname,
                    userName,
                    options.getPass(),
                    id,
                    params.getSocketUrl(),
                    params.getServerId(),
                    bankId,
                    params.getSessionId(),
                    new GsonMessageSerializer(gson),
                    aVoid -> {
                        activeBots.decrementAndGet();
                        if (shutdownCallback != null) {
                            shutdownCallback.apply(null);
                        }
                        return 0;
                    },
                    unused -> {
                        activeBots.incrementAndGet();
                        return 0;
                    },
                    (int) gameId,
                    botStrategy,
                    (int) options.getRoomId(),
                    token
            );

            bot.setSelectedBuyIn(options.getRequestedByInAmount());
            bots.add(bot);
            bot.start();

        } catch (Exception e) {
            logAndPrintToConsole("createMaxBlastBot: failed create bot=" + id + "Exception:" + e.getMessage());
            LOG.error("createMaxBlastBot: failed create bot={}", id , e);
            if (bot != null) {
                if (bots.remove(bot)) {
                    activeBots.decrementAndGet();
                }
            }
        }
    }*/

    public UnifiedBot createUnifiedBot(String id, long gameId, int bankId, String launchUrl, String token, String nickname,
                                       Function<Void, Integer> shutdownCallback, IUnifiedBotStrategy botStrategy, long roomId) {
        UnifiedBot bot = null;
        try {
            BotParams params = createParamsForUnified(launchUrl + token);

            bot = new UnifiedBot(
                    nickname,
                    id,
                    params.getSocketUrl(),
                    (int) gameId,
                    bankId,
                    params.getServerId(),
                    params.getSessionId(),
                    new GsonMessageSerializer(gson),
                    aVoid -> {
                        activeBots.decrementAndGet();
                        if (shutdownCallback != null) {
                            shutdownCallback.apply(null);
                        }
                        return 0;
                    },
                    botStrategy,
                    unused -> {
                        activeBots.incrementAndGet();
                        return 0;
                    });

            setRoom(bot, roomId);

            bots.add(bot);

            bot.start();

        } catch (Exception e) {
            logAndPrintToConsole("createUnifiedBot: failed create bot=" + id + "Exception:" + e.getMessage());
            LOG.error("createUnifiedBot: failed create bot={}", id , e);
            if (bot != null) {
                if (bots.remove(bot)) {
                    activeBots.decrementAndGet();
                }
            }
        }
        return bot;
    }

    protected BotParams createParamsForUnified(String launchUrl) {
        LOG.debug("Sending request to {}", launchUrl);

        String response = getForObject(launchUrl);
        String socketUrl = extractWebSocketUrl(response).replace("mplobby", "mpunified");
        return new BotParams(socketUrl, extractSessionId(response), extractServerId(response));
    }

    public void createBtgBot(String id, String launchUrl, String token, String nickname,
                             Function<Void, Integer> shutdownCallback, IRoomBotStrategy botStrategy, ArgsParser options, long roomId) {
        LobbyBot bot = null;
        try {
            logAndPrintToConsole("createBtgBot: Sending base request to " + launchUrl + ", token: " + token);

            String responseBase = getForObject(launchUrl + token);

            String sessionId = extractSessionId(responseBase);
            logAndPrintToConsole("createBtgBot: sessionId from base request: " + sessionId + ", btgUrl: " + options.getUrl());
            BotParams params = new BotParams(extractWebSocketUrl(responseBase), sessionId, extractServerId(responseBase));
            logAndPrintToConsole("createBtgBot: botParams for BTG " + params);

            bot = new LobbyBot(nickname, id, params.getSocketUrl(), options.getGameId(), options.getBankId(), params.getServerId(),
                    params.getSessionId(), new GsonMessageSerializer(gson), aVoid -> {
                activeBots.decrementAndGet();
                if (shutdownCallback != null) {
                    shutdownCallback.apply(null);
                }
                return 0;
            }, botStrategy, unused -> {
                activeBots.incrementAndGet();
                return 0;
            });
            bot.setNeedTryRandomExitInWaitState(options.isNeedTryRandomExitInWaitState());
            setRoom(bot, roomId);
            bots.add(bot);
            bot.start();
        } catch (Exception e) {
            logAndPrintToConsole("createBtgBot: failed create bot=" + id + "Exception:" + e.getMessage());
            LOG.error("createBtgBot: failed create bot={}", id , e);
            if (bot != null) {
                if (bots.remove(bot)) {
                    activeBots.decrementAndGet();
                }
            }
        }
    }


    public void createBot(String id, long gameId, int bankId, String launchUrl, String token, String nickname,
                          Function<Void, Integer> shutdownCallback, IRoomBotStrategy botStrategy, long roomId) {
        LobbyBot bot = null;
        try {
            BotParams params = createParams(launchUrl + token);
            bot = new LobbyBot(nickname, id, params.getSocketUrl(), (int) gameId, bankId, params.getServerId(),
                    params.getSessionId(), new GsonMessageSerializer(gson), aVoid -> {
                activeBots.decrementAndGet();
                if (shutdownCallback != null) {
                    shutdownCallback.apply(null);
                }
                return 0;
            }, botStrategy, unused -> {
                activeBots.incrementAndGet();
                return 0;
            });
            setRoom(bot, roomId);
            bots.add(bot);
            bot.start();
        } catch (Exception e) {
            logAndPrintToConsole("createBot: failed create bot=" + id + "Exception:" + e.getMessage());
            LOG.error("createBot: failed create bot={}", id , e);
            if (bot != null) {
                if (bots.remove(bot)) {
                    activeBots.decrementAndGet();
                }
            }
        }
    }

    protected void waitForBotsShutdown() {
        Scanner scanner = new java.util.Scanner(System.in);
        while (activeBots.get() > 0) {
            try {
                Thread.sleep(1000L);
                if (activeBots.get() > 0 && scanner.hasNext()) {
                    String input = scanner.next();
                    if (input.contains("stats")) {
                        printStats();
                    }
                    if (input.contains("stop")) {
                        bots.forEach(IBot::stop);
                    }
                }
            } catch (InterruptedException e) {
                // Ignored
            }
        }
        LOG.debug("waitForBotsShutdown: activeBots={}", activeBots.get());
    }

    protected BotParams createParams(String launchUrl) {
        LOG.debug("Sending request to {}", launchUrl);
        String response = getForObject(launchUrl);
        return new BotParams(extractWebSocketUrl(response), extractSessionId(response), extractServerId(response));
    }

    private String extractWebSocketUrl(String response) {
        return extractQuotedStringAfterToken(response, "'websocket': ");
    }

    private int extractServerId(String response) {
        try {
            return Integer.parseInt(extractQuotedStringAfterToken(response, "'serverId': "));
        } catch (NumberFormatException e) {
            LOG.error(response);
            throw e;
        }
    }

    private String extractSessionId(String response) {
        return extractQuotedStringAfterToken(response, "'sessionId': ");
    }

    private String extractQuotedStringAfterToken(String source, String token) {
        int start = source.indexOf("'", source.indexOf(token) + token.length()) + 1;
        int end = source.indexOf("'", start);
        return source.substring(start, end);
    }

    private void printStats() {
        for (IBot bot : bots) {
            logAndPrintToConsole("Stats for Bot-" + bot.getId());
            logAndPrintToConsole("   " + bot.getStats());
        }
    }

    private void setRoom(ILobbyBot bot, long roomId) {
        if (roomId != -1) {
            bot.setRoomFromArgs(roomId);
            bot.setSpecificRoom(true);
        }
    }

    public String getToken(String username, String password, String googleAuthUrl, String tokenGenerationUrl) {
        JSONObject authPayload = new JSONObject();
        authPayload.put("email", username);
        authPayload.put("password", password);
        authPayload.put("returnSecureToken", true);

        String googleAuthResponse = sendPostRequest(googleAuthUrl, authPayload.toString());
        JSONObject googleAuthJson = parseJson(googleAuthResponse);
        String userId = (String) googleAuthJson.get("idToken");

        JSONObject tokenPayload = new JSONObject();
        tokenPayload.put("USERID", userId);

        String tokenResponse = sendPostRequest(tokenGenerationUrl, tokenPayload.toString());
        JSONObject tokenJson = parseJson(tokenResponse);
        JSONObject extSystem = (JSONObject) tokenJson.get("EXTSYSTEM");
        JSONObject response = (JSONObject) extSystem.get("RESPONSE");
        String token = (String) response.get("TOKEN");

        return token;
    }
    private String sendPostRequest(String url, String jsonInputString) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "curl/8.6.0");
        headers.set("Accept", "*/*");

        HttpEntity<String> entity = new HttpEntity<>(jsonInputString, headers);

        return new RestTemplate().postForObject(url, entity, String.class);
    }

    private JSONObject parseJson(String jsonString) {
        try {
            org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
            return (JSONObject) parser.parse(jsonString);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON string", e);
        }
    }

    private String getForObject(String launchUrl) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "curl/8.6.0");
        headers.set("Accept", "*/*");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> responseE = restTemplate.exchange(
                launchUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        String response = responseE.getBody();
        return response;
    }
}
