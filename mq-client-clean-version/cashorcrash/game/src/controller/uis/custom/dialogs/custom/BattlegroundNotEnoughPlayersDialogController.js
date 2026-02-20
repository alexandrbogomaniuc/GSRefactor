import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import BottomPanelController from '../../bottom_panel/BottomPanelController';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';
import RoundController from '../../../../gameplay/RoundController';
import BattlegroundGameController from '../../../../main/BattlegroundGameController';
import BetsController from '../../../../gameplay/bets/BetsController';

class BattlegroundNotEnoughPlayersDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);		

		this._initBattlegroundNotEnoughPlayersDialogController();
	}

	_initBattlegroundNotEnoughPlayersDialogController()
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

		this._fGamePlayersController_gpsc = APP.gameController.gameplayController.gamePlayersController;

		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND, this._onCancelBattlegroundRound, this);

		let lRoundController_rc = APP.gameController.gameplayController.roundController;
		this._fRoundInfo_ri = lRoundController_rc.info;
		lRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);

		if (APP.isBattlegroundGame)
		{
			APP.gameController.on(BattlegroundGameController.EVENT_ON_NEED_NOT_ENOUGH_PLAYERS_DIALOG, this._onCancelBattlegroundRound, this);
		}
	}

	_onRoundStateChanged()
	{
		if (this._fRoundInfo_ri.isRoundPlayState)
		{
			this.__deactivateDialog();
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

			view.setMessage("TAbattlgeroundDialogNotEnoughPlayers");
			view.setAdditionalMessage("TAbattlgeroundDialogBidHasReturned");
			view.setOkCancelMode();

			view.okButton.setContinueAwaitingCaption();
			view.cancelButton.setChangeBuyIn();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	_onCancelBattlegroundRound()
	{
		if (this._fRoundInfo_ri.isRoundWaitState)
		{
			this.__activateDialog();
		}
	}

	__activateDialog ()
	{
		super.__activateDialog();

		let lBetsController_bsc = APP.gameController.gameplayController.gamePlayersController.betsController;	
		lBetsController_bsc.once(BetsController.EVENT_ON_BET_CONFIRMED, this._onAnyBetInRoom, this);
	}

	_onAnyBetInRoom(event)
	{
		this.__deactivateDialog();
	}

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		this.__deactivateDialog();
	}

	__onDialogCancelButtonClicked(event)
	{
		//TO DO redirect to lobby

		super.__onDialogCancelButtonClicked(event);
	}
}

export default BattlegroundNotEnoughPlayersDialogController