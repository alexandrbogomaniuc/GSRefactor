import BonusController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/custom/bonus/BonusController';
import LobbyBonusInfo from '../../../../model/custom/bonus/LobbyBonusInfo';
import BonusInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/bonus/BonusInfo';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyExternalCommunicator from '../../../../external/LobbyExternalCommunicator';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import DialogController from '../dialogs/DialogController';
import LobbyWebSocketInteractionController from '../../../interaction/server/LobbyWebSocketInteractionController';
import LobbyApp from '../../../../LobbyAPP';

class LobbyBonusController extends BonusController {

	static get EVENT_ON_BONUS_STATE_CHANGED() 		{ return BonusController.EVENT_ON_BONUS_STATE_CHANGED; }

	static get EVENT_BONUS_RESTART_REQUIRED() 		{ return 'EVENT_BONUS_RESTART_REQUIRED';}
	static get EVENT_BONUS_LOBBY_INTRO_CONFIRMED() 	{ return 'EVENT_BONUS_LOBBY_INTRO_CONFIRMED';}
	static get EVENT_ON_BONUS_ENTER_ROOM_REQUIRED()	{ return 'EVENT_ON_BONUS_ENTER_ROOM_REQUIRED';}

	constructor()
	{
		super(new LobbyBonusInfo());
	}

