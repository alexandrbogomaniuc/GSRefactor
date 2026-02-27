package com.dgphoenix.casino.actions.api.bonus;

import com.dgphoenix.casino.account.AccountManager;
import com.abs.casino.actions.api.bonus.response.JSONResponse;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.*;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.GameType;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.exception.XmlWriterException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.common.web.bonus.BonusError;
import com.dgphoenix.casino.common.web.bonus.BonusErrors;
import com.dgphoenix.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.bonus.*;
import com.abs.casino.gs.managers.payment.bonus.client.BonusAccountInfoResult;
import com.dgphoenix.casino.services.bonus.ForbiddenGamesForBonusProvider;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

/**
 * User: flsh
 * Date: 7/28/11
 */
public abstract class AbstractBonusAction<T extends BonusForm> extends BaseAction<T> {
    public static final String BANK_ID_PARAM = "bankId";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    public static final DateTimeFormatter DATE_TIME_MS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    protected static final DateTimeFormatter INPUT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy H:m:s");

    protected static final Gson gsonSerializer = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static void buildResponseJSON(HttpServletResponse response, Map<String, String> inParams, Map<String, Object> outParams, BonusForm form) throws IOException {
        response.setContentType(APPLICATION_JSON_UTF8.toString());
        JSONResponse jsonResponse = new JSONResponse(inParams, outParams, form);
        response.getWriter().println(gsonSerializer.toJson(jsonResponse));
    }

    protected void buildResponseXML(XmlWriter xw, Map<String, String> inParams, Map<String, Object> outParams, BonusForm form) {
        try {
            xw.startDocument(GameServerConfiguration.getInstance().getBrandApiRootTagName());
            if (!inParams.isEmpty()) {
                xw.startNode(CBonus.REQUEST_TAG);
                for (Map.Entry<String, String> stringStringEntry : inParams.entrySet()) {
                    xw.node(stringStringEntry.getKey(), stringStringEntry.getValue());
                    getLogger().debug(stringStringEntry.getKey() + " = " + stringStringEntry.getValue());
                }
                xw.endNode(CBonus.REQUEST_TAG);
            }
            LocalDateTime now = LocalDateTime.now();
            xw.node(CBonus.TIME_TAG, now.format(DATE_TIME_MS_FORMATTER));
            if (!outParams.isEmpty()) {
                xw.startNode(CBonus.RESPONSE_TAG);
                for (Map.Entry<String, Object> stringObjectEntry : outParams.entrySet()) {
                    if (stringObjectEntry.getKey().equals(CBonus.BONUS_LIST)) {
                        List<BaseBonus> bonuses = (List<BaseBonus>) stringObjectEntry.getValue();
                        bonuses.sort(Comparator.comparingLong(BaseBonus::getTimeAwarded));
                        for (BaseBonus bonus : bonuses) {
                            xw.startNode(CBonus.BONUS);
                            printBonusInfo(xw, bonus, form);
                            xw.endNode(CBonus.BONUS);
                        }

                    } else {
                        xw.node(stringObjectEntry.getKey(), stringObjectEntry.getValue().toString());
                    }
                    //getLogger().debug(key + " = " + outParams.get(key));
                }
                xw.endNode(CBonus.RESPONSE_TAG);
            }
            xw.endDocument(GameServerConfiguration.getInstance().getBrandApiRootTagName());
        } catch (XmlWriterException e) {
            getLogger().error("can not buildResponseXML", e);
        }
    }

