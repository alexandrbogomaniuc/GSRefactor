package com.betsoft.casino.bots.service;

import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IManagedLobbyBot;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.mqb.*;
import com.betsoft.casino.bots.strategies.*;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.bots.dto.BotsMap;
import com.betsoft.casino.mp.model.bots.dto.SimpleBot;
import com.betsoft.casino.mp.service.BotManagerService;
import com.betsoft.casino.mp.web.GsonFactory;
import com.betsoft.casino.mp.web.GsonMessageSerializer;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.kafka.dto.KafkaHandlerException;
import com.dgphoenix.casino.kafka.dto.bots.response.BotLogInResultDto;
import com.dgphoenix.casino.kafka.dto.bots.response.BotLogOutResultDto;
import com.dgphoenix.casino.kafka.dto.bots.response.BotStatusResponse;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

/**
 * User: flsh
 * Date: 07.07.2022.
 */
public class MQBBotServiceHandler {
    private static final Logger LOG = LogManager.getLogger(MQBBotServiceHandler.class);
    public static final String BOT_MAX_CRASH_ROCK_PERCENT = "BOT_MAX_CRASH_ROCK_PERCENT";
    public static final String BOT_MAX_CRASH_MEDIUM_PERCENT = "BOT_MAX_CRASH_MEDIUM_PERCENT";
    public static final String BOT_MAX_CRASH_AGGRESSIVE_PERCENT = "BOT_MAX_CRASH_AGGRESSIVE_PERCENT";
    public static long MMC_BankId = 6274L;
    public static long MQC_BankId = 6275L;
    public static final Set<Long> MQB_BANKS = new HashSet<>(Arrays.asList(MMC_BankId, MQC_BankId));
    protected Gson gson = GsonFactory.createGson();
    //key is botId
    private final ConcurrentMap<Long, IManagedLobbyBot> botsMap = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, ManagedMaxBlastChampionsPlayer>> roomsPlayers = new ConcurrentHashMap();
    private final IApiClient mqbApiClient;
    private final IApiClient fakeApiClient;
    private final ScriptEngine scriptEngine;
    private int botMaxCrashRockPercent;
    private int botMaxCrashMediumPercent;
    private int botMaxCrashAggressivePercent;

    private final int TIMER_PERIOD_MS = 60000; //
    private ReentrantLock lock = new ReentrantLock();// 30 Sec
    private ScheduledExecutorService executorService;

    public MQBBotServiceHandler(IApiClient mqbApiClient, IApiClient fakeApiClient) {
        this.mqbApiClient = mqbApiClient;
        this.fakeApiClient = fakeApiClient;
        this.scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
        this.initSystemVariables();
        this.initScheduler();
    }

