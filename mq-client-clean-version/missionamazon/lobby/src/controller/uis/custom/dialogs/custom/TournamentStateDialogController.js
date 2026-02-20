import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyAPP from '../../../../../LobbyAPP';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';
import TournamentModeController from '../../../../custom/tournament/TournamentModeController';

class TournamentStateDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._fTournamentModeController_tmc = null;
		this._fTournamentModeInfo_tmi = null;

		this._initTournamentStateDialogController();
	}

	_initTournamentStateDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().tournamentStateDialogView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		if (APP.lobbyAppStarted)
		{
			this._startHandleEnvironmentMessages();
		}
		else
		{
			APP.once(LobbyAPP.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		}

		// DEBUG...
		// window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	//DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 107) // +
	// 	{
	// 		this.__activateDialog();
	// 	}
	// }
	//...DEBUG

	_onLobbyStarted(event)
	{
		this._startHandleEnvironmentMessages();
	}

	_startHandleEnvironmentMessages()
	{
		this._fTournamentModeController_tmc = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = this._fTournamentModeController_tmc.info;

		this._fTournamentModeController_tmc.on(TournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED, this._onTournamentModeStateChanged, this);

		this.__validate();
		this._activateDialogIfRequired();
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		var info = this.info;
		if (info.isActive)
		{
			var view = this.__fView_uo;

			let messageAssetId = undefined;
			if (this._fTournamentModeInfo_tmi.isTournamentFinished)
			{
				messageAssetId = "TADialogMessageTournamentEnded";
			}
			else if (this._fTournamentModeInfo_tmi.isTournamentCancelled)
			{
				messageAssetId = "TADialogMessageTournamentCancelled";
			}
			else
			{
				throw new Error (`Tournament state dialog is not available with tournament state: ${this._fTournamentModeInfo_tmi.tournamentState}`);
			}

			if (messageAssetId !== undefined)
			{
				view.setMessage(messageAssetId);
			}			
			view.setEmptyMode();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	_onTournamentModeStateChanged(event)
	{
		this._activateDialogIfRequired();
	}

	_activateDialogIfRequired()
	{
		if (!this._fTournamentModeInfo_tmi.isTournamentMode)
		{
			return;
		}

		if (
				this._fTournamentModeInfo_tmi.isTournamentFinished
				|| this._fTournamentModeInfo_tmi.isTournamentCancelled
			)
		{
			this.__activateDialog();
		}
	}
}

export default TournamentStateDialogController