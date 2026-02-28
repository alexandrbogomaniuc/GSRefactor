package com.abs.casino.actions.api.vba;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.gs.managers.game.session.GameSessionManager;
import com.abs.casino.gs.singlegames.tools.cbservtools.CM2Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class GetVBAAction extends BaseAction<GetVBAForm> {
    private static final Logger LOG = LogManager.getLogger(GetVBAAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, GetVBAForm actionForm, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        String vbaUrl = null;
        try {
            long gameSessionId = Long.parseLong(actionForm.getGameSessionId());

            GameSession gameSession = GameSessionManager.getInstance().getGameSessionById(gameSessionId);
            if (gameSession != null) {
                long gameId = gameSession.getGameId();
                Date startTime = new Date(gameSession.getStartTime());
                Date endTime = gameSession.getEndTime() == null ? null : new Date(gameSession.getEndTime());

                //INCORRECT: http://games-gp3.discreetgaming.com/vabs/show.jsp?VIEWSESSID=798795221&STARTDATE=2017-07-10%2009:50:21&ENDDATE=2017-07-10%2010:04:04&GAMEID=694
                //CORRECT:   http://hongtubet-gp3.discreetgaming.com/vabs/show.jsp?VIEWSESSID=798795221&STARTDATE=2017-07-10%2009:50:21&ENDDATE=2017-07-10%2010:04:04&GAMEID=694

                //String domain = "games" + GameServerConfiguration.getInstance().getGsDomain();
                String domain = request.getServerName();

                String srtStartTime = CM2Date.parseVAB(CM2Date.parse(startTime));
                String srtEndTime;
                if (endTime == null) {
                    srtEndTime = CM2Date.parseVAB(CM2Date.parse(new Date(startTime.getTime() + TimeUnit.DAYS.toMillis(2))));
                } else {
                    srtEndTime = CM2Date.parseVAB(CM2Date.parse(endTime));
                }

                vbaUrl = "http://" + domain + "/vabs/show.jsp?VIEWSESSID=" + gameSessionId +
                        "&STARTDATE=" + srtStartTime + "&ENDDATE=" + srtEndTime + "&GAMEID=" + gameId;
                LOG.info("Vab url after parse: " + vbaUrl);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        if (StringUtils.isTrimmedEmpty(vbaUrl)) {
            return mapping.findForward(ERROR_FORWARD);
        }
        return getForward(vbaUrl);
    }

    protected ActionForward getForward(String url) {
        ActionRedirect redirect = new ActionRedirect(url);
        return redirect;
    }
}
