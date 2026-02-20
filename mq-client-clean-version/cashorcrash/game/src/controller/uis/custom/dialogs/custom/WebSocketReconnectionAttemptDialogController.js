import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';

class WebSocketReconnectionAttemptDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initWebSocketReconnectionAttemptDialogController();
	}

	_initWebSocketReconnectionAttemptDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().webSocketReconnectionAttemptDialogView;
	}

	__initModelLevel()
	{
		super.__initModelLevel();
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onGameServerConnectionOpened, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_RECONNECTION_START, this._onGameServerReconnectionStart, this);
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
			view.setMessage("TADialogWebSocketReconnection", "TADialogWebSocketReconnectionAttempt", this.info.reconnectionCount);
			view.setEmptyMode();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__activateDialog()
	{
		super.__activateDialog();
	}

	_onGameServerReconnectionStart()
	{
		var info = this.info;
		info.increaseReconnectionCount();

		if (info.reconnectionCount > 1)
		{
			this.__activateDialog();
		}	
	}

	_onGameServerConnectionOpened()
	{
		this.info.reconnectionCount = 1;
		this.__deactivateDialog();
	}
}

export default WebSocketReconnectionAttemptDialogController