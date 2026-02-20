import DialogController from '../../dialogs/DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { BACK_TYPE } from '../../../../../view/uis/custom/dialogs/custom/BattlegroundCafRoomGuestDialogView';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import { BATTLEGROUND_ROOM_STATE } from '../../../../../config/Constants';
import BattlegroundCafRoomGuestDialogView from '../../../../../view/uis/custom/dialogs/custom/BattlegroundCafRoomGuestDialogView';
import { GAME_ROUND_STATE } from '../../../../../../../shared/src/CommonConstants';
import DialogInfo from '../../../../../model/uis/custom/dialogs/DialogInfo';
import DialogsInfo from '../../../../../model/uis/custom/dialogs/DialogsInfo';
import CrashAPP from '../../../../../CrashAPP';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';
import { SERVER_MESSAGES } from '../../../../../model/interaction/server/GameWebSocketInteractionInfo';
import GamePlayersController from '../../../../gameplay/players/GamePlayersController';
import RoomController from '../../../../gameplay/RoomController';
import RoundController from '../../../../gameplay/RoundController';
import PaytableScreenController from '../../secondary/paytable/PaytableScreenController';
import SettingsScreenController from '../../secondary/settings/SettingsScreenController';
import GameplayController from '../../../../gameplay/GameplayController';
import BetsController from '../../../../gameplay/bets/BetsController';

class BattlegroundCafRoomGuestDialogController extends DialogController {
	static get EVENT_BATTLEGROUND_RE_BUY_CONFIRMED() { return "EVENT_BATTLEGROUND_RE_BUY_CONFIRMED" };
	static get EVENT_BATTLEGROUND_MANAGER_START_ROUND_CLICKED() { return "EVENT_BATTLEGROUND_MANAGER_START_ROUND_CLICKED" };
	static get EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED() { return "EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED" };
	static get EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS() { return "EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS" };
	static get EVENT_ACTIVATE() { return "EVENT_ACTIVATE_CAF_ROOM_GUEST" };
	static get EVENT_BATTLEGROUND_GUEST_READY_BUTTON_CLICKED() { return "EVENT_BATTLEGROUND_GUEST_READY_BUTTON_CLICKED" };
	static get EVENT_BATTLEGROUND_GUEST_CANCEL_READY_BUTTON_CLICKED() { return "EVENT_BATTLEGROUND_GUEST_CANCEL_READY_BUTTON_CLICKED" };



	constructor(aOptInfo_usuii, parentController) {
		super(aOptInfo_usuii, undefined, parentController);
		this._initBattlegroundCafRoomMGuestDialogController();

	}

	_initBattlegroundCafRoomMGuestDialogController() {


	}

	_eventActivate(event) {
		var doOpen = event.open;

		if (APP.isCAFRoomManager) return;

		if (this.info.isActive && doOpen) {
			return;
		}


		if (doOpen) {

			let paytableScreenController = APP.secondaryScreenController._paytableScreenController;
			paytableScreenController.off(PaytableScreenController.EVENT_ON_CLOSE_BTN_CLICKED, this._secondaryClosed, this);
			paytableScreenController.off(PaytableScreenController.EVENT_ON_SCREEN_SHOW, this._secondaryOpened, this);
			paytableScreenController.on(PaytableScreenController.EVENT_ON_CLOSE_BTN_CLICKED, this._secondaryClosed, this);
			paytableScreenController.on(PaytableScreenController.EVENT_ON_SCREEN_SHOW, this._secondaryOpened, this);

			let settingsScreenController = APP.secondaryScreenController._settingsScreenController;
			settingsScreenController.off(SettingsScreenController.EVENT_SCREEN_DEACTIVATED, this._secondaryClosed, this);
			settingsScreenController.off(SettingsScreenController.EVENT_SCREEN_ACTIVATED, this._secondaryOpened, this);
			settingsScreenController.on(SettingsScreenController.EVENT_SCREEN_DEACTIVATED, this._secondaryClosed, this);
			settingsScreenController.on(SettingsScreenController.EVENT_SCREEN_ACTIVATED, this._secondaryOpened, this);

			APP.gameController.gameplayController.off(GameplayController.REVALIDATE_CAF_ROOM_MANAGER, this._revalidateModel, this);
			APP.gameController.gameplayController.on(GameplayController.REVALIDATE_CAF_ROOM_MANAGER, this._revalidateModel, this);


			let betsController = APP.gameController.gameplayController.gamePlayersController.betsController;
			betsController.off(BetsController.EVENT_ON_BET_CANCEL_REJECTED, this._cancelBetIsRejected, this);
			betsController.off(BetsController.EVENT_ON_BET_REJECTED, this._betIsRejected, this);
			betsController.on(BetsController.EVENT_ON_BET_CANCEL_REJECTED, this._cancelBetIsRejected, this);
			betsController.on(BetsController.EVENT_ON_BET_REJECTED, this._betIsRejected, this);

			

			if (!this.secundaryOpened) {
				this.info.readyButtonClicked = false;
				this.info.cancelButtonClicked = false;
				this.__activateDialog();
			};

		} else {
			this.__deactivateDialog();
		}
	}