    protected void printBonusInfo(XmlWriter xw, BaseBonus baseBonus, BonusForm form) throws XmlWriterException {
        if (baseBonus instanceof Bonus) {
            Bonus bonus = (Bonus) baseBonus;
            xw.node(CBonus.BONUSID, Long.toString(bonus.getId()));
            xw.node(CBonus.TYPE, bonus.getType().toString());
            LocalDateTime awardDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(bonus.getTimeAwarded()),
                    TimeZone.getDefault().toZoneId());
            xw.node(CBonus.AWARDDATE, awardDate.format(DATE_FORMATTER));
            if (form.isSendBonusAwardTime()) {
                xw.node(CBonus.AWARDTIME, awardDate.format(TIME_FORMATTER));
            }
            xw.node(CBonus.AMOUNT, Long.toString(bonus.getAmount()));
            xw.node(CBonus.BALANCE, Long.toString(bonus.getBalance()));
            xw.node(CBonus.ROLLOVER, Long.toString((long) (bonus.getRolloverMultiplier() * bonus.getAmount())));
            xw.node(CBonus.COLLECTED, Long.toString(bonus.getBetSum()));
            xw.node(CBonus.MAXWIN, bonus.getMaxWinLimit() == null ? "null" : Long.toString(bonus.getMaxWinLimit()));
            xw.node(CBonus.DESCRIPTION, bonus.getDescription());
            xw.node(CBonus.GAMEIDS, getGameIds(bonus));
            LocalDateTime expDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(bonus.getExpirationDate()),
                    TimeZone.getDefault().toZoneId());
            xw.node(CBonus.EXPDATE, expDate.format(DATE_FORMATTER));
        } else {
            FRBonus frBonus = (FRBonus) baseBonus;
            xw.node(CBonus.BONUSID, Long.toString(frBonus.getId()));
            if (form.isSendDetailsOnFrbInfo()) {
                xw.node(CBonus.PARAM_EXTBONUSID, frBonus.getExtId());
            }
            LocalDateTime awardDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(frBonus.getTimeAwarded()),
                    TimeZone.getDefault().toZoneId());
            xw.node(CBonus.AWARDDATE, awardDate.format(DATE_FORMATTER));
            if (form.isSendBonusAwardTime()) {
                xw.node(CBonus.AWARDTIME, awardDate.format(TIME_FORMATTER));
            }
            xw.node(CBonus.ROUNDS, Long.toString(frBonus.getRounds()));
            xw.node(CBonus.ROUNDSLEFT, Long.toString(frBonus.getRoundsLeft()));
            xw.node(CBonus.DESCRIPTION, frBonus.getDescription());
            xw.node(CBonus.GAMEIDS, getGameIds(frBonus));
            if (frBonus.getStartDate() != null) {
                LocalDateTime startDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(frBonus.getStartDate()),
                        TimeZone.getDefault().toZoneId());
                xw.node(CBonus.PARAM_START_DATE, startDate.format(DATE_TIME_FORMATTER));
            }
            if (frBonus.getExpirationDate() != null) {
                LocalDateTime expDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(frBonus.getExpirationDate()),
                        TimeZone.getDefault().toZoneId());
                xw.node(CBonus.PARAM_EXP_DATE, expDate.format(DATE_TIME_FORMATTER));
            }
            if (frBonus.getFreeRoundValidity() != null) {
                xw.node(CBonus.PARAM_DURATION, Long.toString(frBonus.getFreeRoundValidity()));
            }
            if (frBonus.getMaxWinLimit() != null) {
                xw.node(CBonus.PARAM_MAX_WIN_LIMIT, Long.toString(frBonus.getMaxWinLimit()));
            }
        }
    }

    public static String getGameIds(BaseBonus bonus) throws XmlWriterException {
        try {
            long bankId = bonus.getBankId();
            Currency currency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
            Collection<Long> games;
            if (bonus instanceof FRBonus) {
                games = bonus.getGameIds();
            } else {
                games = bonus.getValidGameIds(BaseGameCache.getInstance().getAllGamesSet(bankId, currency));
                ForbiddenGamesForBonusProvider forbiddenGamesForBonusProvider = ApplicationContextHelper.getApplicationContext()
                        .getBean(ForbiddenGamesForBonusProvider.class);
                games.removeAll(forbiddenGamesForBonusProvider.getGames(bankId));
            }

            if (games != null && !games.isEmpty()) {
                StringBuilder buffer = new StringBuilder();
                BonusManager bonusManager = BonusManager.getInstance();
                for (Long gameId : games) {
                    if (isGameEnabled(bankId, gameId, currency)) {
                        buffer.append(bonusManager.getExternalGameId(gameId, bankId));
                        buffer.append(',');
                    }
                }

                return buffer.toString();
            }

            return "";
        } catch (Exception e) {
            throw new XmlWriterException(e);
        }
    }

    public boolean isActionGames(List<Long> gameList) {
        Set<Long> mpGames = gameList.stream()
                .map(BaseGameInfoTemplateCache.getInstance()::getDefaultGameInfo)
                .filter(Objects::nonNull)
                .filter(gameInfo -> gameInfo.getGameType() == GameType.MP)
                .map(BaseGameInfo::getId)
                .collect(Collectors.toSet());
        return !mpGames.isEmpty();
    }

    protected static boolean isGameEnabled(Long bankId, Long gameId, Currency currency) {
        IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, currency);
        return (gameInfo != null) && gameInfo.isEnabled();
    }

    protected AccountInfo createAccount(BankInfo bankInfo, String accountExtId,
                                        BonusSystemType bonusSystemType) throws BonusException {
        try {
            long bankId = bankInfo.getId();
            BonusAccountInfoResult result;
            if (bonusSystemType == BonusSystemType.ORDINARY_SYSTEM) {
                IBonusClient client = BonusManager.getInstance().getClient(bankId);
                result = client.getAccountInfo(accountExtId);
            } else { // FRB System
                IFRBonusManager bonusManager = FRBonusManager.getInstance();
                IFRBonusClient client = bonusManager.getClient(bankId);
                result = client.getAccountInfo(accountExtId);
            }
            return AccountManager.getInstance().saveAccountWithCurrencyUpdate(null, accountExtId, bankInfo, result.getUserName(),
                    false, false, result.getEmail(), ClientType.FLASH, result.getFirstName(), result.getLastName(),
                    result.getCurrency(), result.getCountryCode(), true);
        } catch (Exception e) {
            throw new BonusException(e);
        }
    }

    protected String getHashValue(List<String> params, long bankId) throws BonusException {
        try {
            StringBuilder sb = new StringBuilder();
            for (String param : params) {
                sb.append(param);
            }
            sb.append(BankInfoCache.getInstance().getBankInfo(bankId).getBonusPassKey());
            return StringUtils.getMD5(sb.toString());
        } catch (Exception e) {
            throw new BonusException(e);
        }
    }

    protected Collection<Long> getValidGameIds(Collection<Long> fullList, BonusGameMode mode, Collection<Long> gameIds) {
        List<Long> validGameIds = new ArrayList<>();
        Set<Long> actionGames = BaseGameInfoTemplateCache.getInstance().getMultiplayerGames();

        if (gameIds != null && !gameIds.isEmpty()) {
            validGameIds.addAll(fullList);
            if (mode.equals(BonusGameMode.ALL) || mode.equals(BonusGameMode.ONLY)) {
                validGameIds.retainAll(gameIds);
            } else if (mode.equals(BonusGameMode.EXCEPT)) {
                validGameIds.removeAll(gameIds);
            }

            boolean isActionMode = gameIds.stream().anyMatch(actionGames::contains);
            if (isActionMode) {
                validGameIds.retainAll(actionGames);
            } else {
                validGameIds.removeAll(actionGames);
            }
        } else {
            if (mode.equals(BonusGameMode.ALL)) {
                validGameIds.addAll(fullList);
                validGameIds.removeAll(actionGames);
            }
        }
        return validGameIds;
    }

    protected BonusForm changeBankByExternalUserId(String accountExtId, BonusForm form) throws BonusException {
        try {
            BankInfo sourceBank = BankInfoCache.getInstance().getBankInfo(form.getBankId());
            IFRBonusManager bonusManager = FRBonusManager.getInstance();
            IFRBonusClient client = bonusManager.getClient(sourceBank.getId());
            String currency = client.getAccountInfo(accountExtId).getCurrency();
            if (!currency.equals(sourceBank.getDefaultCurrency().getCode())) {
                List<Long> bankIds = SubCasinoCache.getInstance().getBankIds(sourceBank.getSubCasinoId());
                for (long bankId : bankIds) {
                    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                    if (bankInfo.getExternalBankIdDescription().startsWith(sourceBank.getExternalBankIdDescription())
                            && bankInfo.getDefaultCurrency().getCode().equals(currency)) {
                        form.setBankInfo(bankInfo);
                        form.setBankId((int) bankInfo.getId());
                        form.setExtBankId(bankInfo.getExternalBankId());
                        LOG.debug("Original bankId = {}, adjusted bankId = {}", sourceBank.getId(), bankInfo.getId());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new BonusException(e);
        }
        return form;
    }

    public static Long parseExpDateTime(String sTime, String sTimeZone, BonusError parseError) throws BonusException {
        try {
            Long expTime = parseDateTime(sTime, sTimeZone);
            if (expTime != null && expTime < System.currentTimeMillis()) {
                throw new BonusException(BonusErrors.EXPIRED);
            }
            return expTime;
        } catch (DateTimeParseException e) {
            throw new BonusException(parseError);
        }
    }

    public static Long parseStartDateTime(String sTime, String sTimeZone) throws BonusException {
        try {
            return parseDateTime(sTime, sTimeZone);
        } catch (DateTimeParseException e) {
            throw new BonusException(BonusErrors.INVALID_START_TIME);
        }
    }

    public static Long parseDateTime(String dateTimeStr, String sTimeZone) {
        Long dateTime = null;
        if (!StringUtils.isTrimmedEmpty(dateTimeStr)) {
            dateTimeStr = dateTimeStr.trim();
            if (dateTimeStr.length() <= 10) {
                dateTimeStr += " 00:00:00";
            }

            Integer timeZone = 0;
            if (!StringUtils.isTrimmedEmpty(sTimeZone) && !"null".equals(sTimeZone)) {
                timeZone = Integer.parseInt(sTimeZone);
            }

            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, INPUT_DATE_TIME_FORMATTER);
            ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
            dateTime = zdt.minusHours(timeZone).toEpochSecond() * 1000;
        }
        return dateTime;
    }

    protected Long parseMaxWinLimit(String maxWinLimitString) throws BonusException {
        if (!StringUtils.isTrimmedEmpty(maxWinLimitString)) {
            try {
                Long maxWinLimit = Long.parseLong(maxWinLimitString);
                if (maxWinLimit < 1) {
                    getLogger().error("Incorrect maxWin! maxWin: {}", maxWinLimit);
                    throw new BonusException(BonusErrors.INVALID_MAX_WIN);
                }
                return maxWinLimit;
            } catch (NumberFormatException e) {
                getLogger().error(e.getMessage(), e);
                throw new BonusException(BonusErrors.INVALID_MAX_WIN);
            }
        }
        return null;
    }

    protected Double parseMaxWinMultiplier(String maxWinMultiplierString) throws BonusException {
        if (!StringUtils.isTrimmedEmpty(maxWinMultiplierString)) {
            try {
                return Double.parseDouble(maxWinMultiplierString);
            } catch (NumberFormatException e) {
                getLogger().error(e.getMessage(), e);
                throw new BonusException(BonusErrors.INVALID_MAX_WIN_MULTIPLIER);
            }
        }
        return null;
    }

    protected abstract Logger getLogger();

}
