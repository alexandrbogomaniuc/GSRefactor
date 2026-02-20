import SimpleController from '../../../../unified/controller/base/SimpleController';
import GUSLobbyFRBInfo from '../../../model/custom/frb/GUSLobbyFRBInfo';
import GUSLobbyApplication from '../../main/GUSLobbyApplication';
import { APP } from '../../../../unified/controller/main/globals';
import BonusInfo from '../../../../unified/model/custom/bonus/BonusInfo';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../external/GUSExternalCommunicator';
import GUSLobbyExternalCommunicator from '../../external/GUSLobbyExternalCommunicator';
import { WEAPONS } from '../../../model/weapons/GUSWeaponsInfo';
import GUDialogController from '../../uis/custom/dialogs/GUDialogController';
import GUSLobbyWebSocketInteractionController from '../../interaction/server/GUSLobbyWebSocketInteractionController';

class GUSLobbyFRBController extends SimpleController
{
	static get EVENT_ON_FRB_STATE_CHANGED()					{ return 'EVENT_ON_FRB_STATE_CHANGED'; }
	static get EVENT_FRB_RESTART_REQUIRED()					{ return 'EVENT_FRB_RESTART_REQUIRED'; }
	static get EVENT_FRB_LOBBY_INTRO_CONFIRMED()			{ return 'EVENT_FRB_LOBBY_INTRO_CONFIRMED'; }
	static get EVENT_ON_REFRESH_BALANCE_VALIDATE_REQUIRED()	{ return 'EVENT_ON_REFRESH_BALANCE_VALIDATE_REQUIRED'; }

	onBonusCanceledRoomReload()
	{
		this._onBonusCanceledRoomReload();
	}

	constructor(aOptInfo)
	{
		super(aOptInfo || new GUSLobbyFRBInfo());
	}

