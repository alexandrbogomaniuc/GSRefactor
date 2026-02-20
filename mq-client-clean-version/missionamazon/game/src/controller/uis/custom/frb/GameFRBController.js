import BonusInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/bonus/BonusInfo';
import BonusController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/custom/bonus/BonusController';
import GameFRBInfo from './../../../../model/uis/custom/frb/GameFRBInfo';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../../interaction/server/GameWebSocketInteractionController';
import { CLIENT_MESSAGES, SERVER_MESSAGES } from '../../../../model/interaction/server/GameWebSocketInteractionInfo';
import WebSocketInteractionController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/server/WebSocketInteractionController';
import GameScreen from '../../../../main/GameScreen';
import Game from '../../../../Game';
import GameExternalCommunicator, { LOBBY_MESSAGES } from '../../../../external/GameExternalCommunicator';

class GameFRBController extends BonusController
{
	static get EVENT_ON_BONUS_STATE_CHANGED()			{return BonusController.EVENT_ON_BONUS_STATE_CHANGED;}
	static get EVENT_ON_FRB_MODE_CHANGED()				{return "onFRBModeChanged";}
	static get EVENT_ON_FRB_STATUS_CHANGED()			{return "onFRBStatusChanged";}
	static get EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS() 	{ return "EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS"; }
	static get EVENT_ON_FRB_BACK_TO_LOBBY_REQUIRED()			{ return "EVENT_ON_FRB_BACK_TO_LOBBY_REQUIRED"; }

	constructor()
	{
		super(new GameFRBInfo());
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_OK_MESSAGE, this._onServerOkMessage, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_SIT_OUT_RESPONSE_MESSAGE, this._onServerSitOutResponseMessage, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);

		APP.currentWindow.on(GameScreen.EVENT_ON_TIME_TO_RESTART_BONUS_ROOM, this._onTimeToRestartBonusRoom, this);

		APP.on(Game.EVENT_ON_BONUS_CANCEL_ROOM_RELOAD, this._onGameReloadCancel, this);

		APP.externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		let info = this.info;
		switch (data.class)
		{
			case SERVER_MESSAGES.SIT_IN_RESPONSE:
				this.info.isFrbEndedAndConnectionLost = false;
				this._parseGameMode(data.mode);
				break;
			case SERVER_MESSAGES.FRB_ENDED:
				info.bonusCompletionData = data;
				this._handleFRBStateChange(data);
				break;
		}
	}

	_onGameServerConnectionClosed(event)
	{
		if (!event.wasClean && this.info.frbEnded)
		{
			this.info.isFrbEndedAndConnectionLost = true;
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
					this.onRoomClosed();
					break;
			}
		}
	}

	onRoomClosed()
	{
		if (this.info.isRoomRestartRequired)
		{
			APP.webSocketInteractionController.on(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onSocketReopened, this);
		}
		else
		{
			this.info.i_clearAll();
		}
	}

	_onSocketReopened()
	{
		APP.webSocketInteractionController.off(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onSocketReopened, this);

		if (this.info.isRoomRestartRequired)
		{
			this.emit(GameFRBController.EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS, {roomId: this.info.nextRoomId});
		}
		this._clearAll();
	}

	_handleFRBStateChange(aData_obj)
	{
		this.info.frbEndReason = aData_obj.closeReason;
		this.info.frbEnded = true;
		this.info.nextModeFrb = aData_obj.hasNextFrb;
		this.info.continousNextModeFrb = aData_obj.hasNextFrb;
		this.info.winSum = aData_obj.winSum;
		this.info.realWinSum = aData_obj.realWinSum;

		let lWinSum_num = aData_obj.winSum;
		let lRealWinSum_num = aData_obj.realWinSum; // - limited value

		let lWeaponsInfo = APP.currentWindow.weaponsController.info;
		let lDefaultAmmoAmount_int = lWeaponsInfo.ammo;
		let lIsWinLimitExceeded_bl = (
										(lWinSum_num > lRealWinSum_num) 
										|| (lWinSum_num == lRealWinSum_num && (lDefaultAmmoAmount_int > 0))
									)
		this.info.isWinLimitExceeded = aData_obj.isWinLimitExceeded = lIsWinLimitExceeded_bl;

		this.emit(GameFRBController.EVENT_ON_FRB_STATUS_CHANGED)
	}

	_onLobbyExternalMessageReceived(aEvent_obj)
	{
		switch (aEvent_obj.type)
		{
			case LOBBY_MESSAGES.FRB_SHOTS_UPDATED:
				let data = aEvent_obj.data;
				if (data.ammoAmount > 0)
				{
					this.info.continousNextModeFrb = true;
				}
			break;
		}
	}

	_parseGameMode(aMode_str)
	{
		switch(aMode_str)
		{
			case BonusInfo.TYPE_FRB:
				this._updateModeFrb(true);
			break;
			default:
				this._updateModeFrb(false);
			break;
		}
	}

	_onServerSitOutResponseMessage(event)
	{
		if (!this.info.frbMode) return;

		let data = event.messageData;
		if (APP.playerController.info.seatId == data.id)
		{
			this.info.nextRoomId = data.nextRoomId;
		}
	}

	_onTimeToRestartBonusRoom()
	{
		this.info.isRoomRestartRequired = true;
	}

	_onGameReloadCancel()
	{
		this.info.nextRoomId = null;
		this.info.isRoomRestartRequired = false;

		this._clearAll();

		this.emit(GameFRBController.EVENT_ON_FRB_BACK_TO_LOBBY_REQUIRED);
	}

	_updateModeFrb(aIsActive_bln)
	{
		this.info.frbMode = aIsActive_bln;
		this.info.continousNextModeFrb = false;
		this.emit(GameFRBController.EVENT_ON_FRB_MODE_CHANGED, {value: this.info.frbMode});
	}

	_clearAll()
	{
		APP.webSocketInteractionController.off(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onSocketReopened, this);
		this.info.i_clearAll();
	}
}

export default GameFRBController;