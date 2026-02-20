package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.ICrashGameSetting;
import com.betsoft.casino.mp.transport.CrashGameSetting;
import com.dgphoenix.casino.common.currency.CurrencyRate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;

/**
 * User: flsh
 * Date: 14.04.2022.
 */
@Service
public class CrashGameSettingsService implements ICrashGameSettingsService {
    private static final Logger LOG = LogManager.getLogger(CrashGameSettingsService.class);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final String ID_DELIMITER = "+";
    private static final int UPDATE_PERIOD_IN_MINUTES = 10;
    private static final double MAX_POSSIBLE_MULTIPLIER = BigDecimal.valueOf((Integer.MAX_VALUE - 1000) / 100).
            setScale(2, RoundingMode.HALF_UP).doubleValue();
    private static final CrashGameSetting DEFAULT = new CrashGameSetting(0, "EUR", 1000, 100000.00, 20000000, 20000000, 1, 500, false);
    //key is bankId+gameId
    private final ConcurrentMap<String, ICrashGameSetting> settingsMap = new ConcurrentHashMap<>();
    private final ISocketService socketService;
    private final ICurrencyRateService currencyRateService;

    public CrashGameSettingsService(ISocketService socketService, ICurrencyRateService currencyRateService) {
        this.socketService = socketService;
        this.currencyRateService = currencyRateService;
    }

    @PostConstruct
    private void init() {
        LOG.debug("Init");
        scheduler.scheduleWithFixedDelay(new SettingsUpdater(), UPDATE_PERIOD_IN_MINUTES, UPDATE_PERIOD_IN_MINUTES, TimeUnit.MINUTES);
    }

    @PreDestroy
    private void destroy() {
        LOG.debug("Shutdown");
        scheduler.shutdown();
    }

    @Override
    public Collection<ICrashGameSetting> getSettings() {
        return settingsMap.values();
    }

    @Override
    public ICrashGameSetting getSettings(long bankId, long gameId) {
        String key = getKey(bankId, gameId);
        ICrashGameSetting setting = settingsMap.get(key);

        LOG.debug("getSettings: key={}, setting={}", key, setting);

        if (setting != null) {
            return setting;
        }

        if (!settingsMap.containsKey(key)) {

            HashSet<Long> bankIds = new HashSet<>();
            bankIds.add(bankId);

            try {

                Set<ICrashGameSetting> bankSettings = socketService.getCrashGameSetting(bankIds, (int) gameId);
                this.addSettings(bankSettings, (int) gameId);
                setting = settingsMap.get(key);
                LOG.debug("getSettings: setting added for key={}, setting={}", key, setting);

            } catch (Exception e) {
                LOG.error("Cannot load game settings for bankId={}, gameId={}", bankId, gameId, e);
            }
        }

        setting = setting == null ? DEFAULT : setting;

        LOG.debug("getSettings: return setting={}", setting);

        return setting;
    }

    @Override
    public ICrashGameSetting getSettings(long bankId, long gameId, String roomCurrency) {

        ICrashGameSetting defaultBankSettings = getSettings(bankId, gameId);

        if (defaultBankSettings.getCurrencyCode().equals(roomCurrency)) {
            return defaultBankSettings;
        }

        CrashGameSetting crashGameSetting = new CrashGameSetting(
                bankId,
                roomCurrency,
                defaultBankSettings.getMaxRoomPlayers(),
                defaultBankSettings.getMaxMultiplier(),
                convertToPlayerCurrency(defaultBankSettings.getMaxPlayerProfitInRound(), defaultBankSettings.getCurrencyCode(), roomCurrency),
                convertToPlayerCurrency(defaultBankSettings.getTotalPlayersProfitInRound(), defaultBankSettings.getCurrencyCode(), roomCurrency),
                convertToPlayerCurrency(defaultBankSettings.getMinStake(), defaultBankSettings.getCurrencyCode(), roomCurrency),
                convertToPlayerCurrency(defaultBankSettings.getMaxStake(), defaultBankSettings.getCurrencyCode(), roomCurrency),
                defaultBankSettings.isSendRealBetWin()
        );

        LOG.debug("getSettings: return crashGameSetting={}", crashGameSetting);

        return crashGameSetting;
    }

