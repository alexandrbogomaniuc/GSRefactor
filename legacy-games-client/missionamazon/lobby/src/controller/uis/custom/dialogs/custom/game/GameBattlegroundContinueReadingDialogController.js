import DialogController from '../../DialogController';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../../interaction/server/LobbyWebSocketInteractionController';
import {CLIENT_MESSAGES} from '../../../../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import LobbyExternalCommunicator from '../../../../../../external/LobbyExternalCommunicator';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import BattlegroundController from '../../../../../custom/battleground/BattlegroundController';
import DialogsInfo from '../../../../../../model/uis/custom/dialogs/DialogsInfo';
import PaytableScreenController from '../../../secondary/paytable/PaytableScreenController';

class GameBattlegroundContinueReadingDialogController extends DialogController
{
	static get EVENT_BATTLEGROUND_CONTINUE_READING_INFO_CLICKED() 				{ return 'EVENT_BATTLEGROUND_CONTINUE_READING_INFO_CLICKED'; }
	static get EVENT_BATTLEGROUND_CONTINUE_READING_OK_CLICKED() 				{ return 'EVENT_BATTLEGROUND_CONTINUE_READING_OK_CLICKED'; }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);
		
		this._initGameBattlegroundContinueReadingDialog();
	}

	_initGameBattlegroundContinueReadingDialog()
	{
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameBattlegroundContinueReadingDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let l_psc = APP.secondaryScreenController.paytableScreenController;
		l_psc.on(PaytableScreenController.EVENT_ON_CONTINUE_READING_DIALOG_REQUIRED, this.__activateDialog, this);

		// DEBUG...
		//window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
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
	//...DEBUG

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
		//...buttons configuration

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog()
	{
		super.__deactivateDialog();

		clearInterval(this.interval);
	}

	__activateDialog()
	{
		super.__activateDialog();

		let lBattleGroundInfo_bgi = APP.battlegroundController.info;
		if(APP.battlegroundController.info.getTimeToStartInMillis() > 0)
		{
			clearInterval(this.interval);

			this.view.updateTimeIndicator(lBattleGroundInfo_bgi.getFormattedTimeToStart(false));
			this.interval = setInterval(this._tick.bind(this), 100);
		}
		else
		{
			this.view.updateTimeIndicator(lBattleGroundInfo_bgi.getFormattedTimeToStart(false));
			this.__deactivateDialog();
		}
	}

	_tick()
	{
		if (APP.battlegroundController.info.getTimeToStartInMillis() > 0)
		{
			this.view.updateTimeIndicator(APP.battlegroundController.getFormattedTimeToStart(false));

			if(APP.battlegroundController.info.getTimeToStartInMillis() >= 1500)
			{
				this.view.activateOkButton();
			}
			else
			{
				this.view.deactivateOkButton();
			}
		}
		else
		{
			this.__deactivateDialog();
		}
	}

	__onDialogOkButtonClicked(event)
	{
		if(APP.battlegroundController.info.getTimeToStartInMillis() >= 1500)
		{
			super.__onDialogCancelButtonClicked(event);

			this.__deactivateDialog();

			this.emit(GameBattlegroundContinueReadingDialogController.EVENT_BATTLEGROUND_CONTINUE_READING_OK_CLICKED);
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_CONTINUE_READING_INFO_CLICKED);
		}
	}

	__onDialogCancelButtonClicked(event)
	{
		super.__onDialogCancelButtonClicked(event);
		this.__deactivateDialog();

		this.emit(GameBattlegroundContinueReadingDialogController.EVENT_BATTLEGROUND_CONTINUE_READING_INFO_CLICKED);
	}
}

export default GameBattlegroundContinueReadingDialogController