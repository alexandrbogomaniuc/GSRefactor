package com.abs.casino.common.promo.messages.server.notifications;

import com.dgphoenix.casino.common.promo.Status;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;

/**
 * Created by vladislav on 1/24/17.
 */
public class PromoStatusChanged extends PromoNotification {
    private static final String STATUS_CHANGED_NOTIFICATION_PREFIX = "status_changed";

    private final Status newStatus;

    public PromoStatusChanged(long promoId, Status newStatus) {
        Status newStatusForClient = newStatus != Status.QUALIFICATION //clients do not support QUALIFICATION status yet
                ? newStatus
                : Status.FINISHED;
        String id = composeId(STATUS_CHANGED_NOTIFICATION_PREFIX, promoId, newStatusForClient);
        super.setId(id);
        super.setPromoId(promoId);
        this.newStatus = newStatusForClient;
    }

    @Override
    public String httpFormat() {
        return ServerMessage.FIELDS_JOINER.join(super.httpFormat(), newStatus);
    }

    @Override
    public String toString() {
        return "PromoStatusChanged{" + super.toString() +
                ", newStatus='" + newStatus + '\'' +
                "}";
    }
}
