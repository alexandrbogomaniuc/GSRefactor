import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import CrashAPP from '../../../../../CrashAPP';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';
import RoundController from '../../../../gameplay/RoundController';

class ReconnectDialogController extends DialogController {
	static get EVENT_DIALOG_PRESENTED() { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED() { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController) {
		super(aOptInfo_usuii, undefined, parentController);
		this._avilableTranslations = ["TADialogMessageReconnect", "TADialogMessageReconnect1", "TADialogMessageReconnect2","TADialogMessageReconnect3","TADialogMessageReconnect4","TADialogMessageReconnect5"];

		this._initReconnectDialogController();
	}

	_initReconnectDialogController() {
	}

	__init() {
		super.__init();
	}

	__getExternalViewForSelfInitialization() {
		return this.__getViewLevelSelfInitializationViewProvider().reconnectDialogView;
	}

	__initViewLevel() {
		this._shuffleMessage();
		super.__initViewLevel();
	}

	_shuffleMessage() {
		this._reconnectionMessage = this._avilableTranslations[Math.floor(Math.random() * this._avilableTranslations.length)];
	}

	__initControlLevel() {
		super.__initControlLevel();

		let webSocketInteractionController = this._fWebSocketInteractionController_wsic = APP.webSocketInteractionController;
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onGameServerConnectionOpened, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		let lRoundController_rc = this._fRoundController_rc = APP.gameController.gameplayController.roundController;
		lRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);
	}

	//VALIDATION...
	__validateModelLevel() {
		super.__validateModelLevel();
	}

	__validateViewLevel() {
		var info = this.info;
		if (info.isActive) {
			var view = this.__fView_uo;

			view.setMessage(this._reconnectionMessage);
			view.setEmptyMode();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	_onGameServerConnectionOpened(event) {
		APP.on(CrashAPP.EVENT_ON_TICK_TIME, this._onTickTime, this);
	}

	_onTickTime(event) {
		let l_wsi = this._fWebSocketInteractionController_wsic.info;
		if (l_wsi.isConnectionOpenClientTimeStampDefined) {
			let lCurClientTimeStamp_int = Date.now();

			if (lCurClientTimeStamp_int - l_wsi.connectionOpenClientTimeStamp > 500) {
				APP.off(CrashAPP.EVENT_ON_TICK_TIME, this._onTickTime, this);

				this.__activateDialog();
			}
		}
	}

	__activateDialog() {
		this._shuffleMessage();
		this.__validateViewLevel();
		super.__activateDialog();
	}

	__deactivateDialog() {
		super.__deactivateDialog();
	}

	_onGameServerConnectionClosed(event) {
		APP.off(CrashAPP.EVENT_ON_TICK_TIME, this._onTickTime, this);

		if (event.wasClean) {
			return;
		}

		this.__activateDialog();
	}

	_onRoundStateChanged(event) {
		if (this._fRoundController_rc.info.isRoundQualifyState) {
			// should not interrupt tick handle, dialog activation is still possible
		}
		else {
			APP.off(CrashAPP.EVENT_ON_TICK_TIME, this._onTickTime, this);

			if (!this._fWebSocketInteractionController_wsic.recoveringConnectionInProgress) {
				this.__deactivateDialog(); // to deactivate dialog after reconnection or after entering the game
			}
		}


	}

	_onServerErrorMessage(event) {
		APP.off(CrashAPP.EVENT_ON_TICK_TIME, this._onTickTime, this);

		let serverData = event.messageData;
		let requestData = event.requestData;

		switch (serverData.code) {
			case GameWebSocketInteractionController.ERROR_CODES.SERVER_SHUTDOWN:
			case GameWebSocketInteractionController.ERROR_CODES.TOO_MANY_OBSERVERS:
			case GameWebSocketInteractionController.ERROR_CODES.TOO_MANY_PLAYER:
				this.__activateDialog();
				break;
			default:
				this.__deactivateDialog(); // to deactivate dialog after reconnection
				break;
		}
	}

}

export default ReconnectDialogController