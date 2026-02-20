package com.dgphoenix.casino.web.history;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.language.LanguageType;
import com.dgphoenix.casino.common.configuration.messages.MessageManager;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.web.AbstractPageableForm;
import com.dgphoenix.casino.common.web.IdValueBean;
import com.google.common.collect.Collections2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

/**
 * User: flsh
 * Date: 16.04.2009
 */
public class GameHistoryListForm extends AbstractPageableForm {
    public static final long ALL_GAMES = -1;
    private static final Logger LOG = LogManager.getLogger(GameHistoryListForm.class);
    private String sessionId;
    private Long gameId;
    private int startDay;
    private int startMonth;
    private int startYear;
    private int startHour;
    private int startMinute;
    private int startSecond;
    private int endDay;
    private int endMonth;
    private int endYear;
    private int endHour;
    private int endMinute;
    private int endSecond;
    private long timeOffset;
    private int bankOffset;
    private String lang;
    private ZoneId dstZone;

    protected Integer bankId;

    private int mode;         //0 - all, 1 - cash, 2 - bonus, 3 - frb
    private long lastGameSessionDateOnPage;

    public GameHistoryListForm() {
    }

    public int getBankId() {
        return bankId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getEndDay() {
        return endDay;
    }

    public void setEndDay(int endDay) {
        this.endDay = endDay;
    }

    public int getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(int endMonth) {
        this.endMonth = endMonth;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getStartSecond() {
        return startSecond;
    }

    public void setStartSecond(int startSecond) {
        this.startSecond = startSecond;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public int getEndSecond() {
        return endSecond;
    }

    public void setEndSecond(int endSecond) {
        this.endSecond = endSecond;
    }

    public Long getGameId() {
        return gameId;
    }

    protected long getTimeOffset() {
        return timeOffset;
    }

    public int getBankOffset() {
        return bankOffset;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    protected void setTimeOffset(int bankOffset) {
        this.bankOffset = bankOffset;
        this.timeOffset = 0;
        if (bankOffset != 0) {
            this.timeOffset = (bankOffset - TimeUnit.DAYS.toMinutes(1)) * TimeUnit.MINUTES.toMillis(1);
        }
    }

    protected void setDSTZone(ZoneId zone) {
        this.dstZone = zone;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Date getStartDate() {
        return new Date(removeOffset(
                new GregorianCalendar(startYear, startMonth - 1, startDay,
                        startHour, startMinute, startSecond).getTimeInMillis()));
    }

    public Date getEndDate() {
        return new Date(removeOffset(
                new GregorianCalendar(endYear, endMonth - 1, endDay,
                        endHour, endMinute, endSecond).getTimeInMillis()));
    }

    protected Long applyOffset(long millis) {
        return millis + getTimeOffset() + getDstOffset(millis);
    }

    protected Long removeOffset(long millis) {
        return millis - getTimeOffset() - getDstOffset(millis);
    }

    protected Long getDstOffset(long millis) {
        return (dstZone != null) ? GameHistoryServlet.getDSTTimeInMillis(dstZone, millis) : 0L;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public long getLastGameSessionDateOnPage() {
        return applyOffset(lastGameSessionDateOnPage);
    }

    public void setLastGameSessionDateOnPage(long lastGameSessionDateOnPage) {
        this.lastGameSessionDateOnPage = lastGameSessionDateOnPage;
    }

    @Override
    public int getItemsPerPage() {
        int bankValue = 0;
        if (bankId != null) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(getBankId());
            bankValue = bankInfo.getHistoryItemsPerPage();
        }
        return bankValue != 0 ? bankValue : DEFAULT_ITEMS_PER_PAGE;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        LOG.debug("GameHistoryListForm::validate start {}", this.toString());

        try {
            if (LanguageType.toLanguageType(lang) == null) {
                LOG.error("Invalid language code: {}", lang);
                lang = "en";
            }
            if (isTrimmedEmpty(sessionId)) {
                LOG.error("GameHistoryListForm::validate sessionId:{} gameId:{}", sessionId, gameId);

                removeAttributes(request);
                errors.add("history", new ActionMessage("error.history.invalidParameters"));
                return errors;
            }

            Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserId(sessionId);

            this.bankId = pair.getKey();
            request.setAttribute(GameHistoryListAction.BANK_ID, bankId);
            request.setAttribute(GameHistoryListAction.SESSION_ID, sessionId);
            if (gameId == null) {
                populate(request);
            } else {
                if (isPopulate(request)) {
                    populate(request);
                }
            }
            if (mode < 0 || mode > 3) {
                mode = 0;            // all
            }

            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(getBankId());
            setTimeOffset(bankInfo.getHistoryTimeOffset());
            if (bankInfo.getHistoryDSTZone() != null) {
                setDSTZone(bankInfo.getHistoryDSTZone());
            }
            request.setAttribute(GameHistoryListAction.CURRENT_TIME, applyOffset(System.currentTimeMillis()));

        } catch (Exception e) {
            LOG.error("GameHistoryListForm::validate sessionId:{} gameId:{} error:{}", sessionId, gameId, e);
            removeAttributes(request);
            errors.add("history", new ActionMessage("error.history.internalError"));
            return errors;
        }

        return errors;
    }

    public boolean isPopulate(HttpServletRequest request) {
        return request.getSession().getAttribute(GameHistoryListAction.GAMES_LIST) == null;
    }

    protected void populate(HttpServletRequest request) {
        Map<Long, IBaseGameInfo> games = BaseGameCache.getInstance().getAllGameInfosAsMap(bankId, null);
        List<IdValueBean> gamesList = new ArrayList<>(games.size());
        for (Map.Entry<Long, IBaseGameInfo> entry : games.entrySet()) {
            IBaseGameInfo gameInfo = entry.getValue();
            String title = MessageManager.getLocalizedTitleOrDefault(bankId, gameInfo.getId(), this.lang);
            if (isTrimmedEmpty(title)) {
                BaseGameInfoTemplate gameInfoTemplate = BaseGameInfoTemplateCache.getInstance()
                        .getBaseGameInfoTemplateById(gameInfo.getId());
                if (gameInfoTemplate != null) {
                    title = gameInfoTemplate.getTitle();
                }
            }
            gamesList.add(new IdValueBean(gameInfo.getId(), title));
        }

        request.getSession().setAttribute(GameHistoryListAction.GAMES_LIST,
                Collections2.filter(gamesList, input -> !isTrimmedEmpty(input.getValue())));
    }

    private void removeAttributes(HttpServletRequest request) {
        request.removeAttribute(GameHistoryListAction.GAME_HISTORY_LIST);
        request.removeAttribute(GameHistoryListAction.GAMES_LIST);
        request.removeAttribute(GameHistoryListAction.SESSION_ID);
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.bankId = null;
        this.startHour = 0;
        this.startMinute = 0;
        this.startSecond = 0;
        this.endHour = 23;
        this.endMinute = 59;
        this.endSecond = 59;
    }

    @Override
    public String toString() {
        final String TAB = "    ";
        final StringBuilder sb = new StringBuilder();
        sb.append("GameHistoryListForm");
        sb.append("{ " + super.toString() + TAB);
        sb.append("{sessionId='").append(sessionId).append('\'');
        sb.append(", bankId=").append(bankId);
        sb.append(", timeOffset=").append(timeOffset);
        sb.append(", gameId=").append(gameId);
        sb.append(", startDay=").append(startDay);
        sb.append(", startMonth=").append(startMonth);
        sb.append(", startYear=").append(startYear);
        sb.append(", startHour=").append(startHour);
        sb.append(", startMinute=").append(startMinute);
        sb.append(", startSecond=").append(startSecond);
        sb.append(", endDay=").append(endDay);
        sb.append(", endMonth=").append(endMonth);
        sb.append(", endYear=").append(endYear);
        sb.append(", endHour=").append(endHour);
        sb.append(", endMinute=").append(endMinute);
        sb.append(", endSecond=").append(endSecond);
        sb.append(", lastGameSessionDateOnPage=").append(lastGameSessionDateOnPage);
        sb.append(", lang=").append(lang);
        sb.append(", dstZone=").append(dstZone);
        sb.append('}');
        return sb.toString();
    }
}
