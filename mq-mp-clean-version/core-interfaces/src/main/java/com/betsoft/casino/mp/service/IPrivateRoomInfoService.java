package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IPrivateRoomPlayersStatusService;
import com.betsoft.casino.mp.model.privateroom.PrivateRoom;
import com.betsoft.casino.mp.model.privateroom.UpdatePrivateRoomResponse;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import org.apache.commons.codec.binary.Base64;

public interface IPrivateRoomInfoService {
    byte[] SECRET_KEY =
            ("mw1o61em0or4dk9!2g4t6ee1h16jeafiiv^*t2@2!dvez*i#2r" +
                    "7y1&1fz5xmcoex1m@9g588f*ixe75h7@87r2vm^27tj1&obbxw" +
                    "7l8c$gr5dfy5om2s05532^%q9fhcru8vb%zjn0h#pxb3x3l$w%" +
                    "cw*5uow*3z0$!fg&313g8qfo%z1po@sl3n7s6cc0i3a55qmy8t" +
                    "9e5mtf7km847j@aq#$60xkry7wkt$a8pn%q7kpnerm!3mq!k!t").getBytes();

    default String buildJoinUrl(String domainUrl, String privateRoomId) {
        return domainUrl + "battleground/startPrivateRoom?privateRoomId=" + privateRoomId;
    }

    default String generatePrivateRoomId(String value) {
        return encodeToXOR(value);
    }

    default String encodeToXOR(String value) {
        byte[] input = value.getBytes();
        byte[] out = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = (byte) (input[i] ^ (i >= SECRET_KEY.length ? 0xb : SECRET_KEY[i]));
        }
        return Base64.encodeBase64URLSafeString(out);
    }

    default IPrivateRoomPlayersStatusService getPrivateRoomPlayersStatusService() {return null;}

    UpdatePrivateRoomResponse updatePlayersStatusInPrivateRoom(PrivateRoom privateRoom,
                                                               boolean isTransitionLimited, boolean updateTime);

    PrivateRoom getPlayersStatusInPrivateRoom(String privateRoomId);

    IRoomInfo getRoomByPrivateRoomId(String privateRoomId);
}