	__init()
	{
		super.__init();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE, this._onServerBonusStatusChangedMessage, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		this._fBonusDialogController_bdc = APP.dialogsController.bonusDialogController;
		this._fBonusDialogController_bdc.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onBonusDialogRequestConfirmed, this);
		this._fBonusDialogController_bdc.on(DialogController.EVENT_REQUEST_NOT_CONFIRMED, this._onBonusDialogRequestNotConfirmed, this);
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(LobbyApp.EVENT_ON_LOBBY_RESTART_REQUIRED, this._onLobbyRestartRequired, this);
	}

	destroy()
	{
		APP.webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		APP.webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE, this._onServerBonusStatusChangedMessage, this);
		APP.externalCommunicator.off(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
		this._fBonusDialogController_bdc.off(DialogController.EVENT_REQUEST_CONFIRMED, this._onBonusDialogRequestConfirmed, this);
		this._fBonusDialogController_bdc.off(DialogController.EVENT_REQUEST_NOT_CONFIRMED, this._onBonusDialogRequestNotConfirmed, this);


		super.destroy();
	}

	_onServerEnterLobbyMessage(event)
	{
		let data = event.messageData;
		let info = this.i_getInfo();
		/*cashBonusInfo:: CashBonus only parameter, always null for real/free/frbonus/tournament.
		cashBonusInfo:balance:: current balance. Shots reduce balance, wins increase
		cashBonusInfo:amount:: initial bonus balance
		cashBonusInfo:amountToRelease:: amount of bets required to release the bonus
		cashBonusInfo:status:: current bonus status (ACTIVE, RELEASED, LOST, CANCELLED, EXPIRED, CLOSED)*/

		info.isLobbyRestartRequired = false;

		if (this._fCheckRoomRestartOnEnter_bln && data.frBonusInfo && !info.nextModeFRB)
		{
			info.nextModeFRB = true;
			info.isRoomRestartRequired = false;

			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BONUS_CANCEL_ROOM_RELOAD);
			APP.FRBController.onBonusCanceledRoomReload();
		}

		this._fCheckRoomRestartOnEnter_bln = false;

		if (data.cashBonusInfo)
		{
			info.id = data.cashBonusInfo.id;
			info.isActivated = true;
			info.winSum = data.cashBonusInfo.amountToRelease;
			info.currentBalance = data.cashBonusInfo.balance;
			info.initialBalance = data.cashBonusInfo.amount;
			info.currentStatus = data.cashBonusInfo.status;

			if (!APP.layout.isGamesLayoutVisible)
			{
				if (info.currentStatus == BonusInfo.STATUS_ACTIVE)
				{
					info.messageType = BonusInfo.MESSAGE_BONUS_LOBBY_INTRO;
				}
				else
				{
					info.messageType = this._calcMessageType(info.currentStatus);
				}
			}
		}
		else
		{
			if (info.isActivated)
			{
				this.emit(LobbyBonusController.EVENT_BONUS_RESTART_REQUIRED);
			}

			info.i_clearAll();
		}
		this.emit(LobbyBonusController.EVENT_ON_BONUS_STATE_CHANGED);

		//BONUS DEBUG...
		/*info.isActivated = true;
		info.winSum = 56;
		info.messageType = BonusInfo.MESSAGE_BONUS_LOBBY_INTRO;
		this.emit(LobbyBonusController.EVENT_ON_BONUS_STATE_CHANGED);*/
		//...BONUS DEBUG
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		let msgData = event.data;
		let info = this.info;

		if (!info.isActivated)
			return;

		switch (msgType)
		{
			case GAME_MESSAGES.ON_SIT_IN_RESPONSE:
				if (msgData.rid !== -1)
				{
					if (info.currentStatus === BonusInfo.STATUS_ACTIVE)
					{
						info.messageType = BonusInfo.MESSAGE_BONUS_ROOM_INTRO;
						info.weapons = msgData.weapons;
						this.emit(LobbyBonusController.EVENT_ON_BONUS_STATE_CHANGED);
					}
				}
				break;
			case GAME_MESSAGES.EVENT_ON_BONUS_STATUS_CHANGED_RESPONSE:
				this._onBonusStatusChanged(msgData);
				break;
			case GAME_MESSAGES.GAME_BONUS_COMPLETION_STARTED:
				this._onGameBonusCompletionStarted();
				break;
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (LobbyWebSocketInteractionController.isGeneralError(msgData.errorType))
				{
					this._handleGameGeneralError(msgData.errorCode);
				}
				break;
		}
	}

	_onBonusStatusChanged(data)
	{
		let messageData = data.messageData;

		if (!messageData || messageData.type !== BonusInfo.TYPE_CASH_BONUS) return;

		let info = this.info;

		if (messageData.id !== info.id)
		{
			throw new Error(`Current Cash bonus id is ${info.id}, updated bonus id is ${messageData.id}!`);
		}

		info.nextRoomId = data.nextRoomId;
		info.nextModeFRB = data.nextModeFRB;

		info.currentStatus = messageData.newStatus;

		let lMessageType_str = this._calcMessageType(messageData.newStatus);
		if (lMessageType_str == null)
		{
			throw new Error (`Unsupported bonus's newStatus: ${messageData.newStatus}`);
		}
		info.messageType = lMessageType_str;
		info.winSum = messageData.winSum;
		info.realWinSum = messageData.realWinSum;

		if (APP.gameLauncher.isGameLoadingInProgress)
		{
			info.bonusEndedDuringGameLoad = true;
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BONUS_CANCELED_DURING_LOAD);
		}

		this.emit(LobbyBonusController.EVENT_ON_BONUS_STATE_CHANGED);
	}

	_calcMessageType(aStatus_str)
	{
		switch (aStatus_str)
		{
			case BonusInfo.STATUS_RELEASED:
				return BonusInfo.MESSAGE_BONUS_RELEASED;

			case BonusInfo.STATUS_EXPIRED:
				return BonusInfo.MESSAGE_BONUS_EXPIRED;

			case BonusInfo.STATUS_CANCELLED:
				return BonusInfo.MESSAGE_BONUS_CANCELLED;

			case BonusInfo.STATUS_LOST:
				return BonusInfo.MESSAGE_BONUS_LOST;

			case BonusInfo.STATUS_CLOSED:
				throw new Error("Bonus closed!"); //[Y]TODO what to do?
				break;
		}
		return null;
	}

	_onServerBonusStatusChangedMessage(data)
	{
		let info = this.info;

		if (!info.isActivated)
			return;

		let commonPanelInfo = APP.commonPanelController.info;
		if (commonPanelInfo.gameUIVisible)
		{
			//BonusStatusChange message will be handled via game socket
			console.log("[Y] ignore BonusStatusChange in Lobby. Message will be handler via game socket");
			return;
		}

		this._onBonusStatusChanged(data);
	}

	_onBonusDialogRequestConfirmed(event)
	{
		let info = this.info;

		//reset messageType
		info.messageType = null;

		if (info.bonusEndedDuringGameLoad)
		{
			APP.lobbyScreen.gameInitiated = false;
			APP.gameLauncher.isGameLoadingInProgress = false;
			APP.gameLauncher.switchToLobby();
			APP.layout.showCommonPanel();
			info.bonusEndedDuringGameLoad = false;
		}

		switch (info.currentStatus)
		{
			case BonusInfo.STATUS_EXPIRED:
			case BonusInfo.STATUS_CANCELLED:
			case BonusInfo.STATUS_LOST:
				info.isLobbyRestartRequired = true;
				this.emit(LobbyBonusController.EVENT_BONUS_RESTART_REQUIRED);
				break;
			case BonusInfo.STATUS_ACTIVE:
				this.emit(LobbyBonusController.EVENT_BONUS_LOBBY_INTRO_CONFIRMED);
				break;
			case BonusInfo.STATUS_RELEASED:
				if (!info.isRoomRestartPossible)
				{
					info.isLobbyRestartRequired = true;
					this.emit(LobbyBonusController.EVENT_BONUS_RESTART_REQUIRED);
				}
				else
				{
					info.isRoomRestartRequired = true;
					this.emit(LobbyBonusController.EVENT_BONUS_RESTART_REQUIRED);
				}
				break;
		}
	}

	_onBonusDialogRequestNotConfirmed(event)
	{
		let info = this.info;

		//reset messageType
		info.messageType = null;

		switch (info.currentStatus)
		{
			case BonusInfo.STATUS_RELEASED:
				info.isLobbyRestartRequired = true;
				this.emit(LobbyBonusController.EVENT_BONUS_RESTART_REQUIRED);
				break;
			default:
				throw new Error("Wrong Bonus status for rejecting in Bonus Dialog : " + info.currentStatus);
				break;
		}
	}

	_onLobbyRestartRequired()
	{
		if (this.info.isActivated && this.info.isRoomRestartRequired)
		{
			this._fCheckRoomRestartOnEnter_bln = true;
		}

		this.info.i_clearAll();
	}

	_onGameBonusCompletionStarted()
	{
		this.info.isCompletionInProgress = true;
		this.emit(LobbyBonusController.EVENT_ON_BONUS_STATE_CHANGED);
	}

	_handleGameGeneralError(errorCode)
	{
		let supported_codes = LobbyWebSocketInteractionController.ERROR_CODES;
		switch (errorCode)
		{
			case supported_codes.TOO_MANY_OBSERVERS:
				if (this.info.isActivated)
				{
					this.emit(LobbyBonusController.EVENT_ON_BONUS_ENTER_ROOM_REQUIRED);
				}
				break;
		}
	}
}

export default LobbyBonusController