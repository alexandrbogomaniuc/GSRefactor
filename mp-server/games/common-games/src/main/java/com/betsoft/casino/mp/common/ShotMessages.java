package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.utils.ITransportObject;
import com.dgphoenix.casino.common.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: flsh
 * Date: 14.02.19.
 */
public class ShotMessages {
    private static final Logger LOG = LogManager.getLogger(ShotMessages.class);
    private final List<ITransportObject> ownMessages = new ArrayList<>();
    private final List<ITransportObject> allMessages = new ArrayList<>();
    private final List<ITransportObject> allMessagesForObserversOnly = new ArrayList<>();

    private final List<Pair<ITransportObject, ITransportObject>> messages = new ArrayList<>();

    private final List<Pair<ISeat, ITransportObject>> messageForSeats = new ArrayList<>();

    private final IShotResponse ownResponse;
    private final IShotResponse allResponse;

    private final IActionGameSeat seat;
    private final IRoom room;


    public ShotMessages(IActionGameSeat seat, IShot shot, IRoom room, IShotResponse ownResponse, IShotResponse allResponse) {
        this.seat = seat;
        this.room = room;
        this.ownResponse = ownResponse;
        this.allResponse = allResponse;
    }

    public void addMessageForSeat(ISeat seat, ITransportObject message) {
        messageForSeats.add(new Pair<>(seat, message));
    }


    public void addOwnMessage(ITransportObject message) {
        ownMessages.add(message);
    }

    public void addAllMessage(ITransportObject message) {
        allMessages.add(message);
    }

    public void add(ITransportObject allMessage, ITransportObject ownMessage) {
        messages.add(new Pair<>(allMessage, ownMessage));
    }

    public void addMessageForRealObservers(ITransportObject allMessage) {
        allMessagesForObserversOnly.add(allMessage);
        LOG.debug("allMessagesForObserversOnly.size(): {}", allMessagesForObserversOnly.size());
    }

    public void send(int remainingSWShots, IShot shot) {
        for (ITransportObject message : ownMessages) {
            seat.sendMessage(message, shot);
        }

        for (ITransportObject message : allMessages) {
            room.sendChanges(message);
        }

        for (Pair<ITransportObject, ITransportObject> message : messages) {
            room.sendChanges(message.getKey(), message.getValue(), seat.getAccountId(), shot);
        }

        for (Pair<ISeat, ITransportObject> messageForSeat : messageForSeats) {
            messageForSeat.getKey().sendMessage(messageForSeat.getValue());
        }

        for (ITransportObject message : allMessagesForObserversOnly) {
            room.sendChangesToObserversOnly(message);
        }

    }

    public void send(IShot shot) {

        List<ITransportObject> lastOwnMessages = new ArrayList<>();
        for (ITransportObject message : ownMessages) {
            if (message instanceof IShotResult) {
                ((IShotResult) message).setServerAmmo(seat.getAmmoAmount());
            }
            if (message instanceof IHit) {
                if (((IHit) message).isLastResult()) {
                    lastOwnMessages.add(message);
                } else {
                    seat.sendMessage(message, shot);
                }
            } else {
                seat.sendMessage(message, shot);
            }
        }


        for (ITransportObject message : allMessages) {
            room.sendChanges(message);
        }

        List<Pair<ITransportObject, ITransportObject>> lastMessages = new ArrayList<>();
        for (Pair<ITransportObject, ITransportObject> message : messages) {
            if (message.getKey() instanceof IShotResult) {
                ((IShotResult) message.getValue()).setServerAmmo(seat.getAmmoAmount());
                if (((IShotResult) message.getKey()).isLastResult()) {
                    lastMessages.add(message);
                } else {
                    room.sendChanges(message.getKey(), message.getValue(), seat.getAccountId(), shot);
                }
            } else {
                room.sendChanges(message.getKey(), message.getValue(), seat.getAccountId(), shot);
            }
        }

        List<Pair<ISeat, ITransportObject>> lastMessagesForSeats = new ArrayList<>();
        for (Pair<ISeat, ITransportObject> messageForSeat : messageForSeats) {
            ISeat key = messageForSeat.getKey();
            ITransportObject message = messageForSeat.getValue();
            if (message instanceof IShotResult) {
                if (((IShotResult) message).isLastResult()) {
                    lastMessagesForSeats.add(messageForSeat);
                } else {
                    sendMessageToSeat(key, message, room);
                }
            } else {
                sendMessageToSeat(key, message, room);
            }
        }


        LOG.debug("lastOwnMessages: {}", lastOwnMessages);
        for (ITransportObject lastOwnMessage : lastOwnMessages) {
            seat.sendMessage(lastOwnMessage, shot);
        }

        LOG.debug("lastMessages: {}", lastMessages);
        for (Pair<ITransportObject, ITransportObject> message : lastMessages) {
            room.sendChanges(message.getKey(), message.getValue(), seat.getAccountId(), shot);
        }

        for (Pair<ISeat, ITransportObject> lastMessagesForSeat : lastMessagesForSeats) {
            sendMessageToSeat(lastMessagesForSeat.getKey(), lastMessagesForSeat.getValue(), room);
        }

        for (ITransportObject message : allMessagesForObserversOnly) {
            room.sendChangesToObserversOnly(message);
        }

    }

    private void sendMessageToSeat(ISeat seat, ITransportObject message, IRoom room) {
        if (!seat.isDisconnected()) {
            seat.sendMessage(message);
        } else {
            LOG.debug("seat {} is disconnected, try send via observers ", seat.getAccountId());
            room.sendMessageToPlayer(message, seat.getAccountId());
        }
    }
}
