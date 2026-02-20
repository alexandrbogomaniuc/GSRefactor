import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';
import BattlegroundController from '../../../../custom/battleground/BattlegroundController';
import { BTN_TYPE } from '../../../../../view/uis/custom/dialogs/custom/BattlegroundBuyInConfirmationDialogView';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import LobbyExternalCommunicator from '../../../../../external/LobbyExternalCommunicator';
import BattlegroundBuyInConfirmationDialogInfo from '../../../../../model/uis/custom/dialogs/custom/BattlegroundBuyInConfirmationDialogInfo';
import GameBattlegroundNoWeaponsFiredDialogController from './game/GameBattlegroundNoWeaponsFiredDialogController';
import { BATTLEGROUND_ROOM_STATE } from '../../../../../config/Constants';
import { GAME_CLIENT_MESSAGES } from '../../../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import LobbyApp from '../../../../../LobbyAPP';
import { GAME_ROUND_STATE } from '../../../../../../../shared/src/CommonConstants';


class BattlegroundBuyInConfirmationDialogController extends DialogController {
	static get EVENT_DIALOG_PRESENTED() { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED() { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };
	static get EVENT_BATTLEGROUND_BUY_IN_CONFIRMED() { return "EVENT_BATTLEGROUND_BUY_IN_CONFIRMED" };
	static get EVENT_BATTLEGROUND_RE_BUY_CONFIRMED() { return "EVENT_BATTLEGROUND_RE_BUY_CONFIRMED" };
	static get EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED() { return "EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED" };
	static get EVENT_DIALOG_ACTIVATED() { return DialogController.EVENT_DIALOG_ACTIVATED };
	static get EVENT_DIALOG_DEACTIVATED() { return DialogController.EVENT_DIALOG_DEACTIVATED };
	static get EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS() { return "EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS" };

	constructor(aOptInfo_usuii, parentController) {
		super(aOptInfo_usuii, undefined, parentController);

		this._fTournamentModeInfo_tni = null;
		this._fInterval = null;
		this._fBattlegroundController_bc = null;

		this._fCurrentBalance = null;

		this._fIsRoomManagerChecked_bl = false;

		this._initBattlegroundBuyInConfirmationDialogController();

		//this._fIsPlayerSitOutRequired_bl = false;
		this._fIsGameStateQualify_bl = false;
	}

	_initBattlegroundBuyInConfirmationDialogController() {

	}

	__init() {
		super.__init()

	}

	__getExternalViewForSelfInitialization() {
		return this.__getViewLevelSelfInitializationViewProvider().battlegroundBuyInConfirmationDialogView;
	}

	__initModelLevel() {
		super.__initModelLevel();

		this._fTournamentModeInfo_tni = APP.tournamentModeController.info;
	}

	__initControlLevel() {
		super.__initControlLevel();

		if (!APP.isBattlegroundGame || APP.isCAFMode) {
			return;
		}

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.once(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);

		this._fBattlegroundController_bc = APP.battlegroundController;
		this._fBattlegroundController_bc.on(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_REOPENED, this._reopenBuyInDialog, this);
		this._fBattlegroundController_bc.on(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_CLOSED, this._closeBuyInDialog, this);


		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);


		APP.dialogsController.gameGameBattlegroundNoWeaponsFiredDialogController.on(
			GameBattlegroundNoWeaponsFiredDialogController.EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED, this._noWeaponsFiredPlayAgainClicked, this);


		/*this._fGameStateController_gsc = APP.currentWindow.gameStateController;
		this._fGameStateController_gsc.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);*/

		// DEBUG...
		//window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG

	}


	_onGameStateChanged(event) {
		let l_gsi = this._fGameStateController_gsc.info;
		if (this.info.isActive && l_gsi.isWaitState && APP.isNewMatchMakingSupported) {
			this.__deactivateDialog();
		}
	}

	__initViewLevel() {
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

	_reopenBuyInDialog() {
		let l_bi = APP.battlegroundController.info;
		if (l_bi.isRoundInProgress && APP.isNewMatchMakingSupported) return;

		this.info.setMode(BattlegroundBuyInConfirmationDialogInfo.MODE_ID_RE_BUY);
		this.info.setBuyInCost(l_bi.getConfirmedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn);

		
		this._fullGameInfoLock = false;
		//this.view.hidePleaseWait();
		this.view.showPleaseWait();
		this._betButtonClicked = false;
		this.view.disableBetButton();
		this._awaitFullGameInfoOnReopen = true;
		this.__activateDialog();
	}

	_closeBuyInDialog() {
		this.__deactivateDialog();
	}

	_noWeaponsFiredPlayAgainClicked() {
		let l_bi = APP.battlegroundController.info;
		if (l_bi.isRoundInProgress) return;
		this.info.setMode(BattlegroundBuyInConfirmationDialogInfo.MODE_ID_RE_BUY);
		this.info.setBuyInCost(l_bi.getConfirmedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn);

		this.__activateDialog();
	}

	_onServerEnterLobbyMessage(event) {
		this._fCurrentBalance = event.messageData.balance;

		this._startHandleEnvironmentMessages();
	}

	_startHandleEnvironmentMessages() {
		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onLobbyServerBalanceUpdated, this);
	}

