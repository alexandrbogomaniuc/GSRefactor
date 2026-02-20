import GUGameBaseDialogController from './GUGameBaseDialogController';
import { APP } from '../../../../../../../unified/controller/main/globals';
import GULobbyGameLauncher from '../../../../../../controller/main/GUSLobbyGameLauncher';
import GUSLobbyApplication from '../../../../../main/GUSLobbyApplication';

class GUGameNetworkErrorDialogController extends GUGameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return GUGameBaseDialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return GUGameBaseDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._initGameNetworkErrorDialogController();
	}

	_initGameNetworkErrorDialogController()
	{	
	}

	__init()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameNetworkErrorDialogView;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		if (APP.lobbyAppStarted)
		{
			this._startHandleGameErrors();
		}
		else
		{
			APP.once(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		}
	}

	_onLobbyStarted()
	{
		this._startHandleGameErrors();
	}

	_startHandleGameErrors()
	{
		APP.gameLauncher.on(GULobbyGameLauncher.EVENT_ON_GAME_LOADING_ERROR, this._onGameAssetsLoadingError, this);
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

			view.setMessage("TADialogMessageNetworkError");
			view.setEmptyMode();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	_onGameAssetsLoadingError()
	{
		this.__activateDialog();
	}

}

export default GUGameNetworkErrorDialogController