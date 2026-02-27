package com.abs.casino.common.promo.messages.server.notifications.tournament;

import com.dgphoenix.casino.common.promo.messages.server.notifications.PromoNotification;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;

/**
 * Created by vladislav on 3/9/17.
 */
public class TournamentPlace extends PromoNotification {
    private static final String TOURNAMENT_PLACE_NOTIFICATION_PREFIX = "tournament_place";

    private final String place;

    public TournamentPlace(long promoId, String place) {
        String id = composeId(TOURNAMENT_PLACE_NOTIFICATION_PREFIX, promoId, System.currentTimeMillis());
        super.setId(id);
        super.setPromoId(promoId);
        this.place = place;
    }

    @Override
    public String httpFormat() {
        return ServerMessage.FIELDS_JOINER.join(super.httpFormat(), place);
    }

    @Override
    public String toString() {
        return "TournamentPlace{" +
                super.toString() +
                ", place=" + place +
                "}";
    }
}
