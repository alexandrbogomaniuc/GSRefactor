import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import FRBInfo from '../../../model/custom/frb/FRBInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../interaction/server/LobbyWebSocketInteractionController';
import LobbyExternalCommunicator from '../../../external/LobbyExternalCommunicator';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import DialogController from '../../uis/custom/dialogs/DialogController';
import LobbyLayout from './../../../layout/LobbyLayout';
import LobbyApp from './../../../LobbyAPP';
import BonusInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/bonus/BonusInfo';
import { WEAPONS } from '../../../../../shared/src/CommonConstants';

class FRBController extends SimpleController
{
	static get EVENT_ON_FRB_STATE_CHANGED() 					{ return 'EVENT_ON_FRB_STATE_CHANGED'; }
	static get EVENT_FRB_RESTART_REQUIRED() 					{ return 'EVENT_FRB_RESTART_REQUIRED';}
	static get EVENT_FRB_LOBBY_INTRO_CONFIRMED() 				{ return 'EVENT_FRB_LOBBY_INTRO_CONFIRMED';}
	static get EVENT_ON_REFRESH_BALANCE_VALIDATE_REQUIRED()		{ return 'EVENT_ON_REFRESH_BALANCE_VALIDATE_REQUIRED';}

	onBonusCanceledRoomReload()
	{
		this._onBonusCanceledRoomReload();
	}

	constructor()
	{
		super(new FRBInfo());
	}