	_revalidateModel(event) {
		if (this.info.isActive) {
			this.info.cancelButtonClicked = false;
			this.info.readyButtonClicked = false;
		}
	}

	__init() {
		super.__init();

	}

	open() {
		this._eventActivate();
	}

	__getExternalViewForSelfInitialization() {
		return this.__getViewLevelSelfInitializationViewProvider().battlegroundCafRoomGuestDialogView;
	}

	__initModelLevel() {
		super.__initModelLevel();
	}

	__initControlLevel() {
		super.__initControlLevel();

		let roundController = this._roundController = APP.gameController.gameplayController.roundController;
		let gamePlayersController = this._gamePlayersController = APP.gameController.gameplayController.gamePlayersController;
		gamePlayersController.on(GamePlayersController.EVENT_ACTIVATE_CAF_ROOM_MANAGER, this._eventActivate, this);

		let roomController = this._roomController = APP.gameController.gameplayController.roomController;
		roomController.on(RoomController.EVENT_ACTIVATE_KICKED_DIALOG, this._iAmKicked, this);


	}

	_cancelBetIsRejected(event) {
		if (this.info.isActive) {
			this.info.readyButtonClicked = false;
			this.info.cancelButtonClicked = false;
		}

	}

	_betIsRejected(event) {
		if (this.info.isActive) {
			this.info.readyButtonClicked = false;
			this.info.cancelButtonClicked = false;
		}
	}

	_secondaryClosed() {
		if (APP.isCAFRoomManager || !this.secundaryOpened) return;
		this.secundaryOpened = false;

		let l_gpi = APP.gameController.gameplayController.info;
		let l_ri = l_gpi.roundInfo;
		if (l_ri.isRoundWaitState || l_ri.isRoundQualifyState) {
			if (!this.info.isActive) {
				this.__activateDialog();
			}

		}

	}


	_secondaryOpened() {
		if (APP.isCAFRoomManager) return;
		this.secundaryOpened = true;
		if (this.info.isActive) {
			this.__deactivateDialog();
		}
	}

	_roundStarted(event) {

	}

	__initViewLevel() {
		super.__initViewLevel();
		let view = this.__fView_uo;
		view.on(BattlegroundCafRoomGuestDialogView.EVENT_ON_READY_BTN_CLICKED, this._readyButtonClicked, this);
		view.on(BattlegroundCafRoomGuestDialogView.EVENT_ON_CANCEL_READY_BTN_CLICKED, this._cancelReadyButtonClicked, this);
	}


	_cancelReadyButtonClicked(event) {
		this.info.readyButtonClicked = false;
		this.info.cancelButtonClicked = true;
		this.emit(BattlegroundCafRoomGuestDialogController.EVENT_BATTLEGROUND_GUEST_CANCEL_READY_BUTTON_CLICKED);
	}

	_readyButtonClicked(event) {
		this.info.readyButtonClicked = true;
		this.info.cancelButtonClicked = false;
		this.emit(BattlegroundCafRoomGuestDialogController.EVENT_BATTLEGROUND_GUEST_READY_BUTTON_CLICKED);
	}

	_closeBuyInDialog() {
		// when open paytable or settings
		this.__deactivateDialog();
	}

	_iAmKicked(event) {

		const status = event.status;

		if (this.info.isActive && status == true && APP.isKicked) {
			this.info.readyButtonClicked = false;
			this.info.cancelButtonClicked = false;
			this.__deactivateDialog();
		}

		if (!this.info.isActive && status == false && !APP.isKicked && this._roundController.info.isRoundWaitState) {
			this.info.readyButtonClicked = false;
			this.info.cancelButtonClicked = false;
			this.__activateDialog();
		}
	}




	//VALIDATION...
	__validateModelLevel() {

		this.info.setIsReadyConfirmed(APP.gameController.info.battlegroundBuyInConfirmed);
		super.__validateModelLevel();


	}



	__validateViewLevel() {
		var view = this.__fView_uo;

		view.setOkCancelCustomMode();
		view.validateManagerStateButtons();
		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog() {
		if (!this.info.isActive) return;
		APP.off(CrashAPP.EVENT_ON_TICK_TIME, this._onTickTime, this); // to prevent multiple listeners
		super.__deactivateDialog();
	}



	__activateDialog() {
		if (
			!APP.isBattlegroundGame
			|| (APP.isCAFMode && APP.isCAFRoomManager)
			|| APP.isKicked
			|| this.info.isActive
			|| this._roundController.info.isRoundPlayState
		) {
			return;
		}


		APP.off(CrashAPP.EVENT_ON_TICK_TIME, this._onTickTime, this); // to prevent multiple listeners
		APP.on(CrashAPP.EVENT_ON_TICK_TIME, this._onTickTime, this);

		super.__activateDialog();





	}

	_onTickTime(aEvent_e) {
		if (!this.info.isPresented) {
			return;
		}

		if (this.view) {
			this.__validateModelLevel();
			this.__validateViewLevel();
		}
	}


}

export default BattlegroundCafRoomGuestDialogController