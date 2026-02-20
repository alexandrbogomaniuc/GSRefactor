import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyApplication from '../../../../main/GUSLobbyApplication';
import GUSLobbyTournamentModeController from '../../../../custom/tournament/GUSLobbyTournamentModeController';

class GUTournamentStateDialogController extends GUDialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return GUDialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return GUDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

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

	__init()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().tournamentStateDialogView;
	}

	__initViewLevel()
	{
		super.__initViewLevel();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		if (APP.lobbyAppStarted)
		{
			this._startHandleEnvironmentMessages();
		}
		else
		{
			APP.once(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		}
	}

	_onLobbyStarted()
	{
		this._startHandleEnvironmentMessages();
	}

	_startHandleEnvironmentMessages()
	{
		this._fTournamentModeController_tmc = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = this._fTournamentModeController_tmc.info;

		this._fTournamentModeController_tmc.on(GUSLobbyTournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED, this._onTournamentModeStateChanged, this);

		this.__validate();
		this._activateDialogIfRequired();
	}

	//VALIDATION...
	__validateModelLevel()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel()
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
				throw new Error(`Tournament state dialog is not available with tournament state: ${this._fTournamentModeInfo_tmi.tournamentState}`);
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

	_onTournamentModeStateChanged()
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

export default GUTournamentStateDialogController