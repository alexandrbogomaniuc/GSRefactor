import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';
import LobbyPlayerController from '../../../../custom/LobbyPlayerController';
import PlayerInfo from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import {CLIENT_MESSAGES} from '../../../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import LobbyStateController from '../../../../state/LobbyStateController';
import LobbyExternalCommunicator from '../../../../../external/LobbyExternalCommunicator';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import BattlegroundController from '../../../../custom/battleground/BattlegroundController';
import DialogsInfo from '../../../../../model/uis/custom/dialogs/DialogsInfo';
import LobbyAPP from '../../../../../LobbyAPP';


class LobbyBattlegroundNotEnoughPlayersDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };
	static get EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING () { return "EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING" };
	static get EVENT_DIALOG_ACTIVATED()			{return DialogController.EVENT_DIALOG_ACTIVATED};
	static get EVENT_DIALOG_DEACTIVATED()		{return DialogController.EVENT_DIALOG_DEACTIVATED};

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);
		
		this._initLobbyBattlegroundNotEnoughPlayersDialogController();
	}

	_initLobbyBattlegroundNotEnoughPlayersDialogController()
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
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
		
		APP.battlegroundController.on(BattlegroundController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_REOPENED, this._reopenNotEnoughPlayersDialog, this);
		APP.battlegroundController.on(BattlegroundController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CLOSED, this._closeNotEnoughPlayersDialog, this);

		APP.playerController.on(LobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.on(LobbyAPP.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.on(LobbyAPP.EVENT_ON_OBSERVER_MODE_ACTIVATED, this._onObserverModeActivated, this);
    }

    _onObserverModeActivated(event)
    {
        this.__deactivateDialog();
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
			{
				this.__deactivateDialog();
			}
			break;
			case GAME_MESSAGES.BATTLEGROUND_ROUND_CANCELED:
				if (event.data.isWaitState && (!APP.isCAFMode || !APP.playerController.info.isKicked))
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

		this.emit(LobbyBattlegroundNotEnoughPlayersDialogController.EVENT_BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING);
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_NOT_ENOUGH_PLAYERS_CONTINUE_WAITING);
		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_PLAY_AGAIN_BUTTON_CLICKED);
	}

	__activateDialog()
	{
		if (
				APP.dialogsController.battlegroundBuyInConfirmationDialogController.info.isActive
				|| (APP.isCAFMode && APP.playerController.info.isCAFRoomManager)
			)
		{
			return;
		}

		super.__activateDialog();
	}
}

export default LobbyBattlegroundNotEnoughPlayersDialogController