	_stopHandleEnvironmentMessages() {
		this._fBattlegroundController_bc.off(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_REOPENED, this._reopenBuyInDialog, this);
		this._fBattlegroundController_bc.off(BattlegroundController.EVENT_BATTLEGROUND_BUY_IN_CLOSED, this._closeBuyInDialog, this);
		this._fBattlegroundController_bc.off(BattlegroundController.EVENT_ON_ROUND_PROGRESS_STATE_CHANGED, this._onBattlegroundRoundProgressStateChanged, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.off(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		APP.dialogsController.gameGameBattlegroundNoWeaponsFiredDialogController.off(
			GameBattlegroundNoWeaponsFiredDialogController.EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED, this._noWeaponsFiredPlayAgainClicked, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onLobbyServerBalanceUpdated, this);
	}

	_onBattlegroundRoundProgressStateChanged(event) {
		if (this._fBattlegroundController_bc.info.isRoundInProgress) {
			this.__deactivateDialog();
		}
	}

	_onGameMessageReceived(event) {
		let msgType = event.type;
		let msgData = event.data;
		let info = this.info;

		switch (msgType) {
			case GAME_MESSAGES.FULL_GAME_INFO_UPDATED:
				if (this._fullGameInfoLock) {
					if (event.data.seats.length == 0) {
						this._fullGameInfoLock = false;
						this.info.setFullGameInfo(event.data);
						this.view.hidePleaseWait();
						this._betButtonClicked = false;
					}
				} else {
					this.info.setFullGameInfo(event.data);
					if(this._awaitFullGameInfoOnReopen)
					{
						this.view.hidePleaseWait();
						this._awaitFullGameInfoOnReopen = false;
					}
				}
				break;
			case GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST:
				let l_bi = APP.battlegroundController.info;
				if (l_bi.isRoundInProgress) {
					break;
				}
				this.info.setMode(BattlegroundBuyInConfirmationDialogInfo.MODE_ID_RE_BUY);
				this.info.setBuyInCost(l_bi.getConfirmedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn);
				if (event.data && event.data.isRRActive)
					this.view.changeButtons(BTN_TYPE.BTN_TYPE_RETURN_TO_SCOREBOARD);
				else
					this.view.changeButtons(BTN_TYPE.BTN_TYPE_RETURN_TO_LOBBY);
				//do not show when not enought money on client side, prevent server NEM error 1008
				//if(this._fPlayerInfo_pi.balance >= l_bi.getConfirmedBuyInCost())
				//{
				this.__activateDialog();
				/***
				 * TODO check
				 */
				/*if (this._fIsPlayerSitOutRequired_bl && msgData && msgData.alreadySitInNumber !== undefined) {
					this._fIsPlayerSitOutRequired_bl = msgData.alreadySitInNumber !== -1;
					//this._validateOkButton();
				}else if (this._fIsPlayerSitOutRequired_bl && !msgData)
				{
					this._fIsPlayerSitOutRequired_bl = false;
					this._validateOkButton();
				}*/
				//}
				break;
			case GAME_MESSAGES.BATTLEGROUND_BUY_IN_REOPENING_REQUIRED:
				//implement fix here
				this.view.changeButtons(BTN_TYPE.BTN_TYPE_RETURN_TO_LOBBY);
				this._reopenBuyInDialog();
				break;
			case GAME_MESSAGES.ON_GAME_STATE_CHANGED:
				if (event.data.roundState == GAME_ROUND_STATE.PLAY) {
					this._fullGameInfoLock = true;
					this.__deactivateDialog();
					this.info.resetStatusesForObservers();
					this.view.disableBetButton();
					this.view.showPleaseWait();
				}
				break;
			case GAME_MESSAGES.BATTLEGROUND_ROUND_CANCELED:
				this.view.changeButtons(BTN_TYPE.BTN_TYPE_RETURN_TO_LOBBY);
				break;
			case GAME_MESSAGES.ON_SIT_OUT_REQUIRED:
				//this._fIsPlayerSitOutRequired_bl = true;
				//this._validateOkButton();
				break;
			case GAME_MESSAGES.ON_SIT_OUT_RESPONSE:
				if (event.data.rid != -1) {
					this._fIsPlayerSitOutRequired_bl = false;
					//this._validateOkButton();
				}
				break;
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (msgData.requestClass === GAME_CLIENT_MESSAGES.SIT_OUT) {
					//this._fIsPlayerSitOutRequired_bl = false;
					//this._validateOkButton();

					if (
						msgData.errorCode == LobbyWebSocketInteractionController.ERROR_CODES.SIT_OUT_NOT_ALLOWED
						&& !APP.playerController.info.isObserver
						&& this.info.isActive
					) {
						this.__deactivateDialog();
					}
				}
				break;
		}
	}

	_validateOkButton() {
		if (this._fIsGameStateQualify_bl) {
			this.view && this.view.disableBetButton();
		}
	}

	//VALIDATION...
	__validateModelLevel() {
		super.__validateModelLevel();
	}

	__validateViewLevel() {
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

	__deactivateDialog() {
		APP.ticker.off("tick", this._tick, this);
		super.__deactivateDialog();

		this.view.hideWaitLayer();
	}

	__onDialogOkButtonClicked(event) {
		//if (this._fIsPlayerSitOutRequired_bl || this._fIsGameStateQualify_bl) return;
		this._betButtonClicked = true;
		let l_bi = APP.battlegroundController.info;
		if (this._fCurrentBalance >= l_bi.getConfirmedBuyInCost()) {
			super.__onDialogOkButtonClicked(event);

			clearInterval(this._fInterval)

			switch (this.info.getModeId()) {
				case BattlegroundBuyInConfirmationDialogInfo.MODE_ID_BUY_IN:
					this.emit(BattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_BUY_IN_CONFIRMED, { buyIn: this.info.getBuyInCost() });
					this.view.showWaitLayer();
					break;
				case BattlegroundBuyInConfirmationDialogInfo.MODE_ID_RE_BUY:
					APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_PLAY_AGAIN_CONFIRMED);
					this.emit(BattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_RE_BUY_CONFIRMED, { buyIn: this.info.getBuyInCost() });

					APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_RE_BUY_CONFIRMED);
					this.__deactivateDialog();
					break;
			}

			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_PLAY_AGAIN_BUTTON_CLICKED);
			this.view.disableBetButton();
		}
		else {
			this.emit(BattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED);
		}
	}

