import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import LobbyApp from '../../../../../LobbyAPP';
import LobbyExternalCommunicator from '../../../../../external/LobbyExternalCommunicator';
import { GAME_MESSAGES } from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';

class WebGLContextLostDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initWebGLContextLostDialogController();
	}

	_initWebGLContextLostDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().networkErrorDialogView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		APP.on(LobbyApp.EVENT_ON_WEBGL_CONTEXT_LOST, this._onLobbyWebglContextLost, this);

		if (APP.lobbyAppStarted)
		{
			this._startHandleGameErrors();
		}
		else
		{
			APP.once(LobbyApp.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		}

		// DEBUG...
		// window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	//DEBUG...
	/*keyDownHandler(keyCode)
	{
		if (keyCode.keyCode == 65) //a
	 	{
	 		this.__activateDialog();
		}
	}*/
	 // ...DEBUG

	_onLobbyStarted(event)
	{
		this._startHandleGameErrors();
	}

	_startHandleGameErrors()
	{
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
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

			view.setMessage("TADialogMessageWebglContextLost");
			view.setEmptyMode();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	_onLobbyWebglContextLost(event)
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

export default WebGLContextLostDialogController