	__init()
	{
		super.__init();

		this._fBonusCanceledRoomReload_bln = false;
		this._fCheckRoomRestartOnEnter_bln = false;

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE, this._onServerBonusStatusChangedMessage, this);
		
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		this._fFRBDialogController_frbdc = APP.dialogsController.FRBDialogController;
		this._fFRBDialogController_frbdc.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onFRBDialogRequestConfirmed, this);
		this._fFRBDialogController_frbdc.on(DialogController.EVENT_REQUEST_NOT_CONFIRMED, this._onFRBDialogRequestNotConfirmed, this);
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(LobbyApp.EVENT_ON_LOBBY_RESTART_REQUIRED, this._onLobbyRestartRequired, this);
		APP.on(LobbyApp.EVENT_ON_ROOM_CLOSED, this._onRoomClosed, this);
	}

	destroy()
	{
		APP.webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		APP.webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE, this._onServerBonusStatusChangedMessage, this);
		APP.externalCommunicator.off(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
		this._fFRBDialogController_frbdc.off(DialogController.EVENT_REQUEST_CONFIRMED, this._onFRBDialogRequestConfirmed, this);
		this._fFRBDialogController_frbdc.off(DialogController.EVENT_REQUEST_NOT_CONFIRMED, this._onFRBDialogRequestNotConfirmed, this);

		super.destroy();
	}

	_onBonusCanceledRoomReload()
	{
		let info = this.i_getInfo();

		if (info.isActivated)
		{
			info.messageType = FRBInfo.MESSAGE_FRB_LOBBY_INTRO;
			this.emit(FRBController.EVENT_ON_FRB_STATE_CHANGED, {onRoomReload: true});
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
				info.messageType = FRBInfo.MESSAGE_FRB_LOBBY_INTRO;
			}

			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.FRB_SHOTS_UPDATED, {ammoAmount: this.info.currentFreeShotsCount});
		}
		else
		{
			if (info.isActivated)
			{
				this.emit(FRBController.EVENT_FRB_RESTART_REQUIRED, {noLobbyRestart: true});
			}

			info.i_clearAll();
		}

		this.emit(FRBController.EVENT_ON_FRB_STATE_CHANGED);
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
				(data.messageData.newStatus == BonusInfo.STATUS_CANCELLED || data.messageData.newStatus == FRBInfo.CLOSE_REASON_CANCELLED ||
				data.messageData.newStatus == BonusInfo.STATUS_EXPIRED || data.messageData.newStatus == FRBInfo.CLOSE_REASON_EXPIRED) &&
				data.messageData.type == BonusInfo.TYPE_FRB
			)
		{
			if (data.messageData.newStatus == BonusInfo.STATUS_CANCELLED || data.messageData.newStatus == FRBInfo.CLOSE_REASON_CANCELLED)
			{
				data.messageData.closeReason = FRBInfo.CLOSE_REASON_CANCELLED;
			}
			if (data.messageData.newStatus == BonusInfo.STATUS_EXPIRED || data.messageData.newStatus == FRBInfo.CLOSE_REASON_EXPIRED)
			{
				data.messageData.closeReason = FRBInfo.CLOSE_REASON_EXPIRED;
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
					info.messageType = FRBInfo.MESSAGE_FRB_ROOM_INTRO;
					info.weapons = msgData.weapons;
					info.playerSatIn = true;
					this.emit(FRBController.EVENT_ON_FRB_STATE_CHANGED);
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
				msgData.messageData.closeReason = FRBInfo.CLOSE_REASON_FORCE_SIT_OUT;
				this._onFRBEnded(msgData);
				break;
			case GAME_MESSAGES.GAME_STARTED:
				info.gameFRBStarted = true;
				APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.FRB_SHOTS_UPDATED, {ammoAmount: this.info.currentFreeShotsCount});
				break;
			case GAME_MESSAGES.FRB_SHOTS_UPDATE_REQUIRED:
				if (msgData && msgData.alreadySitInAmmo)
				{
					this.info.currentFreeShotsCount = msgData.alreadySitInAmmo;
				}
				APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.FRB_SHOTS_UPDATED, {ammoAmount: this.info.currentFreeShotsCount});
				break;
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (LobbyWebSocketInteractionController.isGeneralError(msgData.errorType))
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
			case FRBInfo.CLOSE_REASON_COMPLETED:
				info.messageType = FRBInfo.MESSAGE_FRB_FINISHED;
				break;
			case FRBInfo.CLOSE_REASON_EXPIRED:
				info.messageType = FRBInfo.MESSAGE_FRB_EXPIRED;
				break;
			case FRBInfo.CLOSE_REASON_CANCELLED:
				info.messageType = FRBInfo.MESSAGE_FRB_CANCELLED;
				break;
			case FRBInfo.CLOSE_REASON_FORCE_SIT_OUT:
				info.messageType = FRBInfo.MESSAGE_FORCE_SIT_OUT;
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

		this.emit(FRBController.EVENT_ON_REFRESH_BALANCE_VALIDATE_REQUIRED);
		this.emit(FRBController.EVENT_ON_FRB_STATE_CHANGED);
	}

	_onFRBDialogRequestConfirmed(event)
	{
		let info = this.info;
		switch (info.messageType)
		{
			case FRBInfo.MESSAGE_FRB_EXPIRED:
			case FRBInfo.MESSAGE_FRB_CANCELLED:
				info.frbCompletionState = false;
				info.isLobbyRestartRequired = true;
				this._onFRBRestart();
				break;
			case FRBInfo.MESSAGE_FRB_FINISHED:
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
			case FRBInfo.MESSAGE_FRB_LOBBY_INTRO:
				this.info.gameFRBStarted = true;
				this.emit(FRBController.EVENT_FRB_LOBBY_INTRO_CONFIRMED);
				break;
		}

		this.emit(FRBController.EVENT_ON_REFRESH_BALANCE_VALIDATE_REQUIRED);

		this._checkEndDuringLoading();
	}

	_onFRBDialogRequestNotConfirmed(event)
	{
		let info = this.info;
		switch (info.messageType)
		{
			case FRBInfo.MESSAGE_FRB_FINISHED:
			case FRBInfo.MESSAGE_FRB_EXPIRED:
			case FRBInfo.MESSAGE_FRB_CANCELLED:
				info.frbCompletionState = false;
				info.isLobbyRestartRequired = true;
				this._onFRBRestart();
				break;
		}

		this.emit(FRBController.EVENT_ON_REFRESH_BALANCE_VALIDATE_REQUIRED);

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
		this.emit(FRBController.EVENT_FRB_RESTART_REQUIRED);
	}

	_onTimeToShowNewStartDialog()
	{
		this.info.messageType = FRBInfo.MESSAGE_FRB_LOBBY_INTRO;
		this.emit(FRBController.EVENT_ON_FRB_STATE_CHANGED);
	}

	_handleGameGeneralError(errorCode)
	{
		let supported_codes = LobbyWebSocketInteractionController.ERROR_CODES;
		switch (errorCode)
		{
			case supported_codes.TOO_MANY_OBSERVERS:
				if (this.info.isActivated)
				{
					this.emit(FRBController.EVENT_FRB_LOBBY_INTRO_CONFIRMED);
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

export default FRBController