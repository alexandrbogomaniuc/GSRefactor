package com.abs.casino.promo.events.process;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.promo.DesiredPrize;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.PromoCampaignMember;
import com.dgphoenix.casino.common.promo.PromoNotificationType;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 6/7/22
 */
public class EventProcessorContext {

    private final IPromoCampaign campaign;
    private final PromoCampaignMember member;
    private final AccountInfo account;
    private final Set<PromoNotificationType> notifications;
    private final Set<DesiredPrize> newDesiredPrizes;
    private final Set<DesiredPrize> replacedPrizes;

    public EventProcessorContext(IPromoCampaign campaign, PromoCampaignMember member, AccountInfo account) {
        this.campaign = campaign;
        this.member = member;
        this.account = account;
        notifications = EnumSet.noneOf(PromoNotificationType.class);
        newDesiredPrizes = new HashSet<>();
        replacedPrizes = new HashSet<>();
    }

    public void replaceDesiredPrize(DesiredPrize desiredPrize, DesiredPrize newDesiredPrize) {
        replaceDesiredPrize(desiredPrize, newDesiredPrize, false);
    }

    public void replaceDesiredPrize(DesiredPrize desiredPrize, DesiredPrize newDesiredPrize, boolean withRemove) {
        replacedPrizes.add(desiredPrize);
        if (withRemove) {
            newDesiredPrizes.remove(desiredPrize);
        }
        newDesiredPrizes.add(newDesiredPrize);
    }

    public void updateMemberPrizeList() {
        member.removeDesiredPrizes(replacedPrizes);
        member.addDesiredPrizes(newDesiredPrizes);
    }

    public String getCurrencyCode() {
        return account.getCurrency().getCode();
    }

    public IPromoCampaign getCampaign() {
        return campaign;
    }

    public PromoCampaignMember getMember() {
        return member;
    }

    public AccountInfo getAccount() {
        return account;
    }

    public Set<PromoNotificationType> getNotifications() {
        return notifications;
    }

    public void addNotification(PromoNotificationType notificationType) {
        notifications.add(notificationType);
    }

    public Set<DesiredPrize> getNewDesiredPrizes() {
        return newDesiredPrizes;
    }

    public Set<DesiredPrize> getReplacedPrizes() {
        return replacedPrizes;
    }
}
