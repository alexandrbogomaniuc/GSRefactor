import JSEnvironmentInteractionController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/js/JSEnvironmentInteractionController';
import {APP} from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import DialogController from '../../uis/custom/dialogs/DialogController';
import BottomPanelController from '../../uis/custom/bottom_panel/BottomPanelController';
import RoomController from '../../gameplay/RoomController';
import GameWebSocketInteractionController from '../server/GameWebSocketInteractionController';
import { CLIENT_MESSAGES } from '../../../model/interaction/server/GameWebSocketInteractionInfo';
import BattlegroundPlaceBetsController from '../../uis/custom/betslist/BattlegroundPlaceBetsController';
import BattlegroundCAFPlayerKickedDialogController from '../../uis/custom/battleground/caf/BattlegroundCAFPlayerKickedDialogController';

class GameJSEnvironmentInteractionController extends JSEnvironmentInteractionController
{
	static get EVENT_WAITING_FOR_REDIRECTION() {return "onWaitingForRedirection"};

	constructor()
	{
		super();

		this._fNEMDialogController_nemdc = null;
		this._fBottomPanelController_bpc = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let dialogsController = APP.dialogsController;
		let nemDialogController = this._fNEMDialogController_nemdc = dialogsController.gameNEMDialogController;
		let bottomPanelController = this._fBottomPanelController_bpc = APP.gameController.bottomPanelController;
		let lGamaplayController_gpc = this._fGameplayController_gpc = APP.gameController.gameplayController;
		let lRoomController_rc = this._fRoomController_rc = lGamaplayController_gpc.roomController;
		let lGamePlayersController_gpsc = this._fGamePlayersController_gpsc = lGamaplayController_gpc.gamePlayersController;
		this._fRoundController_rc = APP.gameController.gameplayController.roundController;
		let lPlaceBetsController_pbsc = this._fPlaceBetsController_pbsc = APP.gameController.placeBetsController;

		


		dialogsController.gameNEMDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onNEMDialogConfirmed, this);
		dialogsController.criticalErrorDialogController.on(DialogController.EVENT_ON_DLG_CUSTOM_BTN_CLICKED, this._onCriticalErrorDialogConfirmed, this);

		bottomPanelController.on(BottomPanelController.EVENT_HOME_BUTTON_CLICKED, this._onBottomPanelHomeButtonClicked, this);
		bottomPanelController.on(BottomPanelController.EVENT_BACK_BUTTON_CLICKED, this._onBottomPanelBackButtonClicked, this);
		bottomPanelController.on(BottomPanelController.EVENT_HISTORY_BUTTON_CLICKED, this._onBottomPanelHistoryButtonClicked, this);

		lRoomController_rc.on(RoomController.EVENT_ON_ROOM_STATE_CHANGED, this._onRoomStateChanged, this);

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);

		if (APP.isBattlegroundGame)
		{
			lPlaceBetsController_pbsc.on(BattlegroundPlaceBetsController.EVENT_ON_CHANGE_BET_BUTTON_CLICKED, this._onChangeBetButtonClicked, this);
			dialogsController.battlegroundNotEnoughPlayersDialogController.on(DialogController.EVENT_REQUEST_NOT_CONFIRMED, this._onChangeBetButtonClicked, this);
			dialogsController.battlegroundRejoinDialogController.on(DialogController.EVENT_REQUEST_NOT_CONFIRMED, this._onChangeBetButtonClicked, this);
			if(APP.isCAFMode)
			{
				let kickedDialogController = dialogsController._cafRoomKickedController;

				if(kickedDialogController)
				{
					kickedDialogController.on(BattlegroundCAFPlayerKickedDialogController.BACK_TO_HOME, this._backToHome, this);
				}

				let roomDeactivatedController = dialogsController._battlegroundCafRoomWasDeactivatedDialogController;
				if(roomDeactivatedController)
				{
					roomDeactivatedController.on(DialogController.EVENT_REQUEST_NOT_CONFIRMED, this._backToHome, this);
				}
			}
		}
	}

	_backToHome(event)
	{
		this._callHomeMethod();
	}

	_onNEMDialogConfirmed(event)
	{
		if (
				this._fNEMDialogController_nemdc.info.isFreeMoneyMode
				|| !APP.appParamsInfo.buyInFuncDefined
			)
		{
			return;
		}

		// this.emit(GameJSEnvironmentInteractionController.EVENT_WAITING_FOR_REDIRECTION);

		this.callWindowMethod(APP.appParamsInfo.buyInFuncName);
	}

	_onCriticalErrorDialogConfirmed(event)
	{
		if (APP.appParamsInfo.closeErrorFuncNameDefined)
		{
			this.callWindowMethod(APP.appParamsInfo.closeErrorFuncName);
		}
	}

	_onBottomPanelHomeButtonClicked(event)
	{
		if (
				!this._fGamePlayersController_gpsc.info.isMasterSeatDefined
				|| !APP.webSocketInteractionController._isConnectionOpened
				|| this._fRoundController_rc.info.isRoundBuyInState
				|| this._fRoundController_rc.info.isRoundPauseState
			)
		{
			this._callHomeMethod();
		}
	}

	_onBottomPanelBackButtonClicked(event)
	{
		if (APP.isBattlegroundGame)
		{
			APP.goToHome();
		}
	}

	_onRoomStateChanged(event)
	{
		let lRoomInfo_ri = this._fRoomController_rc.info;
		if (lRoomInfo_ri.isRoomClosed && !this._fGamePlayersController_gpsc.isInactiveRoundsLimit)
		{
			this._callHomeMethod();
		}
	}

	_onServerConnectionClosed(event)
	{
		if (this._fGamePlayersController_gpsc.info.isMasterPlayerLeaveRoomTriggered)
		{
			this._callHomeMethod();
		}
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let errorType = event.errorType;

		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.BAD_REQUEST:
				if (requestData && requestData.class == CLIENT_MESSAGES.SIT_OUT)
				{
					this._callHomeMethod();
				}
				break;
		}
	}

	_callHomeMethod()
	{
		APP.goToHome();
	}

	_onBottomPanelHistoryButtonClicked(event)
	{
		let lHistoryCallback = this._fBottomPanelController_bpc.info.historyCallback;
		if (lHistoryCallback)
		{
			this.callWindowMethod(lHistoryCallback);
		}
	}

	_onChangeBetButtonClicked(event)
	{
		if (APP.isBattlegroundGame)
		{
			APP.goToHome();
		}
	}
}

export default GameJSEnvironmentInteractionController