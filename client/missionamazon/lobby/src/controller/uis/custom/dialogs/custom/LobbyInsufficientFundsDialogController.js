import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';
import LobbyPlayerController from '../../../../custom/LobbyPlayerController';
import PlayerInfo from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import { CLIENT_MESSAGES } from '../../../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import LobbyStateController from '../../../../state/LobbyStateController';
import LobbyScreen from '../../../../../main/LobbyScreen';
import LobbyExternalCommunicator from '../../../../../external/LobbyExternalCommunicator';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import BattlegroundBuyInConfirmationDialogController from './BattlegroundBuyInConfirmationDialogController';
import BattlegroundCafRoomManagerDialogController from './BattlegroundCafRoomManagerDialogController';
import BattlegroundBuyInConfirmationDialogControllerCAF from './BattlegroundBuyInConfirmationDialogControllerCAF';

class LobbyInsufficientFundsDialogController extends DialogController {
	static get EVENT_DIALOG_PRESENTED() { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED() { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController) {
		super(aOptInfo_usuii, undefined, parentController);

		this._fTournamentModeInfo_tni = null;

		this._initLobbyInsufficientFundsDialogController();
	}

	_initLobbyInsufficientFundsDialogController() {
	}

	__init() {
		super.__init();
	}

	__getExternalViewForSelfInitialization() {
		return this.__getViewLevelSelfInitializationViewProvider().insufficientFundsDialogView;
	}

	__initModelLevel() {
		super.__initModelLevel();

		this._fTournamentModeInfo_tni = APP.tournamentModeController.info;
	}

	__initControlLevel() {
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.once(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
		APP.dialogsController.battlegroundBuyInConfirmationDialogControllerCAF.on(BattlegroundBuyInConfirmationDialogControllerCAF.EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED, this._onDialogActivationRequired, this);
		APP.dialogsController.battlegroundBuyInConfirmationDialogController.on(BattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED, this._onDialogActivationRequired, this);
		APP.dialogsController.battlegroundCafRoomManagerDialogController.on(BattlegroundCafRoomManagerDialogController.EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED, this._onDialogActivationRequired, this);

		// DEBUG...
		//window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	//DEBUG...
	/*keyDownHandler(keyCode)
	{
		if (keyCode.keyCode == 66) //b
		{
			this.__activateDialog();
		}
	}*/
	//...DEBUG

	_onServerEnterLobbyMessage(event) {
		this._startHandleEnvironmentMessages();
	}

	_onGameMessageReceived(event) {
		switch (event.type) {
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				{
					if (event.data.errorCode == LobbyWebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS) {
						this.info.setOkMode();
						this.__activateDialog();
					}
					else if (LobbyWebSocketInteractionController.isGeneralError(event.data.errorType)) {
						this._handleGameGeneralError(event.data.errorCode, event.data.requestClass);
					}
				}
				break;
			case GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST:
				{
					//let l_bi = APP.battlegroundController.info;
					//show when not enought money on client side, prevent server NEM error 1008
					//if(this._fPlayerInfo_pi.balance < l_bi.getConfirmedBuyInCost())
					//{
					//	this.__activateDialog();
					//}
				}
				break;
		}
	}

	_handleGameGeneralError(errorCode, requestClass) {
		let supported_codes = LobbyWebSocketInteractionController.ERROR_CODES;
		switch (errorCode) {
			case supported_codes.NOT_ENOUGH_MONEY:
				{
					if (APP.isBattlegroundGamePlayMode) {
						this.info.setCustomMode();
						this.__activateDialog();//show on battleground re-buy NEM
					}
				}
		}
	}

	//VALIDATION...
	__validateModelLevel() {
		super.__validateModelLevel();
	}

	__validateViewLevel() {
		var info = this.info;
		var view = this.__fView_uo;

		//buttons configuration...

		view.setOkMode();
		view.okButton.setOKCaption();
		

		//view.setOkMode();
		//...buttons configuration

		//message configuration...
		view.setMessage("TADialogMessageErrorInsufficientFunds");
		//...message configuration

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog() {
		super.__deactivateDialog();
	}

	__onDialogOkButtonClicked(event) {
		super.__onDialogOkButtonClicked(event);
		this.view.okButton.enabled = false;
		this.view.okButton.alpha = 0.5;

	}

	__onDialogCustomButtonClicked(event) {
		super.__onDialogCustomButtonClicked(event);

		this.__deactivateDialog();
	}

	_startHandleEnvironmentMessages() {

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);

		let lLobbyStateController_lsc = APP.lobbyStateController;
		lLobbyStateController_lsc.on(LobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyVisibilityChanged, this);

		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;

		this._fPlayerController_pc.on(LobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
	}

	_onServerErrorMessage(event) {
		let serverData = event.messageData;
		let requestData = event.requestData;
		let requestClass = undefined;
		if (requestData && requestData.rid >= 0) {
			requestClass = requestData.class;
		}

		switch (serverData.code) {
			case LobbyWebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS:
				this.info.setOkMode();
				this.__activateDialog();
				break;
			/*
			case LobbyWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY:
				if (requestClass == CLIENT_MESSAGES.RE_BUY)
				{
					this.__activateDialog();
				}
				break;
			*/
		}
	}

	_onDialogActivationRequired() {
		this.info.setOkMode();
		this.__activateDialog();
	}

	_onPlayerInfoUpdated(event) {
		if (event.data[PlayerInfo.KEY_BALANCE]) {
			let curBalanceValue = event.data[PlayerInfo.KEY_BALANCE].value;

			if (curBalanceValue >= APP.battlegroundController.info.getSelectedBuyInCost()) {
				this.__deactivateDialog();
			}
		}
	}

	_onLobbyVisibilityChanged(event) {
		if (event.visible) {
		}
		else {
			this.__deactivateDialog();
		}
	}

	_onLobbyServerConnectionOpened(event) {
		this.__deactivateDialog();
	}

	_onLobbyServerConnectionClosed(event) {
		this.__deactivateDialog();
	}
}

export default LobbyInsufficientFundsDialogController