	__init()
	{
		super.__init();

		this._fBonusCanceledRoomReload_bln = false;
		this._fCheckRoomRestartOnEnter_bln = false;

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE, this._onServerBonusStatusChangedMessage, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		this._fFRBDialogController_frbdc = APP.dialogsController.FRBDialogController;
		this._fFRBDialogController_frbdc.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onFRBDialogRequestConfirmed, this);
		this._fFRBDialogController_frbdc.on(GUDialogController.EVENT_REQUEST_NOT_CONFIRMED, this._onFRBDialogRequestNotConfirmed, this);
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(GUSLobbyApplication.EVENT_ON_LOBBY_RESTART_REQUIRED, this._onLobbyRestartRequired, this);
		APP.on(GUSLobbyApplication.EVENT_ON_ROOM_CLOSED, this._onRoomClosed, this);
	}

	destroy()
	{
		APP.webSocketInteractionController.off(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		APP.webSocketInteractionController.off(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE, this._onServerBonusStatusChangedMessage, this);
		APP.externalCommunicator.off(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
		this._fFRBDialogController_frbdc.off(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onFRBDialogRequestConfirmed, this);
		this._fFRBDialogController_frbdc.off(GUDialogController.EVENT_REQUEST_NOT_CONFIRMED, this._onFRBDialogRequestNotConfirmed, this);

		super.destroy();
	}

	_onBonusCanceledRoomReload()
	{
		let info = this.i_getInfo();

		if (info.isActivated)
		{
			info.messageType = GUSLobbyFRBInfo.MESSAGE_FRB_LOBBY_INTRO;
			this.emit(GUSLobbyFRBController.EVENT_ON_FRB_STATE_CHANGED, {onRoomReload: true});
		}
		else
		{
			this._fBonusCanceledRoomReload_bln = true;
		}
	}

	_onServerEnterLobbyMessage(event)
	{
		let data = event.messageData;
		let info = this.i_getInfo();

		info.isLobbyRestartRequired = false;

		if (this._fCheckRoomRestartOnEnter_bln && data.frBonusInfo && !info.nextModeFRB)
		{
			info.nextModeFRB = true;
			info.isRoomRestartRequired = false;

			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BONUS_CANCEL_ROOM_RELOAD);
			this._onBonusCanceledRoomReload();
		}

		this._fCheckRoomRestartOnEnter_bln = false;

		if (data.frBonusInfo)
		{
			info.isActivated = true;
			info.totalFreeShotsCount = data.frBonusInfo.totalShots;
			info.currentFreeShotsCount = data.frBonusInfo.currentShots;
			info.winSum = data.frBonusInfo.winSum;
			info.realWinSum = data.frBonusInfo.realWinSum;
			info.stake = data.frBonusInfo.stake;
			info.id = data.frBonusInfo.id;

			if (!APP.layout.isGamesLayoutVisible || this._fBonusCanceledRoomReload_bln)
			{
				info.messageType = GUSLobbyFRBInfo.MESSAGE_FRB_LOBBY_INTRO;
			}

			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.FRB_SHOTS_UPDATED, { ammoAmount: this.info.currentFreeShotsCount });
		}
		else
		{
			if (info.isActivated)
			{
				this.emit(GUSLobbyFRBController.EVENT_FRB_RESTART_REQUIRED, { noLobbyRestart: true });
			}

			info.i_clearAll();
		}

		this.emit(GUSLobbyFRBController.EVENT_ON_FRB_STATE_CHANGED);
	}

	_onServerBonusStatusChangedMessage(data)
	{
		let commonPanelInfo = APP.commonPanelController.info;
		if (commonPanelInfo.gameUIVisible)
		{
			//BonusStatusChange message will be handled via game socket
			return;
		}

		let info = this.i_getInfo();
		if (
				data.messageData && info.isActivated && info.id == data.messageData.id && 
				(data.messageData.newStatus == BonusInfo.STATUS_CANCELLED || data.messageData.newStatus == GUSLobbyFRBInfo.CLOSE_REASON_CANCELLED ||
				data.messageData.newStatus == BonusInfo.STATUS_EXPIRED || data.messageData.newStatus == GUSLobbyFRBInfo.CLOSE_REASON_EXPIRED) &&
				data.messageData.type == BonusInfo.TYPE_FRB
			)
		{
			if (data.messageData.newStatus == BonusInfo.STATUS_CANCELLED || data.messageData.newStatus == GUSLobbyFRBInfo.CLOSE_REASON_CANCELLED)
			{
				data.messageData.closeReason = GUSLobbyFRBInfo.CLOSE_REASON_CANCELLED;
			}
			if (data.messageData.newStatus == BonusInfo.STATUS_EXPIRED || data.messageData.newStatus == GUSLobbyFRBInfo.CLOSE_REASON_EXPIRED)
			{
				data.messageData.closeReason = GUSLobbyFRBInfo.CLOSE_REASON_EXPIRED;
			}

			this._onFRBEnded(data);
		}
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
					info.messageType = GUSLobbyFRBInfo.MESSAGE_FRB_ROOM_INTRO;
					info.weapons = msgData.weapons;
					info.playerSatIn = true;
					this.emit(GUSLobbyFRBController.EVENT_ON_FRB_STATE_CHANGED);
				}
				break;
			case GAME_MESSAGES.ON_FRB_ENDED_RESPONSE:
				this._onFRBEnded(msgData);
				break;
			case GAME_MESSAGES.EVENT_ON_FORCE_SIT_OUT_REQUIRED:
				if (msgData.messageData == undefined)
				{
					msgData.messageData = {};
				}
				msgData.messageData.closeReason = GUSLobbyFRBInfo.CLOSE_REASON_FORCE_SIT_OUT;
				this._onFRBEnded(msgData);
				break;
			case GAME_MESSAGES.GAME_STARTED:
				info.gameFRBStarted = true;
				APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.FRB_SHOTS_UPDATED, { ammoAmount: this.info.currentFreeShotsCount });
				break;
			case GAME_MESSAGES.FRB_SHOTS_UPDATE_REQUIRED:
				if (msgData && msgData.alreadySitInAmmo)
				{
					this.info.currentFreeShotsCount = msgData.alreadySitInAmmo;
				}
				APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.FRB_SHOTS_UPDATED, { ammoAmount: this.info.currentFreeShotsCount });
				break;
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (GUSLobbyWebSocketInteractionController.isGeneralError(msgData.errorType))
				{
					info.gameFRBStarted = false;
					this._handleGameGeneralError(msgData.errorCode);
				}
				break;
			case GAME_MESSAGES.WEAPONS_UPDATED:
				let lWeapons_obj = msgData.weapons;
				let lIsRoundStatePlay_bln = msgData.isRoundStatePlay;
				if (lIsRoundStatePlay_bln)
				{
					this.info.currentFreeShotsCount = lWeapons_obj[WEAPONS.DEFAULT];
				}
				break;
		}
	}

	_onFRBEnded(data)
	{
		let lMsgData_obj = data.messageData;
		let info = this.info;
		switch (lMsgData_obj.closeReason)
		{
			case GUSLobbyFRBInfo.CLOSE_REASON_COMPLETED:
				info.messageType = GUSLobbyFRBInfo.MESSAGE_FRB_FINISHED;
				break;
			case GUSLobbyFRBInfo.CLOSE_REASON_EXPIRED:
				info.messageType = GUSLobbyFRBInfo.MESSAGE_FRB_EXPIRED;
				break;
			case GUSLobbyFRBInfo.CLOSE_REASON_CANCELLED:
				info.messageType = GUSLobbyFRBInfo.MESSAGE_FRB_CANCELLED;
				break;
			case GUSLobbyFRBInfo.CLOSE_REASON_FORCE_SIT_OUT:
				info.messageType = GUSLobbyFRBInfo.MESSAGE_FORCE_SIT_OUT;
				break;
			default:
				throw new Error (`Unsupported FRB closeReason ${lMsgData_obj.closeReason}`);
				break;
		}
		info.nextModeFRB = lMsgData_obj.hasNextFrb;
		info.winSum = lMsgData_obj.winSum;
		info.realWinSum = lMsgData_obj.realWinSum;
		info.nextRoomId = data.nextRoomId;
		info.frbCompletionState = true;
		info.playerSatIn = false;

		if (lMsgData_obj.isWinLimitExceeded == undefined)
		{
			info.isWinLimitExceeded = info.winSum > info.realWinSum;
		}
		else
		{
			info.isWinLimitExceeded = lMsgData_obj.isWinLimitExceeded;
		}

		if (APP.gameLauncher.isGameLoadingInProgress)
		{
			info.frbEndedDuringGameLoad = true;
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BONUS_CANCELED_DURING_LOAD);
		}

		this.emit(GUSLobbyFRBController.EVENT_ON_REFRESH_BALANCE_VALIDATE_REQUIRED);
		this.emit(GUSLobbyFRBController.EVENT_ON_FRB_STATE_CHANGED);
	}

	_onFRBDialogRequestConfirmed(event)
	{
		let info = this.info;
		switch (info.messageType)
		{
			case GUSLobbyFRBInfo.MESSAGE_FRB_EXPIRED:
			case GUSLobbyFRBInfo.MESSAGE_FRB_CANCELLED:
				info.frbCompletionState = false;
				info.isLobbyRestartRequired = true;
				this._onFRBRestart();
				break;
			case GUSLobbyFRBInfo.MESSAGE_FRB_FINISHED:
				info.frbCompletionState = false;
				if (!info.isRoomRestartPossible)
				{
					info.isLobbyRestartRequired = true;
				}
				else
				{
					info.isRoomRestartRequired = true;
				}
				this._onFRBRestart();
				break;
			case GUSLobbyFRBInfo.MESSAGE_FRB_LOBBY_INTRO:
				this.info.gameFRBStarted = true;
				this.emit(GUSLobbyFRBController.EVENT_FRB_LOBBY_INTRO_CONFIRMED);
				break;
		}

		this.emit(GUSLobbyFRBController.EVENT_ON_REFRESH_BALANCE_VALIDATE_REQUIRED);

		this._checkEndDuringLoading();
	}

	_onFRBDialogRequestNotConfirmed(event)
	{
		let info = this.info;
		switch (info.messageType)
		{
			case GUSLobbyFRBInfo.MESSAGE_FRB_FINISHED:
			case GUSLobbyFRBInfo.MESSAGE_FRB_EXPIRED:
			case GUSLobbyFRBInfo.MESSAGE_FRB_CANCELLED:
				info.frbCompletionState = false;
				info.isLobbyRestartRequired = true;
				this._onFRBRestart();
				break;
		}

		this.emit(GUSLobbyFRBController.EVENT_ON_REFRESH_BALANCE_VALIDATE_REQUIRED);

		this._checkEndDuringLoading();
	}

	_checkEndDuringLoading()
	{
		let info = this.info;
		if (info.frbEndedDuringGameLoad)
		{
			APP.lobbyScreen.gameInitiated = false;
			APP.gameLauncher.isGameLoadingInProgress = false;
			APP.gameLauncher.switchToLobby();
			APP.layout.showCommonPanel();
			info.frbEndedDuringGameLoad = false;
		}
	}

	_onFRBRestart()
	{
		this.emit(GUSLobbyFRBController.EVENT_FRB_RESTART_REQUIRED);
	}

	_onTimeToShowNewStartDialog()
	{
		this.info.messageType = GUSLobbyFRBInfo.MESSAGE_FRB_LOBBY_INTRO;
		this.emit(GUSLobbyFRBController.EVENT_ON_FRB_STATE_CHANGED);
	}

	_handleGameGeneralError(errorCode)
	{
		let supported_codes = GUSLobbyWebSocketInteractionController.ERROR_CODES;
		switch (errorCode)
		{
			case supported_codes.TOO_MANY_OBSERVERS:
				if (this.info.isActivated)
				{
					this.emit(GUSLobbyFRBController.EVENT_FRB_LOBBY_INTRO_CONFIRMED);
				}
				break;
		}
	}

	_onLobbyRestartRequired()
	{
		if (this.info.isActivated && this.info.isRoomRestartRequired)
		{
			this._fCheckRoomRestartOnEnter_bln = true;
		}
	}

	_onRoomClosed()
	{
		this.info.gameFRBStarted = false;
	}
}

export default GUSLobbyFRBController