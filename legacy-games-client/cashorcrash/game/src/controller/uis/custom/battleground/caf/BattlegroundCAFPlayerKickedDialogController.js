import DialogController from '../../dialogs/DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';
import RoomController from '../../../../gameplay/RoomController';

class BattlegroundCAFPlayerKickedDialogController extends DialogController {
	static get EVENT_DIALOG_PRESENTED() { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED() { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };
	static get EVENT_DIALOG_ACTIVATED() { return DialogController.EVENT_DIALOG_ACTIVATED };
	static get EVENT_DIALOG_DEACTIVATED() { return DialogController.EVENT_DIALOG_DEACTIVATED };
	static get BACK_TO_HOME()			  { return "BACK TO HOME "}
	constructor(aOptInfo_usuii, parentController) {
		super(aOptInfo_usuii, undefined, parentController);
	}

	__init() {
		super.__init()

	}

	__getExternalViewForSelfInitialization() {
		return this.__getViewLevelSelfInitializationViewProvider().cafPlayerKickedDialogView;
	}

	__initModelLevel() {
		super.__initModelLevel();
	}

	__initControlLevel() {
		super.__initControlLevel();

		if (!APP.isCAFMode) {
			return;
		}

		let roomController = this._gamePlayersController = APP.gameController.gameplayController.roomController;
		roomController.on(RoomController.EVENT_ACTIVATE_KICKED_DIALOG, this._eventActivate, this);

	}

	_eventActivate(event) {

		var doOpen = event.status;
		if (APP.isCAFRoomManager) return;


		if (doOpen) {
			if (!this.info.isActive) {
				this.__activateDialog();
			}
		} else {
			if (this.info.isActive) {
				this.__deactivateDialog();
			}
		}

	}

	__initViewLevel() {
		super.__initViewLevel();
	}


	_onLobbyServerConnectionClosed(event) {
		this.__deactivateDialog();
	}



	//VALIDATION...
	__validateModelLevel() {
		super.__validateModelLevel();
	}

	__validateViewLevel() {
		var view = this.__fView_uo;
		view.setCancelMode();
		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog() {
		super.__deactivateDialog();
	}

	__onDialogCancelButtonClicked(event) {
		this.emit(BattlegroundCAFPlayerKickedDialogController.BACK_TO_HOME);
	}

	_onServerErrorMessage(event) {
		if (GameWebSocketInteractionController.isFatalError(event.errorType)) {
			this.__deactivateDialog();
		}
	}

	__activateDialog() {
		const isCafMode = APP.isCAFMode;
		const isKicked = APP.isKicked;
		const isCAFRoomManager = APP.isCAFRoomManager;
		if (
			!isCafMode
			|| !isKicked
			|| isCAFRoomManager
		) {
			return;
		}
		super.__activateDialog();
	}
}

export default BattlegroundCAFPlayerKickedDialogController