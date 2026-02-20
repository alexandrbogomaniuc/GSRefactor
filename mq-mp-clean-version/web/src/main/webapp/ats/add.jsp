<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.betsoft.casino.mp.service.BotConfigInfoService" %>
<%@ page import="com.betsoft.casino.mp.config.WebSocketRouter" %>
<%@ page import="com.betsoft.casino.mp.model.bots.BotConfigInfo" %>
<%@ page import="com.betsoft.casino.mp.model.bots.dto.BotStatusResult" %>
<%@ page import="com.betsoft.casino.mp.model.GameType" %>
<%@ page import="java.util.*" %>
<%@ page import="com.betsoft.casino.mp.transport.Avatar" %>
<%@ page import="com.dgphoenix.casino.common.util.RNG" %>
<%@ page import="com.betsoft.casino.mp.web.socket.BotServiceClient" %>
<%@ page import="com.betsoft.casino.mp.model.bots.TimeFrame" %>
<%@ page import="java.time.LocalTime" %>
<%@ page import="java.time.DayOfWeek" %>
<%@ page import="com.betsoft.casino.bots.service.MQBBotServiceHandler" %>
<%@ page import="com.hazelcast.util.StringUtil" %>

<%
    BotConfigInfoService botConfigInfoService = WebSocketRouter.getApplicationContext().getBean(BotConfigInfoService.class);
    BotServiceClient botServiceClient = WebSocketRouter.getApplicationContext().getBean(BotServiceClient.class);

    try {
        String bankIdParam = request.getParameter("bankId");
        String usernameParam = request.getParameter("username");
        String passwordParam = request.getParameter("password");
        String mqNickNameParam = request.getParameter("mqNickName");
        String activeParam = request.getParameter("active");
        String startTimeParam = request.getParameter("startTime");
        String endTimeParam = request.getParameter("endTime");
        String daysParam = request.getParameter("days");
        String gamesParam = request.getParameter("games");
        String allowedBankIdsParam = request.getParameter("bankIds");
        String allowedValues6274Param = request.getParameter("values6274");
        String allowedValues6275Param = request.getParameter("values6275");
        String dsShootingRateParam = request.getParameter("dsSR");
        String maShootingRateParam = request.getParameter("maSR");
        String sxShootingRateParam = request.getParameter("sxSR");

        String dsBulletRateParam = request.getParameter("dsBR");
        String maBulletRateParam = request.getParameter("maBR");
        String sxBulletRateParam = request.getParameter("sxBR");

        String botIdParam = request.getParameter("botId");

        long bankId = Long.parseLong(bankIdParam);
        if (bankId < 0) {
            throw new IllegalArgumentException("Please enter correct bankId");
        }

        if (StringUtils.isTrimmedEmpty(usernameParam)) {
            throw new IllegalArgumentException("Please enter username");
        }

        if (StringUtils.isTrimmedEmpty(passwordParam)) {
            throw new IllegalArgumentException("Please enter password");
        }

        if (StringUtils.isTrimmedEmpty(mqNickNameParam)) {
            throw new IllegalArgumentException("Please enter mqNickName");
        }

        boolean active = Boolean.parseBoolean(activeParam.toUpperCase());

        LocalTime sTime = LocalTime.of(0, 0, 0, 0);
        LocalTime eTime = LocalTime.of(23, 59, 59, 999999999);

        if (!StringUtils.isTrimmedEmpty(startTimeParam)) {
            try {
                sTime = TimeFrame.parseFlexibleLocalTime(startTimeParam);
            } catch ( Exception e) {}
        }

        if (!StringUtils.isTrimmedEmpty(endTimeParam)) {
            try {
                eTime = TimeFrame.parseFlexibleLocalTime(endTimeParam);
            } catch ( Exception e) {}
        }

        Set<DayOfWeek> daysOfWeek = new HashSet<>();
        StringTokenizer daysTokenizer = new StringTokenizer(daysParam, ",");
        while (daysTokenizer.hasMoreTokens()) {
            String dayToken = daysTokenizer.nextToken();
            int dayTokenInt = Integer.parseInt(dayToken);
            DayOfWeek dayOfWeek = DayOfWeek.of(dayTokenInt);

            daysOfWeek.add(dayOfWeek);
        }

        TimeFrame timeFrame = new TimeFrame(sTime, eTime, daysOfWeek);
        Set<TimeFrame> timeFrames = new HashSet<>();
        timeFrames.add(timeFrame);

        Set<GameType> allowedGames = new HashSet<>();
        StringTokenizer gamesTokenizer = new StringTokenizer(gamesParam, ",");
        while (gamesTokenizer.hasMoreTokens()) {
            String gameToken = gamesTokenizer.nextToken();
            GameType game = GameType.getByGameId(Integer.parseInt(gameToken));
            if (game == null || !BotConfigInfo.allowedMQGames.contains(game)) {
                throw new IllegalArgumentException("Game with id=" + gameToken + " not supported");
            }
            allowedGames.add(game);
        }

        Set<Long> allowedBankIds = new HashSet<>();
        StringTokenizer allowedBankIdsTokenizer = new StringTokenizer(allowedBankIdsParam, ",");
        while (allowedBankIdsTokenizer.hasMoreTokens()) {
            String allowedBankIdToken = allowedBankIdsTokenizer.nextToken();
            Long allowedBankId = Long.parseLong(allowedBankIdToken);
            if (!MQBBotServiceHandler.MQB_BANKS.contains(allowedBankId)) {
                throw new IllegalArgumentException("Bank with id=" + allowedBankId + " not supported");
            }
            allowedBankIds.add(allowedBankId);
        }

        allowedValues6274Param = allowedValues6274Param.replace("[", "");
        allowedValues6274Param = allowedValues6274Param.replace("]", "");
        allowedValues6274Param = allowedValues6274Param.replace(" ", "");
        Set<Long> allowedValues6274Set = new HashSet<>();
        if(!StringUtil.isNullOrEmpty(allowedValues6274Param)) {
            if(allowedValues6274Param.contains(",")) {
                for (String allowedValueStr : allowedValues6274Param.split(",")) {
                    long allowedValue = Long.parseLong(allowedValueStr);
                    allowedValues6274Set.add(allowedValue);
                }
            } else {
                long allowedValue = Long.parseLong(allowedValues6274Param);
                allowedValues6274Set.add(allowedValue);
            }
        }

        allowedValues6275Param = allowedValues6275Param.replace("[", "");
        allowedValues6275Param = allowedValues6275Param.replace("]", "");
        allowedValues6275Param = allowedValues6275Param.replace(" ", "");
        Set<Long> allowedValues6275Set = new HashSet<>();
        if(!StringUtil.isNullOrEmpty(allowedValues6275Param)) {
            if(allowedValues6275Param.contains(",")) {
                for (String allowedValueStr : allowedValues6275Param.split(",")) {
                    long allowedValue = Long.parseLong(allowedValueStr);
                    allowedValues6275Set.add(allowedValue);
                }
            } else {
                long allowedValue = Long.parseLong(allowedValues6275Param);
                allowedValues6275Set.add(allowedValue);
            }
        }

        Map<Long, Set<Long>> allowedRoomValues = new HashMap<>();
        allowedRoomValues.put(BotConfigInfo.MMC_BankId, allowedValues6274Set);
        allowedRoomValues.put(BotConfigInfo.MQC_BankId, allowedValues6275Set);

        double dsShootingRate = Double.parseDouble(dsShootingRateParam);
        if(dsShootingRate < 0) {
            dsShootingRate = 0;
        }
        if(dsShootingRate > 1) {
            dsShootingRate = 1;
        }

        double maShootingRate = Double.parseDouble(maShootingRateParam);
        if(maShootingRate < 0) {
            maShootingRate = 0;
        }
        if(maShootingRate > 1) {
            maShootingRate = 1;
        }

        double sxShootingRate = Double.parseDouble(sxShootingRateParam);
        if(sxShootingRate < 0) {
            sxShootingRate = 0;
        }
        if(sxShootingRate > 1) {
            sxShootingRate = 1;
        }

        Map<Long, Double> shotRates = new HashMap<>();
        shotRates.put(GameType.BG_DRAGONSTONE.getGameId(), dsShootingRate);
        shotRates.put(GameType.BG_MISSION_AMAZON.getGameId(), maShootingRate);
        shotRates.put(GameType.BG_SECTOR_X.getGameId(), sxShootingRate);

        double dsBulletRate = Double.parseDouble(dsBulletRateParam);
        if(dsBulletRate < 0) {
            dsBulletRate = 0;
        }
        if(dsBulletRate > 1) {
            dsBulletRate = 1;
        }

        double maBulletRate = Double.parseDouble(maBulletRateParam);
        if(maBulletRate < 0) {
            maBulletRate = 0;
        }
        if(maBulletRate > 1) {
            maBulletRate = 1;
        }

        double sxBulletRate = Double.parseDouble(sxBulletRateParam);
        if(sxBulletRate < 0) {
            sxBulletRate = 0;
        }
        if(sxBulletRate > 1) {
            sxBulletRate = 1;
        }

        Map<Long, Double> bulletRates = new HashMap<>();
        bulletRates.put(GameType.BG_DRAGONSTONE.getGameId(), dsBulletRate);
        bulletRates.put(GameType.BG_MISSION_AMAZON.getGameId(), maBulletRate);
        bulletRates.put(GameType.BG_SECTOR_X.getGameId(), sxBulletRate);

        Avatar avatar= new Avatar(RNG.nextInt(2), RNG.nextInt(2), RNG.nextInt(2));

        long botId = Long.parseLong(botIdParam);

        if (botId > 0) {

            BotConfigInfo botConfigInfo = botConfigInfoService.update(botId, allowedGames, active, passwordParam,
                    mqNickNameParam, avatar, timeFrames, allowedBankIds, shotRates, bulletRates, allowedRoomValues);

            BotStatusResult status = botServiceClient.getStatusForNewBot(botConfigInfo.getUsername(), botConfigInfo.getPassword(),
                                botConfigInfo.getMqNickname(), botConfigInfo.getBankId(), GameType.BG_DRAGONSTONE.getGameId());

            if (status.isSuccess()) {
                botConfigInfo.setMqcBalance(status.getMqcBalance());
                botConfigInfo.setMmcBalance(status.getMmcBalance());

                botConfigInfoService.updateBalance(botId, status.getMmcBalance(), status.getMqcBalance());
            }

            response.setStatus(200);
            response.getWriter().print("Ats with id=" + botConfigInfo.getId() + " was successfully updated");

        } else {

            BotConfigInfo botConfigInfo =
                    new BotConfigInfo(0, bankId, allowedGames, active, usernameParam, passwordParam, mqNickNameParam,
                            avatar, timeFrames, allowedBankIds, shotRates, bulletRates, allowedRoomValues);

            BotStatusResult status = botServiceClient.getStatusForNewBot(botConfigInfo.getUsername(), botConfigInfo.getPassword(),
                botConfigInfo.getMqNickname(), bankId, GameType.BG_DRAGONSTONE.getGameId());

            if (status.isSuccess()) {
                botConfigInfo.setMqcBalance(status.getMqcBalance());
                botConfigInfo.setMmcBalance(status.getMmcBalance());

                botConfigInfoService.create(botConfigInfo);
            }

            response.setStatus(200);
            response.getWriter().print("Ats was successfully created");
        }
    } catch (Exception e) {
        response.setStatus(400);
        response.getWriter().print(e.getMessage());
    }
%>
