package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.common.AbstractActionGameRoom;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.exceptions.BuyInFailedException;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.PurchaseWeaponLootBox;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import com.dgphoenix.casino.common.exception.CommonException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.concurrent.ForkJoinPool;

import static com.betsoft.casino.mp.utils.ErrorCodes.NOT_SEATER;

@SuppressWarnings("unchecked")
@Component
public class PurchaseWeaponLootBoxHandler extends AbstractRoomHandler<PurchaseWeaponLootBox, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(PurchaseWeaponLootBoxHandler.class);

    private final SocketService socketService;
    private final ForkJoinPool fjPool = new ForkJoinPool();
    protected final LobbySessionService lobbySessionService;

    public PurchaseWeaponLootBoxHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                        MultiNodeRoomInfoService multiNodeRoomInfoService,
                                        RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                                        ServerConfigService serverConfigService, SocketService socketService,
                                        LobbySessionService lobbySessionService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.socketService = socketService;
        this.lobbySessionService = lobbySessionService;
    }

    @Override
    public void handle(WebSocketSession session, PurchaseWeaponLootBox message, IGameSocketClient client) {
        fjPool.execute(() -> {
            _handle(session, message, client);
        });
    }

    @SuppressWarnings("rawtypes")
    private void _handle(WebSocketSession session, PurchaseWeaponLootBox message, IGameSocketClient client) {
        Long accountId = client.getAccountId();
        if (client.getRoomId() == null || client.getSeatNumber() < 0 || accountId == null) {
            sendErrorMessage(client, NOT_SEATER, "Not seat", message.getRid());
            return;
        }

        if (hasPendingOperations(accountId, client, message)) {
            return;
        }
        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession == null) {
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }
        if (lobbySession.getActiveFrbSession() != null || lobbySession.getActiveCashBonusSession() != null) {
            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Not allowed for bonus mode", message.getRid());
            return;
        }

        if (!client.getGameType().isSupportLootBox()) {
            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Not allowed for for this game", message.getRid());
            return;
        }

        try {
            AbstractActionGameRoom room = getActionRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                playerInfoService.lock(accountId);
                getLog().debug("_handle HS lock: {}", accountId);
                try {
                    //need double check
                    if (hasPendingOperations(accountId, client, message)) {
                        return;
                    }
                    if (room.isBattlegroundMode()) {
                        sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Not allowed for battleground mode",
                                message.getRid());
                        return;
                    }
                    IActionGameSeat seat = (IActionGameSeat) room.getSeat(client.getSeatNumber());
                    IActionRoomPlayerInfo playerInfo = (IActionRoomPlayerInfo) seat.getPlayerInfo();
                    if (seat.getAccountId() != accountId) {
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());
                    } else if (!room.isBuyInAllowed(seat)) {
                        sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "BuyIn not allowed at current game state",
                                message.getRid());
                    } else if (playerInfo.getWeaponMode().equals(MaxQuestWeaponMode.PAID_SHOTS)) {
                        sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "BuyIn not allowed for this mode ",
                                message.getRid());
                    } else {

                        Money price = (Money) room.getWeaponLootBoxPrices(seat).get(message.getBox());
                        ITournamentSession tournamentSession = playerInfo.getTournamentSession();
                        if (tournamentSession != null) {
                            purchaseInTournament(room, seat, price, client, message, playerInfo);
                        } else {
                            regularPurchase(room, seat, price, client, lobbySession, message, playerInfo);
                        }
                    }
                } finally {
                    playerInfoService.unlock(accountId);
                    getLog().debug("_handle HS unlock: {}", accountId);
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void purchaseInTournament(AbstractActionGameRoom room, IActionGameSeat seat, Money price, IGameSocketClient client,
                                      PurchaseWeaponLootBox message, IRoomPlayerInfo playerInfo) {
        LOG.debug("Before :: AID={}, ammoAmount={}", seat.getAccountId(), seat.getAmmoAmount());
        purchaseFromAmmo(room, seat, price, client, message);
        playerInfoService.put(playerInfo);
        seat.setPlayerInfo(playerInfo);
        LOG.debug("After :: AID={}, ammoAmount={}", seat.getAccountId(), seat.getAmmoAmount());
    }

    @SuppressWarnings("rawtypes")
    private void regularPurchase(AbstractActionGameRoom room, IActionGameSeat seat, Money price, IGameSocketClient client, LobbySession lobbySession,
                                 PurchaseWeaponLootBox message, IRoomPlayerInfo playerInfo) throws Exception {
        Money balance = Money.fromCents(lobbySession.getBalance());
        long stakesReserve = playerInfo.getStakesReserve();
        if (stakesReserve <= 0) { //temp fix for old code compatibility, remove after deploy to All systems
            stakesReserve = IRoom.DEFAULT_STAKES_RESERVE;
        }
        Money reserveAmount = seat.getStake().multiply(stakesReserve);
        Money ammoAmountBalance = seat.getStake().multiply(seat.getAmmoAmount());

        LOG.debug("Before :: AID={}, balance={}, price={}, roundWin={}, stakesReserve={}, " +
                        "reserveAmount={} , ammoAmountBalance={}", seat.getAccountId(), lobbySession.getBalance(),
                price.toCents(), seat.getRoundWin(), stakesReserve, reserveAmount.toCents(),
                ammoAmountBalance.toCents());
        try {
            playerInfo.setPendingOperation(true, "Purchase weaponLootBox, rId=" + message.getRid());
            playerInfoService.put(playerInfo);
            if (reserveAmount.toCents() + price.toCents() < ammoAmountBalance.toCents()) {
                purchaseFromAmmo(room, seat, price, client, message);
            } else if (balance.toCents() >= price.toCents()) {
                purchaseFromBalance(room, seat, price, client, message, playerInfo);
            } else if (balance.toCents() + ammoAmountBalance.toCents() >= price.toCents()) {
                purchaseFromBalanceAndAmmo(room, seat, price, balance, client, message, playerInfo);
            } else {
                sendErrorMessage(client, ErrorCodes.NOT_ENOUGH_MONEY, message.getRid(), message);
            }
            playerInfo.setPendingOperation(false);
            playerInfoService.put(playerInfo);
            seat.setPlayerInfo(playerInfo);
            lobbySession = lobbySessionService.get(client.getSessionId());
            LOG.debug("PurchaseWeaponLootBoxHandler update lobbySocketClientPlayerInfo" +
                    " :: AID={}, balance={}", seat.getAccountId(), lobbySession.getBalance());
        } catch (BuyInFailedException e) {
            playerInfo.setPendingOperation(false);
            playerInfoService.put(playerInfo);
            if (e.getErrorCode() > 0) {
                sendErrorMessage(client, ErrorCodes.translateGameServerErrorCode(e.getErrorCode()),
                        "Purchase weapon failed, reason: " + e.getMessage(), message.getRid());
            } else {
                sendErrorMessage(client, e.isFatal() ? ErrorCodes.BAD_BUYIN : ErrorCodes.NOT_FATAL_BAD_BUYIN,
                        "Purchase weapon failed, reason: " + e.getMessage(), message.getRid());
            }
        } catch (Exception e) {
            playerInfo.setPendingOperation(false);
            playerInfoService.put(playerInfo);
            throw e; //show internal error
        }
        LOG.debug("After  :: AID={}, balance={}, price={}, roundWin={}", seat.getAccountId(),
                lobbySession.getBalance(), price, seat.getRoundWin());
    }

    @SuppressWarnings("rawtypes")
    private void purchaseFromBalance(AbstractActionGameRoom room, IActionGameSeat seat, Money price, IGameSocketClient client,
                                     PurchaseWeaponLootBox message, IRoomPlayerInfo playerInfo) throws Exception {
        BuyInResult buyInResult = socketService.buyIn(client.getServerId(), playerInfo.getId(),
                client.getSessionId(), price, playerInfo.getGameSessionId(),
                playerInfo.getRoomId(), playerInfo.getBuyInCount(), null, room);
        if (buyInResult.getAmount() > 0) {
            playerInfo.incrementBuyInCount();
        }
        finishBuyIn(room, seat, message, playerInfo, buyInResult, 0);
    }

    @SuppressWarnings("rawtypes")
    private void purchaseFromBalanceAndAmmo(AbstractActionGameRoom room, IActionGameSeat seat, Money price, Money balance,
                                            IGameSocketClient client, PurchaseWeaponLootBox message,
                                            IRoomPlayerInfo playerInfo) throws Exception {
        Money stake = seat.getStake();
        long balanceInStakes = balance.divideBy(stake);
        Money reducedBalancePart = stake.multiply(balanceInStakes);
        if (reducedBalancePart.toCents() > price.toCents()) {
            throw new CommonException("Impossible error, buyIn amount greater than balance");
        }
        Money ammoPart = price.subtract(reducedBalancePart);
        int ammo = (int) ammoPart.divideBy(stake);
        LOG.debug("purchaseFromBalanceAndAmmo: ammo={}, seat.getAmmoAmount={}, ammoPart[cents]={}, " +
                        "balanceInStakes={}, reducedBalancePart[cents]={}", ammo, seat.getAmmoAmount(),
                ammoPart.toCents(),
                balanceInStakes, reducedBalancePart.toCents());
        if (ammo > seat.getAmmoAmount()) {
            sendErrorMessage(client, ErrorCodes.NOT_ENOUGH_MONEY, "Not enough money", message.getRid());
        } else {
            seat.decrementAmmoAmount(ammo);
            if (reducedBalancePart.greaterThan(Money.ZERO)) {
                BuyInResult buyInResult;
                try {
                    buyInResult = socketService.buyIn(client.getServerId(), playerInfo.getId(),
                            client.getSessionId(), reducedBalancePart, playerInfo.getGameSessionId(),
                            playerInfo.getRoomId(), playerInfo.getBuyInCount(), null, room);
                } catch (Exception e) {
                    LOG.error("purchaseFromBalanceAndAmmo: buyIn from balance failed, rollback buyIn from ammo={}",
                            ammo);
                    seat.incrementAmmoAmount(ammo);
                    throw e;
                }
                if (buyInResult.getAmount() > 0) {
                    playerInfo.incrementBuyInCount();
                }
                finishBuyIn(room, seat, message, playerInfo, buyInResult, ammo);
            } else {
                IWeaponLootBox lootBox = room.generateWeaponLootBox(seat, message.getRid(), message.getBox(), ammo,
                        Money.ZERO);
                if (lootBox != null) {
                    seat.sendMessage(lootBox);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void finishBuyIn(AbstractActionGameRoom room, IActionGameSeat seat, PurchaseWeaponLootBox message, IRoomPlayerInfo playerInfo,
                             BuyInResult buyInResult, int ammo) {
        LobbySession lobbySession = lobbySessionService.get(playerInfo.getSessionId());
        lobbySession.setBalance(buyInResult.getBalance());
        lobbySessionService.add(lobbySession);
        playerInfo.makeBuyIn(buyInResult.getPlayerRoundId(),
                buyInResult.getAmount());
        seat.updatePlayerRoundInfo(buyInResult.getPlayerRoundId());
        IWeaponLootBox weaponLootBox = room.generateWeaponLootBox(seat, message.getRid(), message.getBox(), ammo,
                Money.fromCents(buyInResult.getAmount()));
        if (weaponLootBox != null) {
            seat.sendMessage(weaponLootBox, message);
        }
    }

    @SuppressWarnings("rawtypes")
    private void purchaseFromAmmo(AbstractActionGameRoom room, IActionGameSeat seat, Money price, IGameSocketClient client,
                                  PurchaseWeaponLootBox message) {
        int ammoReduced = (int) price.divideBy(seat.getStake());
        if (ammoReduced <= seat.getAmmoAmount()) {
            seat.decrementAmmoAmount(ammoReduced);
            IWeaponLootBox lootBox = room.generateWeaponLootBox(seat, message.getRid(), message.getBox(), ammoReduced,
                    Money.ZERO);
            if (lootBox != null) {
                seat.sendMessage(lootBox);
            }
        } else {
            sendErrorMessage(client, ErrorCodes.NOT_ENOUGH_MONEY, "Not enough money", message.getRid());
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
