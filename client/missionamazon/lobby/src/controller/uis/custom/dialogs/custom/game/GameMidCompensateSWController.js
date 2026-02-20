import GameBaseDialogController from './GameBaseDialogController';
import LobbyExternalCommunicator from '../../../../../../external/LobbyExternalCommunicator';
import LobbyAPP from '../../../../../../LobbyAPP';
import LobbyScreen from '../../../../../../main/LobbyScreen';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import {GAME_MESSAGES} from '../../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';

class GameMidCompensateSWController extends GameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED() 			{return GameBaseDialogController.EVENT_DIALOG_PRESENTED};
	static get EVENT_PRESENTED_DIALOG_UPDATED() 	{return GameBaseDialogController.EVENT_PRESENTED_DIALOG_UPDATED};

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._initGameMidCompensateSWController();
	}

	_initGameMidCompensateSWController()
	{
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().midCompensateSWDialogView;
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

		APP.once(LobbyAPP.EVENT_ON_LOBBY_STARTED, this._onLobbyAppStarted, this);
	}

	_onLobbyAppStarted(event)
	{
		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_PSEUDO_GAME_COMPENSATION_REQUIRED, this._onPseudoGameCompensationRequired, this);
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
			view.setMessage("TADialogCompensateSW");
			view.setOkMode();
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

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		switch (msgType)
		{
			case GAME_MESSAGES.MID_ROUND_COMPENSATE_SW_REQUIRED:
				{
					this._showDialogIfRequired(event.data);
					break;
				}
			case GAME_MESSAGES.RESTORED_AFTER_UNREASONABLE_REQUEST:
			case GAME_MESSAGES.BACK_TO_LOBBY:
				if (this.info.isActive)
				{
					this.__deactivateDialog();
				}
				break;
		}
	}

	_onPseudoGameCompensationRequired(event)
	{
		this.info.resitOutInProcess = true;
		this._showDialogIfRequired(event.data);
	}

	_showDialogIfRequired(data)
	{
		if (!this.info.isActive)
		{
			this.info.compensateSpecialWeapons = data.compensateSpecialWeapons || 0;
			this.info.totalReturnedSpecialWeapons = data.totalReturnedSpecialWeapons || 0; 
			this.info.roomId = data.roomId || -1;
			
			this.__activateDialog();
			this.info.resitOutInProcess = false;
		}
	}
}

export default GameMidCompensateSWController