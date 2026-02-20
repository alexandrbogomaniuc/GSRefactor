import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import CrashAPP from '../../../../../CrashAPP';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';
import { CLIENT_MESSAGES } from '../../../../../model/interaction/server/GameWebSocketInteractionInfo';

class TooLateEjectDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initTooLateEjectDialogController();
	}

	_initTooLateEjectDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().tooLateEjectDialogView;
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
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onGameServerConnectionOpened, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);
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

			view.setMessage("TADialogTooLateEject");
			view.setCancelMode();

			view.cancelButton.setOKCaption();
			
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__activateDialog ()
	{
		super.__activateDialog();
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;

		if (
				serverData.code === GameWebSocketInteractionController.ERROR_CODES.BET_NOT_FOUND
				&& requestData
				&& (
						requestData.class === CLIENT_MESSAGES.CRASH_CANCEL_BET
						&& !!requestData.placeNewBet
					)
			)
		{
			// should not activate dialog due to https://jira.dgphoenix.com/browse/CRG-564
			// this.__activateDialog();
		}
	}

	_onGameServerConnectionOpened(event)
	{
		this.__deactivateDialog();
	}

	_onGameServerConnectionClosed(event)
	{
		this.__deactivateDialog();
	}
}

export default TooLateEjectDialogController