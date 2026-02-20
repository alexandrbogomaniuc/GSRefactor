package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.mqb.ManagedBattleGroundRoomBot;
import com.betsoft.casino.bots.strategies.IRoomNaturalBotStrategy;
import com.betsoft.casino.mp.transport.Bullet;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.dgphoenix.casino.common.util.string.StringUtils;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

/**
 * User: flsh
 * Date: 08.04.2021.
 */
public class BulletRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;
    private final String bulletId;
    private ShotRequest shotRequest;
    private int weaponId;
    private int startPointX;
    private int startPointY;
    private int endPointX;
    private int endPointY;
    private float bulletAngle;
    private String shotMetric;

    public BulletRequest(IRoomBot bot, ISocketClient client, String bulletId, int startPointX, int startPointY,
                         int endPointX, int endPointY, float bulletAngle, ShotRequest shotRequest, int weaponId, String shotMetric) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.bulletId = bulletId;
        this.startPointX = startPointX;
        this.startPointY = startPointY;
        this.endPointX = endPointX;
        this.endPointY = endPointY;
        this.bulletAngle = bulletAngle;
        this.shotRequest = shotRequest;
        this.weaponId = weaponId;
        this.shotMetric = shotMetric;
    }

    public BulletRequest(IRoomBot bot, ISocketClient client, String bulletId, ShotRequest shotRequest, int weaponId, String shotMetric) {
        this(bot, client,bulletId, 505, 432, 573, 286, 0, shotRequest, weaponId, shotMetric);
    }

    @Override
    public void send(int rid) {

        if(bot.getStrategy() instanceof IRoomNaturalBotStrategy && !StringUtils.isTrimmedEmpty(shotMetric)) {
            long timeMillis = System.currentTimeMillis();
            getLogger().debug("send: bullet request was made by bot: {}, shotMetric: {}, timeMillis: {}",
                    bot.getId(), shotMetric, toHumanReadableFormat(timeMillis));
            ((IRoomNaturalBotStrategy) bot.getStrategy()).updateBulletRequestTimeMetric(shotMetric, timeMillis);
        }

        long now = System.currentTimeMillis();
        client.sendMessage(new Bullet(now, rid, now, bulletAngle, bulletId,  startPointX, startPointY, endPointX, endPointY, weaponId));
    }

    @Override
    public void handle(ITransportObject response) {

        String className = response.getClassName();
        int rid = response.getRid();

        ManagedBattleGroundRoomBot managedBattleGroundRoomBot = null;
        if(bot instanceof ManagedBattleGroundRoomBot) {
            managedBattleGroundRoomBot = (ManagedBattleGroundRoomBot)bot;
        }

        if(shotRequest == null) {//if shot is not present update metric over BulletRequest, otherwise update metric over ShotRequest
            bot.setLastReceivedServerTime(response.getDate());

            if (bot.getStrategy() instanceof IRoomNaturalBotStrategy && !StringUtils.isTrimmedEmpty(shotMetric)) {
                long timeMillis = System.currentTimeMillis();
                getLogger().debug("handle: bullet response was received by bot: {}, shotMetric: {}, timeMillis: {}",
                        bot.getId(), shotMetric, toHumanReadableFormat(timeMillis));
                ((IRoomNaturalBotStrategy) bot.getStrategy()).updateBulletResponseTimeMetric(shotMetric, timeMillis);
            }
        }

        switch (className) {
            case "BulletResponse":
                break;
            case "Error":
                if(managedBattleGroundRoomBot != null) {
                    managedBattleGroundRoomBot.removeRicochetBulletByRid(rid);
                }
                bot.count(Stats.ERRORS);
                getLogger().error("BulletRequest: error: {}", response);
                break;
            default:
                getLogger().error("MinesRequest: unexpected response type: {}", response);
                break;
        }

        getLogger().debug("handle: {}, send if not null shotRequest={}", response, shotRequest);

        if(shotRequest != null) {
            bot.send(shotRequest);
        }

        boolean needProcess = response.getRid() != -1;
        if(needProcess) {
            BotState botState = bot.getState();
            if(botState == BotState.WAITING_FOR_RESPONSE){
                bot.setState(BotState.PLAYING, "BulletRequest: " + className);
            }
            bot.doActionWithSleep("BulletRequest[" + className + "]");
        }
    }

    @Override
    public String toString() {
        return "BulletRequest[" +
                "bot.id=" + bot.getId() +
                ", bulletId='" + bulletId + '\'' +
                ", weaponId=" + weaponId +
                ", startPointX=" + startPointX +
                ", startPointY=" + startPointY +
                ", endPointX=" + endPointX +
                ", endPointY=" + endPointY +
                ", bulletAngle=" + bulletAngle +
                ", shotRequest=" + shotRequest +
                ']';
    }
}
