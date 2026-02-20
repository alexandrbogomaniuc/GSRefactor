import SimpleUIController from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController";
import { APP } from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import {GAME_MESSAGES} from '../../../../external/GameExternalCommunicator';
import BattlegroundTutorialView from "../../../../view/uis/custom/tutorial/BattlegroundTutorialView";
import { SEATS_POSITION_IDS } from "../../../../main/GameField";
import BattlegroundCountDownDialogController from "../gameplaydialogs/custom/BattlegroundCountDownDialogController";
import Game from '../../../../Game';
import PlayerInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import GameStateController from '../../../state/GameStateController';
import GameScreen from '../../../../main/GameScreen';
import { DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION, DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS } from '../../../../../../shared/src/CommonConstants';

class BattlegroundTutorialController extends SimpleUIController
{
	static get EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED()		{ return "onTutorialStateChanged"; }
	static get VIEW_APPEARING()									{ return "onTutorialAppearing"; }
	static get VIEW_HIDDEN()									{ return BattlegroundTutorialView.VIEW_HIDDEN; }

	constructor()
	{
		super(null, new BattlegroundTutorialView());

		this._fIsShowAgain_bl = false;
		this._fIsOnTutorialAlreadyDisplayed_bl = false;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		if (!APP.isBattlegroundGame)
		{
			return;
		}

		this._fGameStateController_gsc = APP.gameScreen.gameStateController;
		this._fGameStateInfo_gsi = this._fGameStateController_gsc.info;

		let l_bcddc = APP.gameScreen.gameplayDialogController.gameBattlegroundCountDownDialogController;
		l_bcddc.on(BattlegroundCountDownDialogController.EVENT_COUNT_DOWN_DIALOG_ACTIVATED, this._onCountDownDialogActivated, this);
		l_bcddc.on(BattlegroundCountDownDialogController.EVENT_COUNT_DOWN_DIALOG_DEACTIVATED, this._onCountDownDialogDeactivated, this);

		APP.on(Game.EVENT_ON_GAME_SECONDARY_SCREEN_ACTIVATED, this._onSecondaryScreenActivated, this);
		APP.on(Game.EVENT_ON_GAME_SECONDARY_SCREEN_DEACTIVATED, this._onSecondaryScreenDeactivated, this);
		APP.on(Game.EVENT_ON_PLAYER_INFO_UPDATED, this._onLobbyPlayerInfoUpdated, this);

		this._fGameStateController_gsc.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);

		APP.gameScreen.on(GameScreen.EVENT_ON_DIALOG_ACTIVATED, this._onDialogActivated, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);

		this._updateShowAgainStateIfRequired(APP.playerController.info.toolTipEnabled);
	}

	get isTutorialDisplayed()
	{
		return this.view && this.view.visible;
	}

	set showAgain(aValue)
	{
		this._fIsShowAgain_bl = aValue;
	}

	get showAgain()
	{
		return this._fIsShowAgain_bl;
	}

	initView(aContainer)
	{
		this._fViewContainer_sprt = aContainer;
		this._fViewContainer_sprt.addChild(this.view);

		this.view.on(BattlegroundTutorialView.DO_NOT_SHOW_AGAIN_BUTTON_CLICKED, this._onShowAgainClicked, this);
		this.view.visible = false;
		this.view.on(BattlegroundTutorialView.VIEW_HIDDEN, this.emit, this);
	}

	_showOrHideTutorial(aOptAutoHide_bl=false)
	{
		if (this.showAgain)
		{
			this._showTutorialIfNeeded(aOptAutoHide_bl);
		}
		else
		{
			this._hideTutorial();
		}
	}

	_showTutorialIfNeeded(aOptAutoHide_bl=false)
	{
		if (!this.view)
		{
			return;
		}

		let l_gf = APP.gameScreen.gameField;
		let l_pi = APP.playerController.info;

		if (
				this.showAgain
				&& (!this.isTutorialDisplayed || this.view.isDisappearingInProgress)
				&& !this._fIsOnTutorialAlreadyDisplayed_bl
				&& l_gf && l_gf.spot
				&& !l_gf.roundResultActive
				&& !APP.isDialogActive(DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS)
				&& !APP.isDialogActive(DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION)
				&& !(l_pi.isCAFRoomManager && !this._fGameStateInfo_gsi.isPlayState)
			)
		{	
			let l_obj = {
					positionId: 		SEATS_POSITION_IDS[l_pi.seatId],
					mainSpot: 			l_gf.spot.getBounds(),
					isSpotAtBottom:		l_gf.spot.isBottom,
					SWPanel: 			l_gf.scoreboardController.view.getBounds(),
					autoTargetSwitcher:	APP.gameScreen.autoTargetingSwitcherController.view.getBounds()
				}

			this.view && this.view.i_startAppearingAnimation(l_obj, aOptAutoHide_bl);
			this.view.visible = true;
			this._fIsOnTutorialAlreadyDisplayed_bl = !!this._fGameStateInfo_gsi.isPlayState;

			this.emit(BattlegroundTutorialController.VIEW_APPEARING);
		}
		else if (!this.showAgain)
		{
			this._hideTutorial();
		}
	}

	_hideTutorial()
	{
		if (this.isTutorialDisplayed)
		{
			this.view.i_hideTutorial();
		}
	}
	
	_onCountDownDialogActivated()
	{
		this._showOrHideTutorial(false);
	}

	_onCountDownDialogDeactivated()
	{
		this._hideTutorial();
	}

	_onSecondaryScreenActivated()
	{
		this._hideTutorial();
	}

	_onSecondaryScreenDeactivated()
	{
		this._showOrHideTutorial(true);
	}

	_onLobbyPlayerInfoUpdated(aEvent_obj)
	{
		let lData_obj = aEvent_obj.data;
		if (lData_obj[PlayerInfo.KEY_TOOL_TIP_ENABLED])
		{
			this._fIsOnTutorialAlreadyDisplayed_bl = false;
			
			let lValue_bln = lData_obj[PlayerInfo.KEY_TOOL_TIP_ENABLED].value;
			this._updateShowAgainStateIfRequired(lValue_bln);
		}
	}

	_onGameStateChanged()
	{
		this._fIsOnTutorialAlreadyDisplayed_bl = !!this._fGameStateInfo_gsi.isPlayState;
	}

	_onDialogActivated(aEvent_e)
	{
		let lDlgId_int = aEvent_e.dialogId;
		if (
				lDlgId_int === DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS
				|| lDlgId_int === DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION
			)
		{
			this._hideTutorial();
		}
	}

	_onShowAgainClicked(aEvent_e)
	{
		this._updateShowAgainStateIfRequired(!aEvent_e.isDontShowAgainActive);
	}

	_updateShowAgainStateIfRequired(aNewState_bl)
	{
		aNewState_bl = !!aNewState_bl;

		if (this.showAgain === aNewState_bl)
		{
			return;
		}

		this.showAgain = aNewState_bl;

		this.emit(BattlegroundTutorialController.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED, {value: this.showAgain});
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_TUTORIAL_STATE_CHANGED, {value: this.showAgain});
	}

	_onRoomPaused()
	{
		this.view.visible = false;
	}

	destroy()
	{
		super.destroy();

		this._fIsShowAgain_bl = undefined;
		this._fIsOnTutorialAlreadyDisplayed_bl = undefined;
	}
}
export default BattlegroundTutorialController;