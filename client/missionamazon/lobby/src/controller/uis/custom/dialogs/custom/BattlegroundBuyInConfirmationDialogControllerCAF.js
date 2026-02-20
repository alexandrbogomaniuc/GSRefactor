import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';
import BattlegroundController from '../../../../custom/battleground/BattlegroundController';
import {BTN_TYPE} from '../../../../../view/uis/custom/dialogs/custom/BattlegroundBuyInConfirmationDialogViewCAF';
import {GAME_MESSAGES, LOBBY_MESSAGES} from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import LobbyExternalCommunicator from '../../../../../external/LobbyExternalCommunicator';
import GameBattlegroundNoWeaponsFiredDialogController from './game/GameBattlegroundNoWeaponsFiredDialogController';
import { BATTLEGROUND_ROOM_STATE } from '../../../../../config/Constants';
import { GAME_CLIENT_MESSAGES } from '../../../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import LobbyApp from '../../../../../LobbyAPP';
import { GAME_ROUND_STATE } from '../../../../../../../shared/src/CommonConstants';
import BattlegroundBuyInConfirmationDialogInfoCAF from '../../../../../model/uis/custom/dialogs/custom/BattlegroundBuyInConfirmationDialogInfoCAF';


class BattlegroundBuyInConfirmationDialogControllerCAF extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };
	static get EVENT_BATTLEGROUND_BUY_IN_CONFIRMED () { return "EVENT_BATTLEGROUND_BUY_IN_CONFIRMED" };
	static get EVENT_BATTLEGROUND_RE_BUY_CONFIRMED () { return "EVENT_BATTLEGROUND_RE_BUY_CONFIRMED" };
	static get EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED () { return "EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED" };
	static get EVENT_DIALOG_ACTIVATED()	{ return DialogController.EVENT_DIALOG_ACTIVATED };
	static get EVENT_DIALOG_DEACTIVATED() { return DialogController.EVENT_DIALOG_DEACTIVATED };
	static get EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS() { return "EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS" };

	constructor(aOptInfo_usuii, parentController)
	{
		console.log("is caf buy in confirmation ");
		super(aOptInfo_usuii, undefined, parentController);

		this._fTournamentModeInfo_tni = null;
		this._fInterval = null;
		this._fBattlegroundController_bc = null;

		this._fCurrentBalance = null;

		this._fIsRoomManagerChecked_bl = false;

		this._initBattlegroundBuyInConfirmationDialogController();

		this._fIsPlayerSitOutRequired_bl = false;
		this._fIsGameStateQualify_bl = false;
	}

	_initBattlegroundBuyInConfirmationDialogController()
	{

	}

	__init ()
	{
		super.__init()

	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().battlegroundBuyInConfirmationDialogViewCAF;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();

		this._fTournamentModeInfo_tni = APP.tournamentModeController.info;
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		if (!APP.isBattlegroundGame || !APP.isCAFMode)
		{
			return;
		}

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.once(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);

		this._fBattlegroundController_bc = APP.battlegroundController;
		this._fBattlegroundController_bc.on(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_REOPENED, this._reopenBuyInDialog, this);
		this._fBattlegroundController_bc.on(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_CLOSED, this._closeBuyInDialog, this);
		if (APP.isCAFMode)
		{
			this._fBattlegroundController_bc.on(BattlegroundController.EVENT_ON_ROUND_PROGRESS_STATE_CHANGED, this._onBattlegroundRoundProgressStateChanged, this);
		}

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		let roomNotFoundDialogController = APP.dialogsController.roomNotFoundDialogController;
		roomNotFoundDialogController.on(DialogController.EVENT_DIALOG_ACTIVATED, this._onRoomNotFoundDialogActivated, this);

		APP.dialogsController.gameGameBattlegroundNoWeaponsFiredDialogController.on(
			GameBattlegroundNoWeaponsFiredDialogController.EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED, this._noWeaponsFiredPlayAgainClicked, this);


		/*this._fGameStateController_gsc = APP.currentWindow.gameStateController;
		this._fGameStateController_gsc.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);*/

		// DEBUG...
		//window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	_onGameStateChanged(event)
	{
		let l_gsi = this._fGameStateController_gsc.info;
		if (this.info.isActive && l_gsi.isWaitState && APP.isNewMatchMakingSupported)
		{
				this.__deactivateDialog();
		}
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	//DEBUG...
	/*keyDownHandler(keyCode)
	{
		if (keyCode.keyCode == 68) //d
		{
			this.__activateDialog();
		}
	}*/
	//...DEBUG

	_reopenBuyInDialog()
	{
		let l_bi = APP.battlegroundController.info;
		if(l_bi.isRoundInProgress && APP.isNewMatchMakingSupported) return;

		this.info.setMode(BattlegroundBuyInConfirmationDialogInfoCAF.MODE_ID_RE_BUY);
		this.info.setBuyInCost(l_bi.getConfirmedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn);

		this.__activateDialog();
	}

	_closeBuyInDialog()
	{
		this.__deactivateDialog();
	}

	_noWeaponsFiredPlayAgainClicked()
	{
		let l_bi = APP.battlegroundController.info;
		if(l_bi.isRoundInProgress) return;
		this.info.setMode(BattlegroundBuyInConfirmationDialogInfoCAF.MODE_ID_RE_BUY);
		this.info.setBuyInCost(l_bi.getConfirmedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn);

		this.__activateDialog();
	}

	_onServerEnterLobbyMessage(event)
	{
		this._fCurrentBalance = event.messageData.balance;
		
		this._startHandleEnvironmentMessages();
	}

	_startHandleEnvironmentMessages()
	{
		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onLobbyServerBalanceUpdated, this);

		if (APP.isCAFMode)
		{
			APP.on(LobbyApp.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		}
	}

	_onPlayerInfoUpdated(event)
	{
		if (this._fPlayerInfo_pi.isCAFRoomManagerDefined && !this._fIsRoomManagerChecked_bl)
		{
			if (this._fPlayerInfo_pi.isCAFRoomManager)
			{
				APP.off(LobbyApp.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
				this._stopHandleEnvironmentMessages();
				return;
			}
			else
			{
				this._fIsRoomManagerChecked_bl = true;
			}
		}

		if (event.data.isKicked !== undefined)
		{
			if (this._fPlayerInfo_pi.isKicked && this.info.isActive)
			{
				this.__deactivateDialog();
			}

			if(this._fPlayerInfo_pi.isKicked){
				this._wasKicked = true;
			}

			if(this._wasKicked == true && !this._fPlayerInfo_pi.isKicked){
				this.__activateDialog();
			}
		}
	}

	_stopHandleEnvironmentMessages()
	{
		this._fBattlegroundController_bc.off(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_REOPENED, this._reopenBuyInDialog, this);
		this._fBattlegroundController_bc.off(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_CLOSED, this._closeBuyInDialog, this);
		this._fBattlegroundController_bc.off(BattlegroundController.EVENT_ON_ROUND_PROGRESS_STATE_CHANGED, this._onBattlegroundRoundProgressStateChanged, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.off(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		let roomNotFoundDialogController = APP.dialogsController.roomNotFoundDialogController;
		roomNotFoundDialogController.off(DialogController.EVENT_DIALOG_ACTIVATED, this._onRoomNotFoundDialogActivated, this);

		APP.dialogsController.gameGameBattlegroundNoWeaponsFiredDialogController.off(
			GameBattlegroundNoWeaponsFiredDialogController.EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED, this._noWeaponsFiredPlayAgainClicked, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onLobbyServerBalanceUpdated, this);
	}

	_onBattlegroundRoundProgressStateChanged(event)
	{
		if (this._fBattlegroundController_bc.info.isRoundInProgress)
		{
			this.__deactivateDialog();
		}
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		let msgData = event.data;
		let info = this.info;

		switch (msgType)
		{
			case GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST:
				let l_bi = APP.battlegroundController.info;
				if (APP.isCAFMode && l_bi.isRoundInProgress)
				{
					break;
				}

				if(l_bi.isRoundInProgress)
				{
					break;
				}

				this.info.setMode(BattlegroundBuyInConfirmationDialogInfoCAF.MODE_ID_RE_BUY);
				this.info.setBuyInCost(l_bi.getConfirmedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn);
				if(event.data && event.data.isRRActive)
					this.view.changeButtons(BTN_TYPE.BTN_TYPE_RETURN_TO_SCOREBOARD);
				else
					this.view.changeButtons(BTN_TYPE.BTN_TYPE_RETURN_TO_LOBBY);
				//do not show when not enought money on client side, prevent server NEM error 1008
				//if(this._fPlayerInfo_pi.balance >= l_bi.getConfirmedBuyInCost())
				//{
					this.__activateDialog();

					if (this._fIsPlayerSitOutRequired_bl && msgData.alreadySitInNumber !== undefined)
					{
						this._fIsPlayerSitOutRequired_bl = msgData.alreadySitInNumber !== -1;
						this._validateOkButton();
					}
				//}
				break;
			case GAME_MESSAGES.BATTLEGROUND_BUY_IN_REOPENING_REQUIRED:
				this.view.changeButtons(BTN_TYPE.BTN_TYPE_RETURN_TO_LOBBY);
				this._reopenBuyInDialog();
				break;
			case GAME_MESSAGES.ON_GAME_STATE_CHANGED:
				if(APP.isNewMatchMakingSupported)
				{
					if(APP.isCAFMode){
						this._fIsGameStateQualify_bl = event.data.roundState == GAME_ROUND_STATE.QUALIFY;
						this._validateOkButton();
					}else{
						if(event.data.roundState == GAME_ROUND_STATE.PLAY){
							this.__deactivateDialog();
						}
					}
				}else{
					if(!APP.isCAFMode) break;
					this._fIsGameStateQualify_bl = event.data.roundState == GAME_ROUND_STATE.QUALIFY;
					this._validateOkButton();
				}

				break;
			case GAME_MESSAGES.BATTLEGROUND_ROUND_CANCELED:
				this.view.changeButtons(BTN_TYPE.BTN_TYPE_RETURN_TO_LOBBY);
				break;
			case GAME_MESSAGES.ON_SIT_OUT_REQUIRED:
				this._fIsPlayerSitOutRequired_bl = true;
				this._validateOkButton();
				break;
			case GAME_MESSAGES.ON_SIT_OUT_RESPONSE:
				if(event.data.rid != -1)
				{
					this._fIsPlayerSitOutRequired_bl = false;
					this._validateOkButton();
				}
				break;
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (msgData.requestClass === GAME_CLIENT_MESSAGES.SIT_OUT)
				{
					this._fIsPlayerSitOutRequired_bl = false;
					this._validateOkButton();

					if (
							msgData.errorCode == LobbyWebSocketInteractionController.ERROR_CODES.SIT_OUT_NOT_ALLOWED
							&& !APP.playerController.info.isObserver
							&& this.info.isActive
						)
					{
						this.__deactivateDialog();
					}
				}
				break;
		}
	}

	_validateOkButton()
	{
		if(this._fIsPlayerSitOutRequired_bl || this._fIsGameStateQualify_bl)
		{
			this.view && this.view.deactivateOkButton();
		}
		else
		{
			this.view && this.view.activateOkButton();
		}
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		var info = this.info;
		var view = this.__fView_uo;
		var messageAssetId;

		//buttons configuration...
		view.setOkCancelMode();
		//...buttons configuration

		//message configuration...
		//view.setMessage("TABattlegroundToJoinThisGame");
		//...message configuration

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog()
	{
		super.__deactivateDialog();

		this.view.hideWaitLayer();
	}

	__onDialogOkButtonClicked(event)
	{
		if(this._fIsPlayerSitOutRequired_bl || this._fIsGameStateQualify_bl) return;
		
		let l_bi = APP.battlegroundController.info;
		if(this._fCurrentBalance >= l_bi.getConfirmedBuyInCost())
		{
			super.__onDialogOkButtonClicked(event);

			clearInterval(this._fInterval)

			switch(this.info.getModeId())
			{
				case BattlegroundBuyInConfirmationDialogInfoCAF.MODE_ID_BUY_IN:
					this.emit(BattlegroundBuyInConfirmationDialogControllerCAF.EVENT_BATTLEGROUND_BUY_IN_CONFIRMED, { buyIn: this.info.getBuyInCost()} );
					this.view.showWaitLayer();
					break;
				case BattlegroundBuyInConfirmationDialogInfoCAF.MODE_ID_RE_BUY:
					APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_PLAY_AGAIN_CONFIRMED);
					this.emit(BattlegroundBuyInConfirmationDialogControllerCAF.EVENT_BATTLEGROUND_RE_BUY_CONFIRMED, { buyIn: this.info.getBuyInCost()} );

					APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_RE_BUY_CONFIRMED);
					this.__deactivateDialog();
					break;
			}

			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_PLAY_AGAIN_BUTTON_CLICKED);
		}
		else
		{
			this.emit(BattlegroundBuyInConfirmationDialogControllerCAF.EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED);
		}
	}

	__onDialogCancelButtonClicked(event)
	{
		if (this._fBattlegroundController_bc.isRoundResultWasActivated())
		{
			super.__onDialogCancelButtonClicked(event);
			this.__deactivateDialog();
			this.emit(BattlegroundBuyInConfirmationDialogControllerCAF.EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS);
		} else
		{
			super.__onGameDialogChangeWorldBuyInButtonClicked(event);
		}
	}

	_onLobbyServerBalanceUpdated(event)
	{
		this._fCurrentBalance = event.messageData.balance;
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let requestClass = undefined;
		if (requestData && requestData.rid >= 0)
		{
			requestClass = requestData.class;
		}

		if (LobbyWebSocketInteractionController.isFatalError(event.errorType))
		{
			this.info.errorCode = serverData.code;
			this.info.errorTime = serverData.date;
			this.info.rid = serverData.rid;

			this.__deactivateDialog();
		}
	}

	_onRoomNotFoundDialogActivated(event)
	{
		if (APP.isCAFMode)
		{
			this.__deactivateDialog();
		}
	}

	__activateDialog()
	{
		if(this._wasKicked == true && APP.battlegroundController.info.isBattlegroundMode() && APP.isCAFMode && !APP.playerController.info.isCAFRoomManager)
		{
			this._wasKicked = false;
		}else if(
				!APP.battlegroundController.info.isBattlegroundMode()
				|| !APP.isCAFMode
				|| APP.dialogsController.lobbyBattlegroundNotEnoughPlayersDialogController.info.isActive
				|| APP.dialogsController.cafPlayerKickedDialogController.info.isActive
				|| (APP.isCAFMode && APP.playerController.info.isCAFRoomManager)
				|| (APP.isCAFMode && this._fBattlegroundController_bc.info.isRoundInProgress && !this._fIsGameStateQualify_bl) // isCAFMode is here only to keep functionality of standard btg
			)
		{
			return;
		}

		super.__activateDialog();
		this._validateOkButton();
		if (!APP.isCAFMode) this.view.updateBuyInCostIndicator(this.info.getBuyInCost());

	}
}

export default BattlegroundBuyInConfirmationDialogControllerCAF