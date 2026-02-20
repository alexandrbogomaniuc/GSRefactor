package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IRoomTemplateService;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.MoneyType;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import java.util.Collection;

public class StubRoomTemplateService implements IRoomTemplateService<StubRoomTemplate> {
    private static final Logger LOG = LogManager.getLogger(StubRoomTemplateService.class);
    private IMap<Long, StubRoomTemplate> templates;

    public StubRoomTemplateService() {
    }

    @PostConstruct
    private void init() {
        templates.addIndex("id", true);
        templates.addIndex("bankId", false);
        templates.addIndex("moneyType", false);
        templates.addIndex("battlegroundMode", false);
        LOG.info("init: completed");
    }

    @Override
    public StubRoomTemplate put(StubRoomTemplate template) {
        LOG.debug("put: {}", template);
        return templates.put(template.getId(), template);
    }

    @Override
    public void remove(Long id) {
        templates.delete(id);
    }

    @Override
    public Collection<StubRoomTemplate> getAll() {
        return templates.values();
    }

    @Override
    public StubRoomTemplate get(Long id) {
        return templates.get(id);
    }

    @Override
    public StubRoomTemplate getForBankOrDefault(long bankId, GameType gameType, MoneyType moneyType,
                                                boolean battlegroundMode) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        Predicate predicate = object.get("bankId").equal(bankId)
                .and(battlegroundMode ? object.is("battlegroundMode") : object.isNot("battlegroundMode"));
        Collection<StubRoomTemplate> bankTemplates = templates.values(predicate);
        if (bankTemplates.isEmpty()) { //
            bankTemplates = getDefault(moneyType);
        }
        StubRoomTemplate result = null;
        for (StubRoomTemplate template : bankTemplates) {
            if (template.getGameType().equals(gameType) && template.getMoneyType().equals(moneyType)) {
                result = template;
                break;
            }
        }
        return result;
    }

    @Override
    public Collection<StubRoomTemplate> getForBankOrDefault(Long bankId, MoneyType moneyType) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        Predicate predicate = object.get("bankId").equal(bankId != null ? bankId : StubRoomTemplate.DEFAULT_BANK_ID).
                and(object.get("moneyType").equal(moneyType));
        Collection<StubRoomTemplate> result = templates.values(predicate);
        if (result.isEmpty() && bankId != null) { //
            result = getDefault(moneyType);
        }
        return result;
    }

    @Override
    public StubRoomTemplate getMostSuitable(Long bankId, Money stake, MoneyType moneyType, GameType gameType) {
        return getForBankOrDefault(bankId, gameType, moneyType, false);
    }

    @Override
    public Collection<StubRoomTemplate> getDefault(MoneyType moneyType) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        Predicate predicate = object.get("bankId").equal(StubRoomTemplate.DEFAULT_BANK_ID).
                and(object.get("moneyType").equal(moneyType));
        return templates.values(predicate);
    }
}
