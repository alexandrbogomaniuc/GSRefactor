import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GamePlayersController from '../../../../gameplay/players/GamePlayersController';

class BattlegroundRejoinDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);		

		this._initBattlegroundRejoinDialogController();
	}

	_initBattlegroundRejoinDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().battlegroundRejoinDialogView;
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

		if (APP.isBattlegroundGame)
		{
            APP.gameController.gameplayController.gamePlayersController.on(GamePlayersController.EVENT_ON_INACTIVE_ROUNDS_LIMIT, this._onInactiveRoundsLimit, this)
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
		if (info.isActive)
		{
			var view = this.__fView_uo;

			view.setMessage("TAbattlgeroundDialogRejoin");
			view.setOkCancelMode();

			view.okButton.setRejoinCaption();
			view.cancelButton.setChangeBuyIn();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	_onInactiveRoundsLimit()
	{
		this.__activateDialog();
	}

	__activateDialog ()
	{
		super.__activateDialog();
	}

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);
		this.__deactivateDialog();
	}

	__onDialogCancelButtonClicked(event)
	{
		super.__onDialogCancelButtonClicked(event);
	}
}

export default BattlegroundRejoinDialogController