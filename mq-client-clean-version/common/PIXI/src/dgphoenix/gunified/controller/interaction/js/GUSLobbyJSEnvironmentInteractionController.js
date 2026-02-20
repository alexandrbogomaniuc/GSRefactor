import JSEnvironmentInteractionController from '../../../../unified/controller/interaction/js/JSEnvironmentInteractionController';
import { APP } from '../../../../unified/controller/main/globals';
import GUDialogController from '../../uis/custom/dialogs/GUDialogController';

class GUSLobbyJSEnvironmentInteractionController extends JSEnvironmentInteractionController
{
	constructor()
	{
		super();

		this._gameNEMDialogController = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let dialogsController = APP.dialogsController;
		let nemDialogController = this._gameNEMDialogController = dialogsController.gameNEMDialogController;
		nemDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onNEMDialogConfirmed, this);

		dialogsController.lobbyNEMDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onLobbyNEMDialogConfirmed, this);

		dialogsController.criticalErrorDialogController.on(GUDialogController.EVENT_ON_DLG_CUSTOM_BTN_CLICKED, this._onCriticalErrorDialogConfirmed, this);
		dialogsController.gameCriticalErrorDialogController.on(GUDialogController.EVENT_ON_DLG_CUSTOM_BTN_CLICKED, this._onCriticalErrorDialogConfirmed, this);
		dialogsController.lobbyRebuyDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onRebuyDialogRequestConfirmed, this);
		dialogsController.gameRebuyDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onRebuyDialogRequestConfirmed, this);
	}

	_onLobbyNEMDialogConfirmed()
	{
		if (!APP.buyInFuncDefined)
		{
			return;
		}

		this.callWindowMethod(APP.buyInFuncName);
	}

	_onNEMDialogConfirmed()
	{
		if (this._gameNEMDialogController.info.isFRBMode)
		{
			return;
		}

		if (
			this._gameNEMDialogController.info.isFreeMoneyMode
			|| !APP.buyInFuncDefined
			|| APP.lobbyBonusController.info.isActivated
		)
		{
			return;
		}

		this.callWindowMethod(APP.buyInFuncName);
	}

	_onCriticalErrorDialogConfirmed()
	{
		if (APP.appParamsInfo.closeErrorFuncNameDefined)
		{
			this.callWindowMethod(APP.appParamsInfo.closeErrorFuncName);
		}
	}

	_onRebuyDialogRequestConfirmed()
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
}

export default GUSLobbyJSEnvironmentInteractionController