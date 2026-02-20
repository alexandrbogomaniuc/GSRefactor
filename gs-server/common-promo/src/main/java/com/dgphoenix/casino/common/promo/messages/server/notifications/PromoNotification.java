package com.dgphoenix.casino.common.promo.messages.server.notifications;

import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerMessage;

/**
 * Created by vladislav on 12/19/16.
 */
public abstract class PromoNotification extends ServerMessage {
    private long promoId;

    public long getPromoId() {
        return promoId;
    }

    protected void setPromoId(long promoId) {
        this.promoId = promoId;
    }

    @Override
    public String httpFormat() {
        return ServerMessage.FIELDS_JOINER.join(super.getId(), promoId, this.getClass().getSimpleName());
    }

    public String oldHttpFormat() {
        return ServerMessage.FIELDS_JOINER.join(promoId, this.getClass().getSimpleName());
    }

    @Override
    public String toString() {
        return super.toString() + ", promoId=" + promoId;
    }
}
