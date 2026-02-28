package com.abs.casino.services.mp;

import com.abs.casino.ats.BotConfigInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.persistance.remotecall.KafkaRequestMultiPlayer;
import com.abs.casino.kafka.dto.BotConfigInfoDto;
import com.abs.casino.util.BotConfigInfoUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MPBotConfigInfoService {
    private static final Logger LOG = LogManager.getLogger(MPBotConfigInfoService.class);

    private final KafkaRequestMultiPlayer kafkaRequestHelper;

    public MPBotConfigInfoService(KafkaRequestMultiPlayer kafkaRequestHelper) {
        this.kafkaRequestHelper = kafkaRequestHelper;
    }

    public void enableBotService(boolean enable) {
        LOG.debug("enableBotService: enable={}", enable);
        try {
            kafkaRequestHelper.enableBotService(enable);
        } catch (Exception e) {
            LOG.error("enableBotService: Error", e);
        }
    }

    public boolean isBotServiceEnabled() {

        boolean isBotServiceEnabled = false;

        try {
            isBotServiceEnabled = kafkaRequestHelper.isBotServiceEnabled();
        } catch (Exception e) {
            LOG.error("isBotServiceEnabled: Error", e);
        }

        return isBotServiceEnabled;
    }

    public static List<BotConfigInfo> fromTBotConfigInfos(List<BotConfigInfoDto> tBotConfigInfos) {
        if (tBotConfigInfos == null) {
            return null;
        }
        List<BotConfigInfo> botConfigInfos = new ArrayList<>();
        for (BotConfigInfoDto tBotConfigInfo : tBotConfigInfos) {
            if(tBotConfigInfo != null) {
                BotConfigInfo botConfigInfo = BotConfigInfoUtil.fromTBotConfigInfo(tBotConfigInfo);
                if(botConfigInfo != null) {
                    botConfigInfos.add(botConfigInfo);
                }
            }
        }

        return botConfigInfos;
    }

    public List<BotConfigInfo> getAllBotConfigInfos() {

        List<BotConfigInfoDto> allBotConfigInfos = null;

        try {
            allBotConfigInfos = kafkaRequestHelper.getAllBotConfigInfos();
        } catch (Exception e) {
            LOG.error("getAllBotConfigInfos: Error", e);
        }

        LOG.debug("getAllBotConfigInfos: allBotConfigInfos.size()={}",
                allBotConfigInfos == null ? null : allBotConfigInfos.size());

        return fromTBotConfigInfos(allBotConfigInfos);
    }

    public BotConfigInfo getBotConfigInfo(long botId) {

        LOG.debug("getBotConfigInfo: botId={}", botId);

        if(botId < 0) {
            LOG.error("getBotConfigInfo: botId is negative, skip");
            return null;
        }

        BotConfigInfoDto tBotConfigInfo = null;

        try {
            tBotConfigInfo = kafkaRequestHelper.getBotConfigInfo(botId);
        } catch (Exception e) {
            LOG.error("getBotConfigInfo: Error", e);
        }

        LOG.debug("getBotConfigInfo: botId={}, tBotConfigInfo={}", botId, tBotConfigInfo);

        BotConfigInfo botConfigInfo = BotConfigInfoUtil.fromTBotConfigInfo(tBotConfigInfo);
        LOG.debug("getBotConfigInfo: botId={}, botConfigInfo={}", botId, botConfigInfo);

        return botConfigInfo;
    }

    public BotConfigInfo getBotConfigInfoByUserName(String username) {
        LOG.debug("getBotConfigInfoByUserName: username={}", username);

        if(StringUtils.isTrimmedEmpty(username)) {
            LOG.error("getBotConfigInfoByUserName: username is empty, skip");
            return null;
        }

        BotConfigInfoDto tBotConfigInfo = null;

        try {
            tBotConfigInfo = kafkaRequestHelper.getBotConfigInfoByUserName(username);
        } catch (Exception e) {
            LOG.error("getBotConfigInfoByUserName: Error", e);
        }

        LOG.debug("getBotConfigInfoByUserName: username={}, tBotConfigInfo={}", username, tBotConfigInfo);

        BotConfigInfo botConfigInfo = BotConfigInfoUtil.fromTBotConfigInfo(tBotConfigInfo);
        LOG.debug("getBotConfigInfoByUserName: username={}, botConfigInfo={}", username, botConfigInfo);

        return botConfigInfo;
    }

    public BotConfigInfo getBotConfigInfoByMqNickName(String mqNickname) {
        LOG.debug("getBotConfigInfoByMqNickName: mqNickname={}", mqNickname);

        if(StringUtils.isTrimmedEmpty(mqNickname)) {
            LOG.error("getBotConfigInfoByMqNickName: mqNickname is empty, skip");
            return null;
        }

        BotConfigInfoDto tBotConfigInfo = null;

        try {
            tBotConfigInfo = kafkaRequestHelper.getBotConfigInfoByMqNickName(mqNickname);
        } catch (Exception e) {
            LOG.error("getBotConfigInfoByMqNickName: Error", e);
        }

        LOG.debug("getBotConfigInfoByMqNickName: mqNickname={}, tBotConfigInfo={}", mqNickname, tBotConfigInfo);

        BotConfigInfo botConfigInfo = BotConfigInfoUtil.fromTBotConfigInfo(tBotConfigInfo);
        LOG.debug("getBotConfigInfoByMqNickName: mqNickname={}, botConfigInfo={}", mqNickname, botConfigInfo);

        return botConfigInfo;
    }

    public BotConfigInfo upsertBotConfigInfo(BotConfigInfo botConfigInfo) {

        LOG.debug("upsertBotConfigInfo: botConfigInfo={}", botConfigInfo);

        if(botConfigInfo == null) {
            LOG.error("upsertBotConfigInfo: botConfigInfo is null, skip");
            return null;
        }

        BotConfigInfoDto tBotConfigInfo = BotConfigInfoUtil.toTBotConfigInfo(botConfigInfo);
        LOG.debug("upsertBotConfigInfo: tBotConfigInfo={}", tBotConfigInfo);

        try {
            List<BotConfigInfoDto> tBotConfigInfos = new ArrayList<>();
            tBotConfigInfos.add(tBotConfigInfo);

            List<BotConfigInfoDto> tBotConfigInfosReturned =
                    kafkaRequestHelper.upsertBotConfigInfo(tBotConfigInfos);

            BotConfigInfo botConfigInfoReturned = null;
            if (tBotConfigInfosReturned != null && !tBotConfigInfosReturned.isEmpty()) {
                botConfigInfoReturned = BotConfigInfoUtil.fromTBotConfigInfo(tBotConfigInfosReturned.get(0));
            }

            LOG.debug("upsertBotConfigInfo: Successfully upsert botConfigInfoReturned:{}", botConfigInfoReturned);

            return botConfigInfoReturned;

        } catch (Exception e) {
            LOG.error("upsertBotConfigInfo: Error", e);
        }

        return null;
    }

    public BotConfigInfo removeBotConfigInfo(Long botId) {
        LOG.debug("removeBotConfigInfo: botId={}", botId);

        if (botId < 0) {
            LOG.error("removeBotConfigInfo: botId is negative, skip");
        }

        try {
            List<Long> botIds = new ArrayList<>();
            botIds.add(botId);

            List<BotConfigInfoDto> tBotConfigInfosReturned =
                    kafkaRequestHelper.removeBotConfigInfo(botIds);

            BotConfigInfo botConfigInfoReturned = null;
            if (tBotConfigInfosReturned != null && !tBotConfigInfosReturned.isEmpty()) {
                botConfigInfoReturned = BotConfigInfoUtil.fromTBotConfigInfo(tBotConfigInfosReturned.get(0));
            }

            LOG.debug("removeBotConfigInfo: Successfully removed botConfigInfoReturned:{}", botConfigInfoReturned);

            return botConfigInfoReturned;

        } catch (Exception e) {
            LOG.error("removeBotConfigInfo: Error", e);
        }

        return null;
    }
}
