import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyApplication from '../../../../main/GUSLobbyApplication';
import GUSLobbyExternalCommunicator from '../../../../external/GUSLobbyExternalCommunicator';
import { GAME_MESSAGES } from '../../../../external/GUSExternalCommunicator';

class GUWebGLContextLostDialogController extends GUDialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return GUDialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return GUDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);
	}

	__init()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().webglContextLostDialogView;
	}

	__initViewLevel()
	{
		super.__initViewLevel();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(GUSLobbyApplication.EVENT_ON_WEBGL_CONTEXT_LOST, this._onLobbyWebglContextLost, this);

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
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
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

			view.setMessage("TADialogMessageWebglContextLost");
			view.setEmptyMode();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	_onLobbyWebglContextLost()
	{
		this.__activateDialog();
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.WEBGL_CONTEXT_LOST:
				this.__activateDialog();
				break;
		}
	}
}

export default GUWebGLContextLostDialogController