    private void initScheduler() {
        LOG.debug("initScheduler: Starting timer with period {} ms", this.TIMER_PERIOD_MS);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this::timerOccurWrapper, 0, this.TIMER_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    private void timerOccurWrapper() {
        if (lock.tryLock()) {
            try {
                LOG.debug("timerOccurWrapper: locked.");
                timerOccur();
            } finally {
                lock.unlock();
                LOG.debug("timerOccurWrapper: unlock.");
            }
        } else {
            LOG.warn("timerOccurWrapper: Previous timerOccur is still running. Skipping this execution.");
        }
    }

    public void timerOccur() {
        LOG.info("timerOccur: Timer occurred.");
        long timeBegin = System.currentTimeMillis();
        try {
            if (lock.tryLock()) {
                LOG.debug("timerOccur: locked.");

                this.logOutExpiredBots();

            } else {
                LOG.warn("timerOccur: can't lock, Previous timerOccur is still running. Skipping this execution.");
            }
        } catch (Exception e) {
            LOG.error("timerOccur: Error exception", e);
        } finally {
            lock.unlock();
            LOG.debug("timerOccur: unlock, took {} ms", System.currentTimeMillis() - timeBegin);
        }
    }

    private void initSystemVariables() { 
        try{
            botMaxCrashRockPercent = 25;
            String botMaxCrashRockPercentProperty = System.getProperty(BOT_MAX_CRASH_ROCK_PERCENT);
            if (!StringUtils.isTrimmedEmpty(botMaxCrashRockPercentProperty)) {
                botMaxCrashRockPercent = Integer.parseInt(botMaxCrashRockPercentProperty);
            }
        }catch (Exception e)
        {}

        try{
            botMaxCrashMediumPercent = 43;
            String botMaxCrashMediumPercentProperty = System.getProperty(BOT_MAX_CRASH_MEDIUM_PERCENT);
            if (!StringUtils.isTrimmedEmpty(botMaxCrashMediumPercentProperty)) {
                botMaxCrashMediumPercent = Integer.parseInt(botMaxCrashMediumPercentProperty);
            }
        }catch (Exception e)
        {}

        try{
            botMaxCrashAggressivePercent = 100;
            String botMaxCrashAggressivePercentProperty = System.getProperty(BOT_MAX_CRASH_AGGRESSIVE_PERCENT);
            if (!StringUtils.isTrimmedEmpty(botMaxCrashAggressivePercentProperty)) {
                botMaxCrashAggressivePercent = Integer.parseInt(botMaxCrashAggressivePercentProperty);
            }
        }catch (Exception e)
        {}
    }

    public Map<String, ManagedMaxBlastChampionsPlayer> getPlayers(long roomId) {
        Map<String, ManagedMaxBlastChampionsPlayer> players = roomsPlayers.get(roomId);
        if(players == null) {
            players = new ConcurrentHashMap();
            roomsPlayers.put(roomId, players);
        }
        return players;
    }

    public boolean isInBotsMap(String nickname) {
        Collection<IManagedLobbyBot> bots = botsMap.values();
        if(bots != null) {
            boolean isInBotsMapResult = bots.stream()
                    .anyMatch(b ->
                            b.getNickname().equals(nickname)
                    );

            return isInBotsMapResult;
        }

        return false;
    }

    public IApiClient getCorrectApiClient(long bankId) {
        return MQB_BANKS.contains(bankId) ? mqbApiClient : fakeApiClient;
    }

    public BotStatusResponse getStatusForNewBot(String userName, String password, String botNickName, long bankId, long gameId) {
        LOG.debug("getStatusForNewBot  request from MP: userName={}, password=******, bankId={}, gameId={}, botNickname={}", userName, /*password,*/
                bankId, gameId, botNickName);

        BotStatusResponse status;
        try {
            if (!MQB_BANKS.contains(bankId) && !fakeApiClient.isFake()) {
                throw new CommonException("Fake stub service not configured!");
            }

            IApiClient correctApi = getCorrectApiClient(bankId);
            LoginResponse loginResponse = correctApi.login(userName, password, bankId, gameId, 0, getCurrency(bankId));
            LOG.debug("getStatusForNewBot: userName={}, loginResponse={}", userName, loginResponse);

            if (!loginResponse.isSuccess()) {
                throw new CommonException("Call to mqbApiClient failed");
            }

            String token = loginResponse.getToken();
            long mmcBalance = 0;
            long mqcBalance = 0;

            if(bankId == MMC_BankId) {
                mmcBalance = loginResponse.getBalance();
                GetBalancesResponse balancesResponse  = correctApi.getBalance(loginResponse.getLocalId(), MQC_BankId);
                if (balancesResponse.isSuccess()) {
                    mqcBalance = balancesResponse.getBalance();
                }
            } else if(bankId == MQC_BankId) {
                mqcBalance = loginResponse.getBalance();
                GetBalancesResponse balancesResponse  = correctApi.getBalance(loginResponse.getLocalId(), MMC_BankId);
                if (balancesResponse.isSuccess()) {
                    mmcBalance = balancesResponse.getBalance();
                }
            }

            status = new BotStatusResponse(BotStatuses.OK.getStatusCode(), mmcBalance, mqcBalance, true, 0, "");
            LOG.debug("getStatusForNewBot: userName={}, status={}", userName, status);
            correctApi.logout(userName, token);
        } catch (Exception e) {
            LOG.error("getStatusForNewBot failed", e);
            throw new KafkaHandlerException(-1, e.getMessage());
        }
        return status;
    }

    private BotLogInResultDto logOutOldBotIfExistsInRoom(long botId, long roomId) {

        IManagedLobbyBot lobbyBot = botsMap.values().stream()
                .filter(b -> (b.getRoomId() == roomId || String.valueOf(botId).equals(b.getId())))
                .findFirst()
                .orElse(null);
        LOG.debug("logOutOldBotIfExistsInRoom: lobbyBot={} in room={}", lobbyBot, roomId);

        if (lobbyBot != null) {
            IRoomBot roomBot = lobbyBot.getRoomBot();
            String oldBotNickname = roomBot != null ? roomBot.getNickname() : "Unknown lobbyBot";
            long oldBotId = Long.parseLong(lobbyBot.getId());
            LOG.warn("logOutOldBotIfExistsInRoom: Old lobbyBot/roomBot was found in room={}, oldBotNickname={}, oldBotId={}",
                    roomId, oldBotNickname, oldBotId);

            if (roomBot == null || roomBot.getSeatId() == -1) {
                LOG.debug("logOutOldBotIfExistsInRoom, lobbyBot found without roomBot or without seatId in the roomBot, need to stop " +
                        "lobbyBot and/or roomBot, lobbyBot={}, roomBot={}", lobbyBot, roomBot);

                try {
                    LOG.debug("logOutOldBotIfExistsInRoom: try stop lobbyBot={}", lobbyBot);
                    lobbyBot.stop();
                } catch (Exception e) {
                    LOG.error("logOutOldBotIfExistsInRoom: oldBotId={}, oldBotNickname={} cannot stop lobby lobbyBot", oldBotId, oldBotNickname, e);
                }

                if (roomBot != null) {
                    try {
                        LOG.debug("logOutOldBotIfExistsInRoom: try stop roomBot={}", roomBot);
                        roomBot.stop();
                    } catch (Exception e) {
                        LOG.error("logOutOldBotIfExistsInRoom: oldBotId={}, oldBotNickname={} cannot stop room lobbyBot", oldBotId, oldBotNickname, e);
                    }
                }

                LOG.debug("logOutOldBotIfExistsInRoom: remove lobbyBot botId={} from botsMap", oldBotId);
                botsMap.remove(oldBotId);

            } else {
                try {
                    LOG.debug("logOutOldBotIfExistsInRoom: lobbyBot was found with roomBot with seatId={}, oldBotId={}, oldBotNickname={}, " +
                            "lobbyBot={}, roomBot={}, try logOut lobbyBot", roomBot.getSeatId(), oldBotId, oldBotNickname, lobbyBot, roomBot);
                    logOut(oldBotId, lobbyBot.getSessionId(), oldBotNickname, roomId);
                } catch (Exception e) {
                    LOG.error("sitOutOldBotIfExistsInRoom: cannot sitOut old bot, return sitIn error", e);
                    BotLogInResultDto result = new BotLogInResultDto();
                    result.setSuccess(false);
                    result.setReasonPhrases("Found old bot='" + oldBotNickname + "' in room, sitOut failed, reason=" + e.getMessage());
                    return result;
                }
            }
        }
        return null;
    }

    private String adjustStartGameUrlForTestingEnv(String startGameUrl, String openRoomWSUrl, IApiClient correctApi) throws URISyntaxException {

        String openRoomHost = "";
        try {
            openRoomHost =  (new URI(openRoomWSUrl)).getHost();
        } catch (Exception e) {
            LOG.error("adjustStartGameUrlForTestingEnv:  error to get openRoomHost value from openRoomWSUrl '{}': {}", openRoomWSUrl, e);
        }
        LOG.debug("adjustStartGameUrlForTestingEnv: openRoomHost={}", openRoomHost);

        if (openRoomHost.endsWith("mp.local") || openRoomHost.endsWith("mp.local.com") || openRoomHost.endsWith(".mydomain")) { //hack for local/dev deploy
            startGameUrl = startGameUrl.replace("https://mqtech-beta.discreetgaming.com", "http://default-local.mydomain");
            startGameUrl = startGameUrl.replace("https://default-test.maxquest.com", "http://default-local.mydomain");
            startGameUrl = startGameUrl.replace("https://default.mdtest.io", "http://default-local.mydomain");
            startGameUrl = startGameUrl.replace("https://default.mdbase.io", "http://default-local.mydomain");
        } else if (openRoomHost.endsWith(".maxquest.com")) { //hack for testing environment deploy
            if (correctApi instanceof MQBApiClient) {
                String mqblogeBotApiUrl = ((MQBApiClient) correctApi).getMqbSiteBotApiUrl();
                URI mqblogeBotApiUri = new URI(mqblogeBotApiUrl);
                String mqblogeHost = mqblogeBotApiUri.getHost();
                LOG.debug("adjustStartGameUrlForTestingEnv: mqblogeHost={}", mqblogeHost);
                if (mqblogeHost != null && mqblogeHost.endsWith("dev.maxquestgame.com")) {
                    startGameUrl = startGameUrl.replace("https://mqtech-beta.discreetgaming.com", "http://default-test.maxquest.com");
                }
            }
        }

        return startGameUrl;
    }

    public BotLogInResultDto logIn(long botId, String userName, String password, long bankId, long gameId, long buyIn, String botNickname,
                                 long roomId, String lang, int gameServerId, String enterLobbyWsUrl, String openRoomWSUrl, long expiresAt,
                                 double shootsRate, double bulletsRate) {

        LOG.debug("logIn: request from MP: botId={}, userName={}, password=******, bankId={}, gameId={}, buyIn={}, botNickname={}, roomId={}, lang={}, " +
                        "gameServerId={}, enterLobbyWsUrl={}, openRoomWSUrl={}, expiresAt={}, shootsRate={}, bulletsRate={}",
                botId, userName, /*password,*/ bankId, gameId, buyIn, botNickname, roomId, lang, gameServerId,
                enterLobbyWsUrl, openRoomWSUrl, expiresAt, shootsRate, bulletsRate);

        BotLogInResultDto result = new BotLogInResultDto();

        try {
            if (!MQB_BANKS.contains(bankId) && !fakeApiClient.isFake()) {
                throw new CommonException("Fake stub service not configured!");
            }

            IApiClient correctApi = getCorrectApiClient(bankId);
            LoginResponse loginResponse = correctApi.login(userName, password, bankId, gameId, buyIn, getCurrency(bankId));
            LOG.debug("logIn: roomId={}, botId={}, username={}, loginResponse={}", roomId, botId, userName, loginResponse);

            if (!loginResponse.isSuccess()) {
                throw new CommonException("Call to mqbApiClient failed");
            }

            String startGameUrl = loginResponse.getStartGameUrl();
            startGameUrl = this.adjustStartGameUrlForTestingEnv(startGameUrl, openRoomWSUrl, correctApi);

            LOG.debug("logIn: roomId={}, resulting startGameUrl={}", roomId, startGameUrl);

            IRoomBotStrategy roomBotStrategy = getStrategy(gameId, buyIn, roomId);

            IManagedLobbyBot bot = createBot(botId, startGameUrl, botNickname, userName, password,
                    loginResponse.getLocalId(), (int) gameId, (int) bankId, roomId,
                    enterLobbyWsUrl, openRoomWSUrl, gameServerId, null, roomBotStrategy,
                    loginResponse.getToken(), expiresAt, shootsRate, bulletsRate);

            bot.setSelectedBuyIn(buyIn);

            if (roomBotStrategy != null) {
                roomBotStrategy.setLobbyBot(bot);
            }

            LOG.debug("logIn: roomId={}, botId={}, nickname={}, roomBotStrategy={}",
                    roomId, bot.getId(), bot.getNickname(), roomBotStrategy);

            result = new BotLogInResultDto();
            result.setSuccess(true);
            if(bankId == MMC_BankId) {
                result.setMmcBalance(loginResponse.getBalance());
                GetBalancesResponse balancesResponse  = correctApi.getBalance(loginResponse.getLocalId(), MQC_BankId);
                if (balancesResponse.isSuccess()) {
                    result.setMqcBalance(balancesResponse.getBalance());
                }
            } else if(bankId == MQC_BankId) {
                result.setMqcBalance(loginResponse.getBalance());
                GetBalancesResponse balancesResponse  = correctApi.getBalance(loginResponse.getLocalId(), MMC_BankId);
                if (balancesResponse.isSuccess()) {
                    result.setMmcBalance(balancesResponse.getBalance());
                }
            }
            result.setSessionId(bot.getSessionId());
            LOG.debug("logIn: userName={}, result={}", userName, result);
        } catch (Exception e) {
            LOG.error("logIn: error: " + e.getMessage());
            result = new BotLogInResultDto();
            result.setSuccess(false);
            result.setReasonPhrases(e.getMessage());

        }

        return result;
    }

    public BotStatusResponse getStatus(long botId, String sessionId, String botNickName, long roomId) {
        LOG.debug("getStatus request from MP: botId={}, sessionId={}, botNickName={}, roomId={}", botId, sessionId, botNickName, roomId);
        IManagedLobbyBot bot = botsMap.get(botId);
        BotStatusResponse status = new BotStatusResponse(BotStatuses.NOT_FOUND.getStatusCode(), 0, 0, false, -1, BotStatuses.NOT_FOUND.getDescription());

        if (bot instanceof ManagedLobbyBot || bot instanceof ManagedMaxBlastChampionsRoomBot) {
            try {
                String externalId = null;
                if(bot instanceof ManagedLobbyBot) {
                    externalId = ((ManagedLobbyBot)bot).getExternalId();
                } else  {
                    externalId = ((ManagedMaxBlastChampionsRoomBot)bot).getExternalId();
                }

                IApiClient correctApi = MQB_BANKS.contains((long) bot.getBankId()) ? mqbApiClient : fakeApiClient;
                GetBalancesResponse mmcBalance = correctApi.getBalance(externalId, MMC_BankId);
                GetBalancesResponse mqcBalance = correctApi.getBalance(externalId, MMC_BankId);
                if(mmcBalance.isSuccess() && mqcBalance.isSuccess()) {

                    status = new BotStatusResponse(bot.getStatus().getStatusCode(),
                            mmcBalance.getBalance(), mqcBalance.getBalance(), true, 0,
                            bot.getStatus().getDescription());
                }
            } catch (CommonException e) {
                LOG.error("getBalances call failed", e);
                status = new BotStatusResponse(bot.getStatus().getStatusCode(), 0, 0, false, -1, "getBalances failed: " + e.getMessage());
            }
        }

        return status;
    }

    public BotStatusResponse confirmNextRoundBuyIn(long botId, String sessionId, String botNickName, long roomId, long roundId) {
        LOG.debug("getStatus request from MP: botId={}, sessionIdlaunchUrl={}, botNickName={}, roomId={}, roundId={}", botId, sessionId, botNickName, roomId, roundId);
        BotStatusResponse status = getStatus(botId, sessionId, botNickName, roomId);
        if (status.isSuccess()) {
            try {
                IManagedLobbyBot bot = botsMap.get(botId);
                bot.confirmNextRoundPlay(roundId);
            } catch (Exception e) {
                LOG.error("confirmNextRoundPlay failed", e);
                status.setSuccess(false);
                status.setStatusCode(-1);
                status.setReasonPhrases("confirmNextRoundPlay failed: " + e.getMessage());
            }
        }
        return status;
    }

    private List<ManagedMaxBlastChampionsRoomBot> getManagedMaxBlastChampionsRoomBotsFromMap(long roomId) {
        //search all bots from the room with id 'roomId'
        //and if bots are of the type ManagedMaxBlastChampionsRoomBot
        //get all bots of the type ManagedMaxBlastChampionsRoomBot into separate list
        List<ManagedMaxBlastChampionsRoomBot> managedMaxBlastChampionsRoomBots = botsMap.values().stream()
                .filter(managedLB ->
                        (managedLB.getRoomId() == roomId) && managedLB instanceof ManagedMaxBlastChampionsRoomBot)
                .map(managedLB -> (ManagedMaxBlastChampionsRoomBot) managedLB)
                .collect(Collectors.toList());

        return managedMaxBlastChampionsRoomBots;
    }

    private List<ManagedMaxBlastChampionsRoomBot> filterManagedMaxBlastChampionsRoomBotsForStrategy
            (List<ManagedMaxBlastChampionsRoomBot> managedMaxBlastChampionsRoomBots, Class<?> strategyClass) {
        //search all bots from the list for a specified strategy class
        List<ManagedMaxBlastChampionsRoomBot> managedMaxBlastChampionsRoomBotsForClass = managedMaxBlastChampionsRoomBots.stream()
                .filter(managedMBCRBot -> strategyClass.isInstance(managedMBCRBot.getStrategy()))
                .collect(Collectors.toList());

        return managedMaxBlastChampionsRoomBotsForClass;
    }

    private long fetchBotIdFromMaxCrashGameRoomForlogOut(long roomId) {
        long botId = -1;

        //get 'gameId' based on 'roomId' if gameId is BG_MAXCRASHGAME,
        //find most strong bot in the room with id 'roomId'
        List<ManagedMaxBlastChampionsRoomBot> bots =
                getManagedMaxBlastChampionsRoomBotsFromMap(roomId);

        if (bots.isEmpty()) {
            LOG.debug("fetchBotIdFromMaxCrashGameRoomForlogOut: No ManagedMaxBlastChampionsRoomBots found in the room {}", roomId);
        } else { //ManagedMaxBlastChampionsRoomBots from the room with id 'roomId' found
            LOG.debug("fetchBotIdFromMaxCrashGameRoomForlogOut: managedMaxBlastChampionsRoomBots bot {} found in the room {}", bots, roomId);

            //get 'gameId' used by ManagedMaxBlastChampionsRoomBot
            //Note: it should be BG_MAXCRASHGAME
            int gameId = bots.get(0).getGameId();

            //If the 'gameId' is of the type BG_MAXCRASHGAME
            //find 'botId' for most strong bot in the room, priority to search is: Aggressive, Medium, Rock
            if (gameId == GameType.BG_MAXCRASHGAME.getGameId()) {

                //try to find aggressive bot and get its botId if it is found.
                List<ManagedMaxBlastChampionsRoomBot> aggressiveBots =
                        filterManagedMaxBlastChampionsRoomBotsForStrategy(bots, MaxBlastChampionsBotAggressiveStrategy.class);

                if (!aggressiveBots.isEmpty()) {
                    LOG.debug("fetchBotIdFromMaxCrashGameRoomForlogOut: aggressiveBot {} found in the room {}", aggressiveBots.get(0).getId(), roomId);
                    try {
                        botId = Long.parseLong(aggressiveBots.get(0).getId());
                    } catch (Exception exception) {
                        LOG.error("fetchBotIdFromMaxCrashGameRoomForlogOut: failed to convert botId={} to long", aggressiveBots.get(0).getId(), exception);
                    }
                } else {
                    LOG.debug("fetchBotIdFromMaxCrashGameRoomForlogOut: No aggressiveBots found in the room {}", roomId);
                }

                //if no aggressive botId found try to find medium bot and get its botId if it is found.
                if (botId == -1) {
                    List<ManagedMaxBlastChampionsRoomBot> mediumBots =
                            filterManagedMaxBlastChampionsRoomBotsForStrategy(bots, MaxBlastChampionsBotMediumStrategy.class);

                    if (!mediumBots.isEmpty()) {
                        LOG.debug("fetchBotIdFromMaxCrashGameRoomForlogOut: mediumBot {} found in the room {}", mediumBots.get(0).getId(), roomId);
                        try {
                            botId = Long.parseLong(mediumBots.get(0).getId());
                        } catch (Exception exception) {
                            LOG.error("fetchBotIdFromMaxCrashGameRoomForlogOut: failed to convert botId={} to long", mediumBots.get(0).getId(), exception);
                        }
                    } else {
                        LOG.debug("fetchBotIdFromMaxCrashGameRoomForlogOut: No mediumBots found in the room {}", roomId);
                    }
                }

                //if no medium botId found try to find rock bot and get its botId if it is found.
                if (botId == -1) {

                    List<ManagedMaxBlastChampionsRoomBot> rockBots =
                            filterManagedMaxBlastChampionsRoomBotsForStrategy(bots, MaxBlastChampionsBotRockStrategy.class);

                    if (!rockBots.isEmpty()) {
                        LOG.debug("fetchBotIdFromMaxCrashGameRoomForlogOut: rockBot {} found in the room {}", rockBots.get(0).getId(), roomId);
                        try {
                            botId = Long.parseLong(rockBots.get(0).getId());
                        } catch (Exception exception) {
                            LOG.error("fetchBotIdFromMaxCrashGameRoomForlogOut: failed tp convert botId={} to long", rockBots.get(0).getId(), exception);
                        }
                    } else {
                        LOG.debug("fetchBotIdFromMaxCrashGameRoomForlogOut: No rockBots found in the room {}", roomId);
                    }
                }
            }
        }

        return botId;
    }

    public BotLogOutResultDto logOut(long botId, String sessionId, String botNickName, long roomId) {
        long now = System.currentTimeMillis();
        LOG.debug("logOut: request from MP: botId={}, sessionId={}, botNickName={}, roomId={}", botId, sessionId, botNickName, roomId);

        //MaxCrash service sends botId == -1 to let us decide which bot to logOut
        if (botId == -1) {
            botId = fetchBotIdFromMaxCrashGameRoomForlogOut(roomId);
        }

        IManagedLobbyBot bot = botsMap.get(botId);
        BotLogOutResultDto result;

        if (bot == null) {
            result = new BotLogOutResultDto(false, BotStatuses.NOT_FOUND.getStatusCode(), BotStatuses.NOT_FOUND.getDescription());
        } else {

            try {
                bot.sitOut();
            } catch (Exception e) {
                LOG.error("logOut failed sitOut, botId={}", botId, e);
            }

            try{
                if(bot instanceof ManagedLobbyBot ) {
                    LOG.debug("logOut: botId={}, botNickName={}, ManagedLobbyBot set expiresAt={}",
                            botId, botNickName, toHumanReadableFormat(now));
                    ((ManagedLobbyBot) bot).setExpiresAt(now);
                    IRoomBot roomBot = bot.getRoomBot();
                    if(roomBot instanceof  ManagedBattleGroundRoomBot) {
                        LOG.debug("logOut: botId={}, botNickName={}, ManagedBattleGroundRoomBot set expiresAt={}",
                                botId, botNickName, toHumanReadableFormat(now));
                        ((ManagedBattleGroundRoomBot) roomBot).setExpiresAt(now);
                    }
                }

                bot.stop();
                LOG.debug("logOut: botId={}, botNickName={}, roomId=-1 remove from botsMap", botId, botNickName);
                this.removeBot(botId, botNickName, -1);

                LOG.debug("logOut: botId={} removed from botsMap:{}", botId, botsMap.keySet().toArray());
                result = new BotLogOutResultDto(true, BotStatuses.OK.getStatusCode(), BotStatuses.OK.getDescription());
            } catch (Exception e) {
                LOG.error("logOut failed, botId={}", botId, e);
                result = new BotLogOutResultDto(false, -1, "Unknown error:" + e.getMessage());
            }

            try {
                long bankId = bot.getBankId();
                IApiClient correctApi = getCorrectApiClient(bankId);
                correctApi.logout(bot.getNickname(), bot.getToken());
            } catch (CommonException e) {
                LOG.error("Logout call failed", e);
            }
        }
        return result;
    }

    public void removeBot(long botId, String botNickName, long roomId) {
        LOG.debug("removeBot: botId={}, botNickName={}, roomId={}", botId, botNickName, roomId);
        try {
            IManagedLobbyBot bot = botsMap.get(botId);
            if (bot == null) {
                LOG.debug("removeBot: bot not found by id, try find by botNickName, botId={}, botNickName={}", botId, botNickName);
                for (IManagedLobbyBot currentBot : botsMap.values()) {
                    if (currentBot.getNickname().equals(botNickName)) {
                        bot = currentBot;
                        break;
                    }
                }
            }
            if (bot == null) {
                LOG.debug("removeBot: bot not found, botId={}, botNickName={}", botId, botNickName);
                return;
            }
            long bankId = bot.getBankId();
            if(roomId > 0) { //if roomId == -1 do not stop the bot as it is already stopped
                bot.stop();
            }
            botsMap.remove(Long.valueOf(bot.getId()));
            try {
                IApiClient correctApi = getCorrectApiClient(bankId);
                correctApi.logout(bot.getNickname(), bot.getToken());
            } catch (CommonException e) {
                LOG.error("removeBot logout call failed", e);
            }
        } catch (Exception e) {
            LOG.error("removeBot failed", e);
        }
    }

    public String getDetailBotInfo(long botId, String botNickName) {
        LOG.debug("getDetailBotInfo  request from MP: botId={}, botNickName={}", botId, botNickName);
        IManagedLobbyBot bot = botsMap.get(botId);
        if (bot == null) {
            LOG.debug("getDetailBotInfo: bot not found by id, try find by botNickName, botId={}, botNickName={}", botId, botNickName);
            for (IManagedLobbyBot currentBot : botsMap.values()) {
                if (currentBot.getNickname().equals(botNickName)) {
                    bot = currentBot;
                    break;
                }
            }
        }
        StringBuilder res = new StringBuilder();
        if (bot != null) {
            res.append(bot.getStatus().getDescription()).append("|");
            res.append(bot.getRoomBot().getStats().toShortString());
        }
        return res.toString();
    }

    private SimpleBot convertManagedBattleGroundRoomBotToSimpleBot(ManagedBattleGroundRoomBot managedBattleGroundRoomBot) {
        if(managedBattleGroundRoomBot == null) {
            LOG.debug("convertManagedBattleGroundRoomBotToTBot: managedLobbyBot is null, skip");
            return null;
        }

        LOG.debug("convertManagedBattleGroundRoomBotToTBot: botId={}, botNickName={}",
                managedBattleGroundRoomBot.getId(), managedBattleGroundRoomBot.getNickname());

        SimpleBot tRoomBot = new SimpleBot();
        tRoomBot.setId(managedBattleGroundRoomBot.getId());
        tRoomBot.setNickname(managedBattleGroundRoomBot.getNickname());
        tRoomBot.setRoomId(managedBattleGroundRoomBot.getRoomId());
        tRoomBot.setBankId(managedBattleGroundRoomBot.getBankId());
        tRoomBot.setServerId(managedBattleGroundRoomBot.getServerId());
        tRoomBot.setSid(managedBattleGroundRoomBot.getSessionId());
        tRoomBot.setUrl(managedBattleGroundRoomBot.getUrl());

        BotState botState = managedBattleGroundRoomBot.getState();
        if(botState != null) {
            tRoomBot.setBotState(botState.toTBotState());
        }

        tRoomBot.setExpiresAt(managedBattleGroundRoomBot.getExpiresAt());

        return tRoomBot;
    }

    private SimpleBot convertIManagedLobbyBotToTBot(IManagedLobbyBot managedLobbyBot) {

        if(managedLobbyBot == null) {
            LOG.debug("convertLobbyBotToTBot: managedLobbyBot is null, skip");
            return null;
        }

        LOG.debug("convertLobbyBotToTBot: botId={}, botNickName={}",
                managedLobbyBot.getId(), managedLobbyBot.getNickname());

        SimpleBot tLobbyBot = new SimpleBot();

        tLobbyBot.setId(managedLobbyBot.getId());
        tLobbyBot.setNickname(managedLobbyBot.getNickname());
        tLobbyBot.setRoomId(managedLobbyBot.getRoomId());
        tLobbyBot.setBankId(managedLobbyBot.getBankId());
        tLobbyBot.setServerId(managedLobbyBot.getServerId());
        tLobbyBot.setToken(managedLobbyBot.getToken());
        tLobbyBot.setSid(managedLobbyBot.getSessionId());
        tLobbyBot.setUrl(managedLobbyBot.getUrl());

        int gameId = 0;

        if(managedLobbyBot instanceof ManagedMaxBlastChampionsRoomBot) {
            BotState botState = ((ManagedMaxBlastChampionsRoomBot)managedLobbyBot).getState();
            if(botState != null) {
                tLobbyBot.setBotState(botState.toTBotState());
            }

            gameId = ((ManagedMaxBlastChampionsRoomBot)managedLobbyBot).getGameId();
        }

        if(managedLobbyBot instanceof ManagedLobbyBot) {
            long expiresAt = ((ManagedLobbyBot)managedLobbyBot).getExpiresAt();
            tLobbyBot.setExpiresAt(expiresAt);

            gameId = ((ManagedLobbyBot)managedLobbyBot).getGameId();

            ManagedBattleGroundRoomBot managedBattleGroundRoomBot = ((ManagedLobbyBot)managedLobbyBot).getManagedRoomBot();
            if(managedBattleGroundRoomBot != null) {
                SimpleBot roomBot = convertManagedBattleGroundRoomBotToSimpleBot(managedBattleGroundRoomBot);
                tLobbyBot.setRoomBot(roomBot);
            }
        }

        tLobbyBot.setGameId(gameId);

        return tLobbyBot;
    }

    public BotsMap getBotsMap() {

        LOG.debug("getBotsMap: called");

        BotsMap tBotsMap = new BotsMap();

        List<SimpleBot> tBotsList = new ArrayList<>();

        if(!botsMap.isEmpty()) {
           for(Map.Entry<Long, IManagedLobbyBot> entry : botsMap.entrySet()) {
               SimpleBot tBot = convertIManagedLobbyBotToTBot(entry.getValue());
               if (tBot != null) {
                   tBotsList.add(tBot);
               }
           }
        }

        tBotsMap.setBotsMap(tBotsList);
        tBotsMap.setSuccess(true);

        LOG.debug("getBotsMap: tBotsMap={}", tBotsMap);

        return tBotsMap;
    }

    private String getCurrency(long bankId) throws CommonException {
        if (bankId == 6274) {
            return "MMC";
        } else if (bankId == 6275) {
            return "MQC";
        } else if (bankId == 271) {
            return "EUR";
        } else {
            throw new CommonException("Unsupported bankId");
        }
    }

    private void logOutExpiredBots() {
        long now = System.currentTimeMillis();
        LOG.debug("logOutExpiredBots: starts={}", toHumanReadableFormat(now));

        long tresholdMs =
                Duration.ofMinutes(
                                BotManagerService.MAX_BOTS_LIMIT_IN_SHOOTING_ROOM_THRESHOLD_MINUTES + BotManagerService.MAX_MANAGED_BOT_EXPIRATION_MINUTES + 1)
                        .toMillis();

        if(!botsMap.isEmpty()) {
            List<IManagedLobbyBot> expiredBots = botsMap.values().stream()
                    .filter(
                            bot -> bot.getExpiresAt() + tresholdMs < now
                    )
                    .collect(Collectors.toList());

            LOG.debug("logOutExpiredBots: there are {} expiredBots expiration is  older {} ms", expiredBots.size(), tresholdMs);

            for(IManagedLobbyBot expiredBot : expiredBots) {
                try {
                    LOG.debug("logOutExpiredBots: try to logOut expiredBot={}", expiredBot);
                    this.logOut(Long.parseLong(expiredBot.getId()), null, expiredBot.getNickname(), expiredBot.getRoomId());
                } catch (Exception e) {
                    LOG.error("logOutExpiredBots: roomId={}, error to logOut expired bot={}", expiredBot, e);
                }
            }
        }
    }

    public IManagedLobbyBot createBot(long botId, String launchUrl, String nickname, String userName, String password,
                                      String externalId, int gameId, int bankId, long roomId, String enterLobbyWsUrl,
                               String openRoomWSUrl, int gameServerId, Function<Void, Integer> shutdownCallback, IRoomBotStrategy botStrategy,
                               String token, long expiresAt, double shootsRate, double bulletsRate) throws CommonException {
        IManagedLobbyBot bot = null;
        try {
            LOG.debug("createBot: roomId={}, botId={}, nickname={}, Sending request to {}, token={}, openRoomWSUrl={}, " +
                            "expiresAt={}, shootsRate={}, bulletsRate={}",
                    roomId, botId, nickname, launchUrl, token, openRoomWSUrl, toHumanReadableFormat(expiresAt), shootsRate, bulletsRate);

            String response = getForObject(launchUrl);

            String sessionId = extractSessionId(response);
            String parsedLobbySocketUrl = StringUtils.isTrimmedEmpty(enterLobbyWsUrl) ? extractWebSocketUrl(response) : enterLobbyWsUrl;
            int extractedServerId = extractServerId(response);
            LOG.debug("createBot: roomId={}, botId={}, nickname={}, with prams: sessionId={}, parsedLobbySocketUrl={}, extractedServerId={}",
                    roomId, botId, nickname, sessionId, parsedLobbySocketUrl, extractedServerId);
            bot = createLobbyBot(gameId, nickname, userName, password, externalId, String.valueOf(botId), openRoomWSUrl,
                    gameServerId, bankId, sessionId, shutdownCallback, botStrategy, roomId, token, parsedLobbySocketUrl, expiresAt, shootsRate, bulletsRate);

            botsMap.put(botId, bot);
            LOG.debug("createBot: roomId={}, botId={}, nickname={}, bot={} added to botsMap, botsMap.size()={}, botsMap: {}",
                    roomId, botId, nickname, bot, botsMap.size(), botsMap.keySet().toArray());
            bot.start();

        } catch (Exception e) {
            LOG.error("createBot: Cannot create bot error, id={}", botId, e);
            if (bot != null) {
                botsMap.remove(botId);
            }
            throw new CommonException(e);
        }

        return bot;
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

    private IRoomBotStrategy getStrategy(long gameId, long buyInAmount, long roomId) {

        if (GameType.BG_DRAGONSTONE.getGameId() == gameId) {

            return new NaturalBattleGroundDragonStoneStrategy(100, 1, buyInAmount);

        } else if (GameType.BG_MISSION_AMAZON.getGameId() == gameId) {

            return new NaturalBattleGroundMissionAmazonStrategy(100, 1, buyInAmount);

        } else if (GameType.BG_SECTOR_X.getGameId() == gameId) {

            return new NaturalBattleGroundSectorXStrategy(100, 1, buyInAmount,
                    String.valueOf(NaturalBattleGroundSectorXStrategy.ENEMY_TYPE_ID_SECTORX_CONCEALED_COINS));

        } else if (GameType.BG_MAXCRASHGAME.getGameId() == gameId) {

            MaxBlastChampionsBotStrategy maxBlastStrategy = null;
            //search for existing bots with specific strategies in the room with id 'roomId'
            List<ManagedMaxBlastChampionsRoomBot> managedMaxBlastChampionsRoomBots = getManagedMaxBlastChampionsRoomBotsFromMap(roomId);

            //try to find 'rock strategy' bots
            List<ManagedMaxBlastChampionsRoomBot> rockBots
                    = this.filterManagedMaxBlastChampionsRoomBotsForStrategy
                    (managedMaxBlastChampionsRoomBots, MaxBlastChampionsBotRockStrategy.class);

            //if no 'rock strategy' bots found, create 'rock strategy' for the new bot
            if(rockBots.isEmpty()) {
                // from 3000ms=3sec (multiplayer = 1.197) to the 25% of the maximum seconds allowed MAX_CRASH__MILLISECONDS (8000 ms) (multiplayer = 1.62)
                maxBlastStrategy =
                        new MaxBlastChampionsBotRockStrategy(this.scriptEngine, buyInAmount, 3000, botMaxCrashRockPercent);
            } else { //if 'rock strategy' bots found, create default 'aggressive strategy' for the new bot
                // from 4000ms=4sec (multiplayer = 1.27) to the 100% of the maximum seconds allowed MAX_CRASH__MILLISECONDS (32000 ms) (multiplayer = 6.83)
                maxBlastStrategy =
                        new MaxBlastChampionsBotAggressiveStrategy(this.scriptEngine, buyInAmount, 4000, botMaxCrashAggressivePercent);
            }

            return maxBlastStrategy;
        }
        return null;
    }

    private String extractSessionId(String response) {
        return extractQuotedStringAfterToken(response, "'sessionId': ");
    }

    private String extractQuotedStringAfterToken(String source, String token) {
        int start = source.indexOf("'", source.indexOf(token) + token.length()) + 1;
        int end = source.indexOf("'", start);
        return source.substring(start, end);
    }

    private String extractWebSocketUrl(String response) {
        return extractQuotedStringAfterToken(response, "'websocket': ");
    }

    private int extractServerId(String response) {
        try {
            String serverId = this.extractQuotedStringAfterToken(response, "'serverId': ");
            return Integer.parseInt(serverId);
        } catch (NumberFormatException e) {
            LOG.error(response);
            throw e;
        }
    }

    IManagedLobbyBot createLobbyBot(int gameId, String nickname, String userName, String password, String externalId,
                                    String botId, String openRoomWSUrl, int gameServerId, int bankId, String sessionId,
                                    Function<Void, Integer> shutdownCallback, IRoomBotStrategy botStrategy, long roomId,
                                    String token, String parsedLobbySocketUrl, long expiresAt, double shootsRate, double bulletsRate) {
        IManagedLobbyBot bot;
        if (gameId == GameType.BG_MAXCRASHGAME.getGameId()) {
            bot = new ManagedMaxBlastChampionsRoomBot(
                    this,
                    nickname,
                    userName,
                    password,
                    externalId,
                    botId,
                    openRoomWSUrl,
                    gameServerId,
                    bankId,
                    sessionId,
                    new GsonMessageSerializer(gson),
                    aVoid ->
                        {
                            if (shutdownCallback != null) {
                                shutdownCallback.apply(null);
                            }
                            return 0;
                        },
                    unused -> 0,
                    gameId,
                    botStrategy,
                    (int) roomId,
                    token);

            if(botStrategy instanceof MaxBlastChampionsBotStrategy) {
                ((MaxBlastChampionsBotStrategy)botStrategy).setBot(bot);
            }

            return bot;

        } else {
            bot = new ManagedLobbyBot(
                    this,
                    nickname,
                    userName,
                    password,
                    externalId,
                    String.valueOf(botId),
                    parsedLobbySocketUrl,
                    gameId,
                    bankId,
                    roomId,
                    openRoomWSUrl,
                    gameServerId,
                    sessionId,
                    new GsonMessageSerializer(gson),
                    aVoid ->
                        {
                            if (shutdownCallback != null) {
                                shutdownCallback.apply(null);
                            }
                            return 0;
                        },
                    botStrategy,
                    unused -> 0,
                    token,
                    expiresAt,
                    shootsRate,
                    bulletsRate);
        }

        return bot;
    }
}
