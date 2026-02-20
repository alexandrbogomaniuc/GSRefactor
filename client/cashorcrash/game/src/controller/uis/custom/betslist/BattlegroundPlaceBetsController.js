import PlaceBetsBaseController from './PlaceBetsBaseController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import DialogController from '../dialogs/DialogController';
import BattlegroundPlaceBetsView from '../../../../view/uis/custom/placebets/battleground/BattlegroundPlaceBetsView';
import CrashAPP from '../../../../CrashAPP';
import BattlegroundGameController from '../../../main/BattlegroundGameController';
import RoundController from '../../../gameplay/RoundController';
import GameWebSocketInteractionController from '../../../interaction/server/GameWebSocketInteractionController';
import BattlegroundCafRoomManagerDialogController from '../battleground/caf/BattlegroundCafRoomManagerDialogController';
import BattlegroundCafRoomGuestDialogController from '../battleground/caf/BattlegroundCafRoomGuestDialogController';
import { SERVER_MESSAGES } from '../../../../model/interaction/server/GameWebSocketInteractionInfo';


class BattlegroundPlaceBetsController extends PlaceBetsBaseController {
	static get EVENT_ON_CHANGE_BET_BUTTON_CLICKED() { return BattlegroundPlaceBetsView.EVENT_ON_CHANGE_BET_BUTTON_CLICKED; }
	static get EVENT_ON_CAF_READY_BUTTON_CLICKED() { return "EVENT_ON_CAF_READY_BUTTON_CLICKED"; }
	static get EVENT_ON_CAF_CANCEL_READY_BUTTON_CLICKED() { return "EVENT_ON_CAF_CANCEL_READY_BUTTON_CLICKED"; }

	init() {
		super.init();
	}

	//INIT...
	constructor(...args) {
		super(...args);
	}

	__initControlLevel() {
		super.__initControlLevel();

		this._fBattlegroundGameController_bgc = APP.gameController;
		this._fBattlegroundGameInfo_bgi = this._fBattlegroundGameController_bgc.info;

		APP.dialogsController.battlegroundNotEnoughPlayersDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onBattlegroundNotEnoughPlayersDialogConfirmed, this);

		this._fBattlegroundGameController_bgc.on(BattlegroundGameController.EVENT_ON_BATTLEGROUND_BUY_IN_DEFINED, this._onBattelgroundBuyInDefined, this);

		this._fBattlegroundGameController_bgc.gameplayController.roundController.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);

		APP.on(CrashAPP.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);

		this._gamePlayersController = APP.gameController.gameplayController.gamePlayersController;


	}

	_onServerMessage(event) {
		let data = event.messageData;
		switch (data.class) {
			case SERVER_MESSAGES.CRASH_GAME_INFO:

				let wsInteractionController = APP.webSocketInteractionController;
				wsInteractionController.off(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);

				setTimeout(() => {
					if (APP.isBattlegroundGame && APP.isCAFMode) {
						this._setDefaultCaffMessageResponder();
					}

				}, 100);
				break;
		}
	}
	_setDefaultCaffMessageResponder() {

		if (APP.isCAFRoomManager) {
			console.log("inited manager responder ");
			var caffRoomManagerController = APP.dialogsController._cafRoomManagerController;
			caffRoomManagerController.on(BattlegroundCafRoomManagerDialogController.EVENT_BATTLEGROUND_MANAGER_READY_BUTTON_CLICKED, this._onCafReady, this);
			caffRoomManagerController.on(BattlegroundCafRoomManagerDialogController.EVENT_BATTLEGROUND_MANAGER_CANCEL_READY_BUTTON_CLICKED,this._cafCancelReady, this);
		} else {
			console.log("inited guest responder ");
			var caffRoomGuestController = APP.dialogsController._cafGuestController;
			caffRoomGuestController.on(BattlegroundCafRoomGuestDialogController.EVENT_BATTLEGROUND_GUEST_READY_BUTTON_CLICKED, this._onCafReady, this);
			caffRoomGuestController.on(BattlegroundCafRoomGuestDialogController.EVENT_BATTLEGROUND_GUEST_CANCEL_READY_BUTTON_CLICKED, this._cafCancelReady, this);
		}
	}

	_onCafReady(event) {
		console.log("caf ready detected and the bet is placed ");
		this.view._onPlaceBetClicked(event);
	}


	_cafCancelReady(event) {
		this.view._onCancelBetClicked(event);
	}


	_onBattelgroundBuyInDefined(event) {
		this.info.betIndex = 0;
		this.info.betValue = this._fBattlegroundGameInfo_bgi.battlegroundBetValue;
		this.info.betAutoEjectMult = undefined;
	}

	_onBattlegroundNotEnoughPlayersDialogConfirmed() {
		this.view.needPlaceBet();
	}

	__initViewLevel() {
		super.__initViewLevel();
		this.view.on(BattlegroundPlaceBetsView.EVENT_ON_CHANGE_BET_BUTTON_CLICKED, this.emit, this);
	}

	_onRoomPaused(event) {
		this.view.skipBetCancelDelay();
	}

	_onServerConnectionClosed(event) {
		this.view.skipBetCancelDelay();
	}

	_onRoundStateChanged(event) {
		this.view.adjustLayoutSettings();
	}
	//...INIT
}

export default BattlegroundPlaceBetsController