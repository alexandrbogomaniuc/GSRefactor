import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import AutoTargetingSwitcherInfo from '../../../model/uis/targeting/AutoTargetingSwitcherInfo';
import AutoTargetingSwitcherView from '../../../view/uis/targeting/AutoTargetingSwitcherView';
import PlayerSpot from '../../../main/playerSpots/PlayerSpot';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameField from '../../../main/GameField';
import TargetingController from './TargetingController';
import GameStateController from '../../state/GameStateController';
import { ROUND_STATE } from '../../../model/state/GameStateInfo';
import GameScreen from '../../../main/GameScreen';
import FireSettingsController from '../fire_settings/FireSettingsController';

class AutoTargetingSwitcherController extends SimpleUIController {

	static get EVENT_ON_AUTO_TARGETING_SWITCHED() { return 'EVENT_ON_AUTO_TARGETING_SWITCHED'; }

	update()
	{
		this._validate();
	}

	constructor()
	{
		super(new AutoTargetingSwitcherInfo(), new AutoTargetingSwitcherView());
		this._gameScreen = null;
	}

	__init()
	{
		super.__init();

		this._gameScreen = APP.currentWindow;

		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_UNPAUSED, this._onRoomUnpaused, this);
		this._gameScreen.on(GameScreen.EVENT_ON_GAME_FIELD_SCREEN_CREATED, this._onGameFieldScreenCreated, this);

		this._gameScreen.fireSettingsController.on(FireSettingsController.EVENT_ON_FIRE_SETTINGS_CHANGED, this._onFireSettingsChanged, this);

		this._fTargetingController_tc = APP.currentWindow.targetingController;
		this._fTargetingController_tc.on(TargetingController.EVENT_ON_TARGET_UPDATED, this._onTargetUpdated, this);

		this.view.on(AutoTargetingSwitcherView.EVENT_ON_CLICK, this._onViewClick, this);	

		//switch off when round end
		this._fGameStateController_gsc = this._gameScreen.gameStateController;
		this._fGameStateController_gsc.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
		this._fGameStateController_gsc.on(GameStateController.EVENT_ON_PLAYER_SEAT_STATE_CHANGED, this._onGamePlayerStateChanged, this);
	}

	_addViewIfRequired()
	{
		if (this.view.parent)
		{
			return;
		}
		this.view.i_addToContainerIfRequired(this._gameScreen.gameField.autoTargetingSwitcherContainerInfo);
	}

	_onViewClick(e)
	{
		this._switchState();
	}

	_switchState()
	{
		this.info.switchState();
		this.view.i_updateState(this.info.state);

		this.emit(AutoTargetingSwitcherController.EVENT_ON_AUTO_TARGETING_SWITCHED, {on: this.info.isOn});
	}

	_onTargetUpdated(e)
	{
		if (this.info.isOn && APP.isAutoFireMode)
		{
			this._switchState();
		}
	}

	_onGamePlayerStateChanged(e)
	{
		this._validate();
	}

	_onGameStateChanged(e)
	{
		this._validate();
	}

	_onFireSettingsChanged()
	{
		this._validate();
	}

	_validate()
	{
		if (this._gameScreen && this._gameScreen.gameField.isGameplayStarted())
		{
			this._addViewIfRequired();
		}

		let lGameState_str = this._fGameStateController_gsc.info.gameState;
		if (this.info.isOn && lGameState_str !== ROUND_STATE.PLAY)
		{			
			this._switchState();
		}

		if (lGameState_str !== ROUND_STATE.PLAY || !this._gameScreen.fireSettingsController.info.lockOnTarget)
		{
			this.view.i_disable();
		}
		else
		{		
			this.view.i_enable();
		}	

		this.view.visible = this._fGameStateController_gsc.info.isPlayerSitIn;
	}

	_onRoomUnpaused(e)
	{
		if (this.info.isOn)
		{
			this._switchState();
		}
	}

	_onGameFieldScreenCreated(e)
	{
		this._validate();
	}

	destroy()
	{
		if (this._gameScreen)
		{
			this._gameScreen.off(GameScreen.EVENT_ON_ROOM_UNPAUSED, this._onRoomUnpaused, this);
			this._gameScreen.oFF(GameScreen.EVENT_ON_GAME_PLAY_STARTED, this._onGameFieldScreenCreated, this);
			this._gameScreen.fireSettingsController.off(FireSettingsController.EVENT_ON_FIRE_SETTINGS_CHANGED, this._onFireSettingsChanged, this);
		}
		this._gameScreen = null;

		this._fTargetingController_tc && this._fTargetingController_tc.off(TargetingController.EVENT_ON_TARGET_UPDATED, this._onTargetUpdated, this);
		this._fTargetingController_tc = null;

		this._fGameStateController_gsc && this._fGameStateController_gsc.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
		this._fGameStateController_gsc && this._fGameStateController_gsc.off(GameStateController.EVENT_ON_PLAYER_SEAT_STATE_CHANGED, this._onGamePlayerStateChanged, this);
		this._fGameStateController_gsc = null;

		super.destroy();
	}
}

export default AutoTargetingSwitcherController