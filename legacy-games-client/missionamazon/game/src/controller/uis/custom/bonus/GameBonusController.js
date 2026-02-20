import BonusController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/custom/bonus/BonusController';
import GameWebSocketInteractionController from '../../../interaction/server/GameWebSocketInteractionController';
import GameBonusInfo from '../../../../model/uis/custom/bonus/GameBonusInfo';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { CLIENT_MESSAGES, SERVER_MESSAGES } from '../../../../model/interaction/server/GameWebSocketInteractionInfo';
import { GAME_MESSAGES } from '../../../../external/GameExternalCommunicator';
import BonusInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/bonus/BonusInfo';
import GameScreen from '../../../../main/GameScreen';
import WebSocketInteractionController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/server/WebSocketInteractionController';
import Game from '../../../../Game';

class GameBonusController extends BonusController {

	static get EVENT_ON_BONUS_STATE_CHANGED() 					{ return BonusController.EVENT_ON_BONUS_STATE_CHANGED; }

	static get EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS() 	{ return "EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS"; }
	static get EVENT_ON_BONUS_SIT_OUT_REQUIRED() 				{ return "EVENT_ON_BONUS_SIT_OUT_REQUIRED"; }
	static get EVENT_ON_BONUS_BACK_TO_LOBBY_REQUIRED()			{ return "EVENT_ON_BONUS_BACK_TO_LOBBY_REQUIRED"; }

	constructor()
	{
		super(new GameBonusInfo());
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_OK_MESSAGE, this._onServerOkMessage, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_SIT_OUT_RESPONSE_MESSAGE, this._onServerSitOutResponseMessage, this);

		APP.currentWindow.on(GameScreen.EVENT_ON_TIME_TO_RESTART_BONUS_ROOM, this._onTimeToRestartBonusRoom, this);

		APP.on(Game.EVENT_ON_BONUS_CANCEL_ROOM_RELOAD, this._onGameReloadCancel, this);
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		let info = this.info;
		switch (data.class)
		{
			case SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE:
			case SERVER_MESSAGES.FULL_GAME_INFO:
				/*cashBonusInfo:: CashBonus only parameter, always null for real/free/frbonus/tournament.
				cashBonusInfo:balance:: current balance. Shots reduce balance, wins increase
				cashBonusInfo:amount:: initial bonus balance
				cashBonusInfo:amountToRelease:: amount of bets required to release the bonus
				cashBonusInfo:status:: current bonus status (ACTIVE, RELEASED, LOST, CANCELLED, EXPIRED, RELEASING, CANCELLING, CLOSED)*/
				if (data.cashBonusInfo)
				{
					info.id = data.cashBonusInfo.id;
					info.isActivated = true;
					info.winSum = data.cashBonusInfo.amountToRelease;
					info.currentBalance = data.cashBonusInfo.balance;
					info.initialBalance = data.cashBonusInfo.amount;
					info.currentStatus = data.cashBonusInfo.status;

					this._validateInitialBonusStatus();
				}
				break;
			case SERVER_MESSAGES.SIT_IN_RESPONSE:
				//[Y]TODO parse mode = FRB, CASHBONUS, REAL,TOURNAMENT, FREE
				break;
			case SERVER_MESSAGES.BONUS_STATUS_CHANGED:
				if (data.type !== BonusInfo.TYPE_CASH_BONUS) return;

				if (data.id !== info.id)
				{
					throw new Error(`Current Cash bonus id is ${info.id}, updated bonus id is ${data.id}!`);
				}

				info.currentStatus = data.newStatus;
				switch (info.currentStatus)
				{
					case BonusInfo.STATUS_RELEASED:
					case BonusInfo.STATUS_LOST:
					case BonusInfo.STATUS_CANCELLED:
					case BonusInfo.STATUS_EXPIRED:
						info.bonusCompletionData = data;
						info.bonusCompletionData.realWinSum = info.realWinSum;
						info.bonusCompletionData.winSum = info.winSum;
						APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_BONUS_COMPLETION_STARTED);

						if (info.currentStatus !== BonusInfo.STATUS_RELEASED && !APP.gameScreen.isRoomGameActive)
						{
							this.emit(GameBonusController.EVENT_ON_BONUS_SIT_OUT_REQUIRED);
						}
						break;
				}
				this.emit(GameBonusController.EVENT_ON_BONUS_STATE_CHANGED, {noWait: info.currentStatus !== BonusInfo.STATUS_RELEASED});
				break;
			case SERVER_MESSAGES.ROUND_RESULT:
				if (!info.isActivated) return;
				info.nextModeFRB = data.hasNextFrb;
				info.realWinSum = data.realWinAmount;
				info.winSum = data.winAmount;
				if (info.bonusCompletionData)
				{
					info.bonusCompletionData.realWinSum = info.realWinSum;
					info.bonusCompletionData.winSum = info.winSum;
				}
				break;
		}

	}

	_validateInitialBonusStatus()
	{
		let info = this.info;
		switch (info.currentStatus)
		{
			case BonusInfo.STATUS_RELEASED:
			case BonusInfo.STATUS_LOST:
			case BonusInfo.STATUS_CANCELLED:
			case BonusInfo.STATUS_EXPIRED:
			case BonusInfo.STATUS_CLOSED:
				APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_BONUS_COMPLETION_STARTED);
				APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.EVENT_ON_BONUS_STATUS_CHANGED_RESPONSE,
					{
						messageData: {id: info.id, newStatus: info.currentStatus, type: BonusInfo.TYPE_CASH_BONUS},
						nextRoomId: info.nextRoomId,
						nextModeFRB: info.nextModeFRB
					}
				);
				break;
		}

	}

	_onServerOkMessage(event)
	{
		let requestClass = undefined;
		let requestData = event.requestData;
		if (requestData && requestData.rid >= 0)
		{
			requestClass = requestData.class;
		}

		if (requestClass !== undefined)
		{
			switch (requestClass)
			{
				case CLIENT_MESSAGES.CLOSE_ROOM:
					if (this.info.isRoomRestartRequired)
					{
						APP.webSocketInteractionController.on(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onSocketReopened, this);
					}
					else
					{
						this.info.i_clearAll();
					}
					break;
			}
		}
	}

	_onSocketReopened()
	{
		APP.webSocketInteractionController.off(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onSocketReopened, this);

		if (this.info.isRoomRestartRequired)
		{
			this.emit(GameBonusController.EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS, {roomId: this.info.nextRoomId});
		}
		this._clearAll();
	}

	_onServerSitOutResponseMessage(event)
	{
		if (!this.info.isActivated) return;

		let data = event.messageData;
		if (data.rid != -1 || APP.playerController.info.seatId == data.id)
		{
			this.info.nextRoomId = data.nextRoomId;
		}
	}

	_onGameReloadCancel()
	{
		this.info.nextRoomId = null;
		this.info.isRoomRestartRequired = false;

		this._clearAll();

		this.emit(GameBonusController.EVENT_ON_BONUS_BACK_TO_LOBBY_REQUIRED);
	}

	_onTimeToRestartBonusRoom()
	{
		this.info.isRoomRestartRequired = true;
	}

	_clearAll()
	{
		APP.webSocketInteractionController.off(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onSocketReopened, this);
		this.info.i_clearAll();
	}
}

export default GameBonusController;