    private void addSettings(Set<ICrashGameSetting> settings, int gameId) {
        for (ICrashGameSetting setting : settings) {

            LOG.debug("addSettings: setting before checks {}", setting);

            if (setting.getMaxRoomPlayers() > 1000) {
                setting.setMaxRoomPlayers(DEFAULT.getMaxRoomPlayers());
            }

            if (setting.getMaxMultiplier() >= MAX_POSSIBLE_MULTIPLIER) {
                setting.setMaxMultiplier(DEFAULT.getMaxMultiplier());
            }

            String currencyCode = setting.getCurrencyCode();
            if (setting.getTotalPlayersProfitInRound() == Long.MAX_VALUE) {
                setting.setTotalPlayersProfitInRound(getDefaultLimitConvertedToPlayerCurrency(DEFAULT.getTotalPlayersProfitInRound(), currencyCode));
            }

            if (setting.getMaxPlayerProfitInRound() == Long.MAX_VALUE) {
                setting.setMaxPlayerProfitInRound(getDefaultLimitConvertedToPlayerCurrency(DEFAULT.getMaxPlayerProfitInRound(), currencyCode));
            }

            if (setting.getMinStake() == Long.MAX_VALUE) {
                setting.setMinStake(getDefaultLimitConvertedToPlayerCurrency(DEFAULT.getMinStake(), currencyCode));
            } else if(setting.getMinStake() < 1) {
                LOG.warn("addSettings: bad minStake value [temp fix to 1], setting={}", settings);
                setting.setMinStake(1);
            }

            if (setting.getMaxStake() == Long.MAX_VALUE) {
                setting.setMaxStake(getDefaultLimitConvertedToPlayerCurrency(DEFAULT.getMaxStake(), currencyCode));
            }

            String key = getKey(setting.getBankId(), gameId);

            LOG.debug("addSettings: setting after checks for key {}:{}", key, setting);

            settingsMap.put(key, setting);
        }
    }

    private long getDefaultLimitConvertedToPlayerCurrency(long limit, String playerCurrencyCode) {
        return convertToPlayerCurrency(limit, DEFAULT.getCurrencyCode(), playerCurrencyCode);
    }

    private long convertToPlayerCurrency(long limit, String bankCurrencyCode, String playerCurrencyCode) {

        if(bankCurrencyCode.equals(playerCurrencyCode)) {
            return limit < 1 ? 1 : limit;
        }

        CurrencyRate currencyRate = currencyRateService.get(bankCurrencyCode, playerCurrencyCode);
        if (isSyntheticCurrency(currencyRate)) {
            return limit < 1 ? 1 : limit;
        }

        double rate = currencyRate.getRate();

        if (rate < 0) {
            CurrencyRate currencyRateNew = new CurrencyRate(bankCurrencyCode, playerCurrencyCode, -2, System.currentTimeMillis());
            CurrencyRate updatedRate = null;
            try {
                updatedRate = socketService.getCurrencyRatesSync(currencyRateNew);
            } catch (Exception e) {
                LOG.warn("convertToPlayerCurrency: update rate error, getCurrencyRatesSync call failed for rate={}", currencyRateNew, e);
            }
            if (updatedRate != null && updatedRate.getRate() != -2) {
                currencyRateService.updateOneCurrencyToCache(updatedRate);
                rate = updatedRate.getRate();
                LOG.debug("convertToPlayerCurrency: success update rate: {}", updatedRate);
            }
        }

        long convertedLimit = (long) (limit * rate);

        return convertedLimit < 1 ? 1 : convertedLimit;
    }

    private boolean isSyntheticCurrency(CurrencyRate currencyRate) {
        String playerCurrency = currencyRate.getDestinationCurrency();
        return currencyRate.getRate() <= 0 && ("MMC".equalsIgnoreCase(playerCurrency) || "MQC".equalsIgnoreCase(playerCurrency));
    }

    private String getKey(Long bankId, long gameId) {
        return bankId + ID_DELIMITER + gameId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashGameSettingsService.class.getSimpleName() + "[", "]")
                .add("settingsMap=" + settingsMap)
                .toString();
    }

    class SettingsUpdater implements Runnable {

        @Override
        public void run() {
            LOG.debug("SettingsUpdater: start");
            try {
                Map<Integer, Set<Long>> gameBanksMap = new HashMap<>();

                for (Map.Entry<String, ICrashGameSetting> entry : settingsMap.entrySet()) {
                    String key = entry.getKey();
                    StringTokenizer st = new StringTokenizer(key, ID_DELIMITER);
                    long bankId = Long.parseLong(st.nextToken());
                    int gameId = Integer.parseInt(st.nextToken());
                    Set<Long> banks = gameBanksMap.computeIfAbsent(gameId, k -> new HashSet<>());
                    banks.add(bankId);
                }

                for (Map.Entry<Integer, Set<Long>> entry : gameBanksMap.entrySet()) {
                    Integer gameId = entry.getKey();
                    Set<Long> bankIds = new HashSet<>(entry.getValue());
                    Set<ICrashGameSetting> bankSettings = socketService.getCrashGameSetting(bankIds, gameId);
                    addSettings(bankSettings, gameId);
                }

            } catch (Exception e) {
                LOG.error("SettingsUpdater: update error", e);
            }

            LOG.debug("SettingsUpdater: finish");
        }
    }
}
