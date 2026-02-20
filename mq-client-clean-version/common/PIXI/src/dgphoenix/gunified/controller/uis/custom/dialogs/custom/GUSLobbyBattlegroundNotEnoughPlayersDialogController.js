import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import GUSLobbyExternalCommunicator, { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../external/GUSLobbyExternalCommunicator';
import GUSLobbyBattlegroundController from '../../../../custom/battleground/GUSLobbyBattlegroundController';
import GUSLobbyPlayerController from '../../../../custom/GUSLobbyPlayerController';
import GUSLobbyApplication from '../../../../main/GUSLobbyApplication';

class GUSLobbyBattlegroundNotEnoughPlayersDialogController extends GUDialogController
{
	static get EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING () { return "EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING" };
	
	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initGUSLobbyBattlegroundNotEnoughPlayersDialogController();
	}

	_initGUSLobbyBattlegroundNotEnoughPlayersDialogController()
	{
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().battlegroundNotEnoughPlayersDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
		
		APP.battlegroundController.on(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_REOPENED, this._reopenNotEnoughPlayersDialog, this);
		APP.battlegroundController.on(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CLOSED, this._closeNotEnoughPlayersDialog, this);

		APP.playerController.on(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.on(GUSLobbyApplication.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
	}

	_onPlayerInfoUpdated(aEvent_e)
	{
		if (this.info.isActive && this.__fView_uo)
		{
			this.__validateViewLevel();
		}
	}

	_reopenNotEnoughPlayersDialog()
	{
		this.__activateDialog();
	}

	_closeNotEnoughPlayersDialog()
	{
		this.__deactivateDialog();
	}

	_onGameMessageReceived(event)
	{
		switch (event.type)
		{
			case GAME_MESSAGES.GAME_IN_PROGRESS:
				this.__deactivateDialog();
				break;
			case GAME_MESSAGES.BATTLEGROUND_ROUND_CANCELED:
				if (event.data.isWaitState && !APP.battlegroundController.info.isConfirmBuyinDialogRequired)
				{
					this.__activateDialog();
				}
				break;
		}
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		var info = this.info;
		var view = this.__fView_uo;
		var messageAssetId;

		//buttons configuration...
		view.setOkCancelMode();

		if (APP.playerController.info.isObserver)
		{
			view.activateOkButton();
		}
		else
		{
			view.deactivateOkButton();
		}
		
		//...buttons configuration

		super.__validateViewLevel();
	}
	//...VALIDATION

	__onDialogCancelButtonClicked(event)
	{
		super.__onGameDialogChangeWorldBuyInButtonClicked(event);
	}

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);
		this.__deactivateDialog();

		this.emit(GUSLobbyBattlegroundNotEnoughPlayersDialogController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING);
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING);
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_PLAY_AGAIN_BUTTON_CLICKED);
	}

	__activateDialog()
	{
		if (APP.dialogsController.battlegroundBuyInConfirmationDialogController.info.isActive)
		{
			return;
		}

		super.__activateDialog();
	}
}

export default GUSLobbyBattlegroundNotEnoughPlayersDialogController