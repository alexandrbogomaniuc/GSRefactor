import JSEnvironmentInteractionController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/js/JSEnvironmentInteractionController';
import {APP} from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyScreen from '../../../main/LobbyScreen'
import DialogController from '../../uis/custom/dialogs/DialogController';
import DialogsController from '../../uis/custom/dialogs/DialogsController';
import LobbyWebSocketInteractionController from '../server/LobbyWebSocketInteractionController';
import LobbyExternalCommunicator, { GAME_MESSAGES } from '../../../external/LobbyExternalCommunicator';
import APPParamsInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/APPParamsInfo';

class LobbyJSEnvironmentInteractionController extends JSEnvironmentInteractionController
{
	static get EVENT_WAITING_FOR_REDIRECTION() {return "onWaitingForRedirection"};

	constructor()
	{
		super();

		this._nemDialogController = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let lobbyScreen = APP.lobbyScreen;
		lobbyScreen.on(LobbyScreen.EVENT_ON_LOBBY_CASHIER_CLICKED, this._onLobbyCashierBtnClicked, this);

		let dialogsController = APP.dialogsController;
		let nemDialogController = this._nemDialogController = dialogsController.gameNEMDialogController;
		nemDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onNEMDialogConfirmed, this);

		dialogsController.on(DialogsController.EVENT_ON_DIALOG_CHANGE_WORLD_BUY_IN_TRIGGERED, this._onDialogChangeWorldBuyInTriggered, this);
		dialogsController.lobbyNEMDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._goToHomeDirectly, this);
		dialogsController.lobbyInsufficientFundsDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._goToHomeDirectly, this);

		dialogsController.criticalErrorDialogController.on(DialogController.EVENT_ON_DLG_CUSTOM_BTN_CLICKED, this._onCriticalErrorDialogConfirmed, this);
		dialogsController.gameCriticalErrorDialogController.on(DialogController.EVENT_ON_DLG_CUSTOM_BTN_CLICKED, this._onCriticalErrorDialogConfirmed, this);
		dialogsController.lobbyRebuyDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onRebuyDialogRequestConfirmed, this);
		dialogsController.gameRebuyDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onRebuyDialogRequestConfirmed, this);
		dialogsController.roomNotFoundDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._onRoomNotFoundDialogRequestConfirmed, this);
		dialogsController.roundAlreadyFinishedDialogController.on(DialogController.EVENT_ON_DLG_CUSTOM_BTN_CLICKED, this._onRoundAlreadyFinishedDialogCustomBtnClick, this);
		dialogsController._gameNEMDialogController.on(DialogController.EVENT_REQUEST_CONFIRMED, this._goToHomeDirectly, this);
		
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
	}

	_onLobbyCashierBtnClicked(event)
	{
		// this.emit(LobbyJSEnvironmentInteractionController.EVENT_WAITING_FOR_REDIRECTION);


		if (APP.buyInFuncDefined)
		{
			this.callWindowMethod(APP.buyInFuncName);
		}
	}

	_goToHomeDirectly(event)
	{
		APP.goToHome();
	}


	_onLobbyNEMDialogConfirmed(event)
	{

		if (!APP.buyInFuncDefined)
		{
			return;
		}

		this.callWindowMethod(APP.buyInFuncName);
	}

	_onNEMDialogConfirmed(event)
	{
		if (this._nemDialogController.info.isFRBMode)
		{
			return;
		}

		if (
				this._nemDialogController.info.isFreeMoneyMode
				|| !APP.buyInFuncDefined
				|| APP.lobbyBonusController.info.isActivated
			)
		{
			return;
		}

		// this.emit(LobbyJSEnvironmentInteractionController.EVENT_WAITING_FOR_REDIRECTION);

		this.callWindowMethod(APP.buyInFuncName);
	}

	_onCriticalErrorDialogConfirmed(event)
	{
		if (APP.appParamsInfo.closeErrorFuncNameDefined)
		{
			this.callWindowMethod(APP.appParamsInfo.closeErrorFuncName);
		}
	}

	_onRebuyDialogRequestConfirmed(event)
	{
		let lTournamentModeinfo_tmi = APP.tournamentModeController.info;		
		let lAppParams = APP.appParamsInfo;

		if (
				lTournamentModeinfo_tmi.isTournamentMode
				&& (!lTournamentModeinfo_tmi.rebuyAllowed || lTournamentModeinfo_tmi.isRebuyLimitExceeded)
				&& lAppParams.homeFuncNameDefined
			)
		{
			APP.goToHome();	
		}
	}

	_onRoomNotFoundDialogRequestConfirmed(event)
	{
		let l_rnfdi = APP.dialogsController.roomNotFoundDialogController.info;
		let lAppParams = APP.appParamsInfo;

		if (
				APP.isCAFMode
				&& (
						l_rnfdi.roomSelectionErrorCode == LobbyWebSocketInteractionController.ERROR_CODES.TOO_MANY_OBSERVERS
						|| l_rnfdi.roomSelectionErrorCode == LobbyWebSocketInteractionController.ERROR_CODES.TOO_MANY_PLAYER
					)
			)
		{
			APP.goToHome();
		}
	}

	_onRoundAlreadyFinishedDialogCustomBtnClick(event)
	{
		APP.goToHome();
	}

	_onDialogChangeWorldBuyInTriggered(event)
	{
		APP.goToHome();
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.BATTLEGROUND_BACK_TO_MQB_LOBBY:
				APP.goToHome();
				break;
		}
	}

}

export default LobbyJSEnvironmentInteractionController