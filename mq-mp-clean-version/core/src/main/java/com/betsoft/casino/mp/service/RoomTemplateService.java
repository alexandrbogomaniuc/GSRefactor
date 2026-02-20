package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.*;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * User: flsh
 * Date: 05.04.18.
 */
@Service
public class RoomTemplateService implements IRoomTemplateService<RoomTemplate> {
    private static final Logger LOG = LogManager.getLogger(RoomTemplateService.class);
    public static final String ROOM_TEMPLATE_STORE = "roomTemplateStore";
    private final HazelcastInstance hazelcast;
    private final CurrencyRateService currencyRateService;
    private IMap<Long, RoomTemplate> templates;

    public RoomTemplateService(HazelcastInstance hazelcast, CurrencyRateService currencyRateService) {
        this.hazelcast = hazelcast;
        this.currencyRateService = currencyRateService;
    }

    @PostConstruct
    private void init() {
        templates = hazelcast.getMap(ROOM_TEMPLATE_STORE);
        templates.addIndex("id", true);
        templates.addIndex("bankId", false);
        templates.addIndex("moneyType", false);
        templates.addIndex("gameType", false);
        templates.addIndex("battlegroundMode", false);
        templates.addIndex("privateRoom", false);
        LOG.info("init: completed");
    }

    @Override
    public RoomTemplate put(RoomTemplate template) {
        LOG.debug("put: {}", template);
        return templates.put(template.getId(), template);
    }

    @Override
    public void remove(Long id) {
        templates.delete(id);
    }

    @Override
    public Collection<RoomTemplate> getAll() {
        return templates.values();
    }

    @Override
    public RoomTemplate get(Long id) {
        return templates.get(id);
    }


    @Override
    public RoomTemplate getForBankOrDefault(long bankId, GameType gameType, MoneyType moneyType,
                                            boolean battlegroundMode) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        Predicate predicate = object.get("bankId").equal(bankId)
                .and(object.get("moneyType").equal(moneyType))
                .and(object.get("gameType").equal(gameType))
                .and(battlegroundMode ? object.is("battlegroundMode") : object.isNot("battlegroundMode"));

        Collection<RoomTemplate> bankTemplates = templates.values(predicate);
        if (bankTemplates.isEmpty()) { //
            bankTemplates = getDefault(moneyType);
        }
        RoomTemplate result = null;
        for (RoomTemplate template : bankTemplates) {
            if (template.getGameType().equals(gameType) && template.getMoneyType().equals(moneyType)) {
                result = template;
                break;
            }
        }
        return result;
    }

    @Override
    public Collection<RoomTemplate> getForBankOrDefault(Long bankId, MoneyType moneyType) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        Predicate predicate = object.get("bankId").equal(bankId != null ? bankId : RoomTemplate.DEFAULT_BANK_ID).
                and(object.get("moneyType").equal(moneyType));
        Collection<RoomTemplate> result = templates.values(predicate);
        if (result.isEmpty() && bankId != null) { //
            result = getDefault(moneyType);
        }
        return result;
    }

    @Override
    public RoomTemplate getMostSuitable(Long bankId, Money stake, MoneyType moneyType, GameType gameType) {
        return getForBankOrDefault(bankId, gameType, moneyType, false);
    }

    @Override
    public Collection<RoomTemplate> getDefault(MoneyType moneyType) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        Predicate predicate = object.get("bankId").equal(RoomTemplate.DEFAULT_BANK_ID).
                and(object.get("moneyType").equal(moneyType));
        return templates.values(predicate);
    }

    public RoomTemplate getPrivateTemplate(long bankId, MoneyType moneyType, GameType gameType, long minBuyIn) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        Predicate predicate = object.get("bankId").equal(bankId)
                .and(object.get("moneyType").equal(moneyType))
                .and(object.get("gameType").equal(gameType))
                .and(object.is("privateRoom"))
                .and(object.is("battlegroundMode"));
        Collection<RoomTemplate> filtered = templates.values(predicate);
        RoomTemplate result = null;
        for (RoomTemplate roomTemplate : filtered) {
            if (roomTemplate.getBattlegroundBuyIn() == minBuyIn) {
                result = roomTemplate;
                break;
            }
        }
        return result;
    }
}