	__onDialogCancelButtonClicked(event) {
		if (this._fBattlegroundController_bc.isRoundResultWasActivated()) {
			super.__onDialogCancelButtonClicked(event);
			this.__deactivateDialog();
			this.emit(BattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS);
		} else {
			super.__onGameDialogChangeWorldBuyInButtonClicked(event);
		}
	}

	_onLobbyServerBalanceUpdated(event) {
		this._fCurrentBalance = event.messageData.balance;
	}

	_onServerErrorMessage(event) {
		let serverData = event.messageData;
		let requestData = event.requestData;
		let requestClass = undefined;
		if (requestData && requestData.rid >= 0) {
			requestClass = requestData.class;
		}

		if (LobbyWebSocketInteractionController.isFatalError(event.errorType)) {
			this.info.errorCode = serverData.code;
			this.info.errorTime = serverData.date;
			this.info.rid = serverData.rid;
			this.__deactivateDialog();
		}
	}

	__activateDialog() {
		if (
			!APP.battlegroundController.info.isBattlegroundMode()
			|| APP.dialogsController.lobbyBattlegroundNotEnoughPlayersDialogController.info.isActive
		) {
			return;
		}
		APP.ticker.off("tick", this._tick, this);
		APP.ticker.on("tick", this._tick, this);
		super.__activateDialog();
		this._validateOkButton();
	}

	_tick(event) {
		this.view.updateObservers(this.info.observers);
		if (this.info.isBtgGameReadyToStart) {
			this.view.updateTimeToStart(this.info.timeToStart);
			if (((this.info.timeToStart - Date.now()) / 1000) <= 3.5) {
				this.view.disableBetButton();
			} else {
				if (!this._fullGameInfoLock && this.info.observers.length > 0) {
					this.view.enableBetButton();
				} else {
					this.view.disableBetButton();
				}
			}
		} else {
			this.view.updateTimeToStart(undefined);
			if (!this._fullGameInfoLock && this.info.observers.length > 0) {
				this.view.enableBetButton();
			} else {
				this.view.disableBetButton();
			}
		}

		if (this._betButtonClicked || this._awaitFullGameInfoOnReopen) {
			this.view.disableBetButton();
		}
		
	}
}

export default BattlegroundBuyInConfirmationDialogController