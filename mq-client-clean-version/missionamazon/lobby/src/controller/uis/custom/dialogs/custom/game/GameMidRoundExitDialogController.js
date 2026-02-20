import GameBaseDialogController from './GameBaseDialogController';
import LobbyExternalCommunicator from '../../../../../../external/LobbyExternalCommunicator';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import {GAME_MESSAGES, LOBBY_MESSAGES} from '../../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';

class GameMidRoundExitDialogController extends GameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED() 			{return GameBaseDialogController.EVENT_DIALOG_PRESENTED};
	static get EVENT_PRESENTED_DIALOG_UPDATED() 	{return GameBaseDialogController.EVENT_PRESENTED_DIALOG_UPDATED};

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._initGameMidRoundExitDialogController();
	}

	_initGameMidRoundExitDialogController()
	{
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().midRoundExitDialogView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

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
		let info = this.info;
		if (info.isActive)
		{
			let view = this.__fView_uo;

			//message configuration...
			view.setOkCancelCustomMode();
			view._setMessage(); 
			//...message configuration
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		this.__deactivateDialog();
	}

	__deactivateDialog()
	{
		super.__deactivateDialog();

		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.MID_ROUND_EXIT_DIALOG_DEACTIVATED);
	}

	__activateDialog()
	{
		super.__activateDialog();

		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.MID_ROUND_EXIT_DIALOG_ACTIVATED);
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		switch (msgType)
		{
			case GAME_MESSAGES.MID_ROUND_EXIT_REQUIRED:
				if (!this.info.isActive)
				{
					this.__activateDialog();
				}
				break;
			case GAME_MESSAGES.GAME_ROUND_STATE_CHANGED:
				if (event.data.state != undefined && !event.data.state
					&& this.info.isActive)
				{
					this.__deactivateDialog();
				}
				break;
			case GAME_MESSAGES.RESTORED_AFTER_UNREASONABLE_REQUEST:
			case GAME_MESSAGES.BACK_TO_LOBBY:
				if (this.info.isActive)
				{
					this.__deactivateDialog();
				}
				break;
		}
	}
}

export default GameMidRoundExitDialogController