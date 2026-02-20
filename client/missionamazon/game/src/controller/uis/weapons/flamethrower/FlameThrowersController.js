import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import FlameThrowersInfo from '../../../../model/uis/weapons/flamethrower/FlameThrowersInfo';
import FlameThrowersView from '../../../../view/uis/weapons/flamethrower/FlameThrowersView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameScreen from '../../../../main/GameScreen';
import GameField from '../../../../main/GameField';
import { WEAPONS } from '../../../../../../shared/src/CommonConstants';
import ShotResultsUtil from '../../../../main/ShotResultsUtil';
import Weapon from '../../../../main/playerSpots/Weapon';

class FlameThrowersController extends SimpleUIController {

	static get EVENT_ON_BEAM_ROTATION_UPDATED() 					{ return FlameThrowersView.EVENT_ON_BEAM_ROTATION_UPDATED; }
	static get EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED() 	{ return 'EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED'; }
	static get EVENT_ON_HIT_ANIMATION_STARTED() 					{ return FlameThrowersView.EVENT_ON_HIT_ANIMATION_STARTED; }
	static get EVENT_ON_HIT_ANIMATION_COMPLETED() 					{ return FlameThrowersView.EVENT_ON_HIT_ANIMATION_COMPLETED; }
	
	constructor()
	{
		super(new FlameThrowersInfo(), new FlameThrowersView());

		this._gameScreen = null;
	}

	__init()
	{
		super.__init();

		this._gameScreen = APP.currentWindow;

		this._gameScreen.once(GameScreen.EVENT_ON_READY, this._onGameScreenReady, this);

		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_UNPAUSED, this._onRoomUnpaused, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_FIELD_CLEARED, this._onRoomFieldCleared, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();
		this.view.on(FlameThrowersView.EVENT_ON_BEAM_BASIC_ANIMATION_COMPLETED, this._onSomeBeamBasicAnimationCompleted, this);
		this.view.on(FlameThrowersView.EVENT_ON_BEAM_ROTATION_UPDATED, this.emit, this);
		this.view.on(FlameThrowersView.EVENT_ON_HIT_ANIMATION_STARTED, this.emit, this);
		this.view.on(FlameThrowersView.EVENT_ON_HIT_ANIMATION_COMPLETED, this.emit, this);
	}

	_onGameScreenReady(aEvent_obj)
	{
		this._gameScreen.gameField.on(GameField.EVENT_ON_GAME_FIELD_SCREEN_CREATED, this._onGameFieldScreenCreated, this);
		this._gameScreen.gameField.on(GameField.EVENT_SHOW_FIRE, this._onTimeToShowFire, this);
	}

	_onGameFieldScreenCreated(aEvent_obj)
	{
		this.view.i_init();
	}

	_onRoomPaused(aEvent_obj)
	{
		this._clearAll();
	}

	_onRoomUnpaused(aEvent_obj)
	{

	}

	_onRoomFieldCleared()
	{
		this._clearAll();
	}

	_onTimeToShowFire(aEvent_obj)
	{
		let data = aEvent_obj.data;
		let callback = aEvent_obj.callback;

		if (data.usedSpecialWeapon === WEAPONS.FLAMETHROWER || data.usedSpecialWeapon === WEAPONS.FLAMETHROWER_POWER_UP)
		{
			this._showFire(data, callback);
		}
	}

	_showFire(data, callback)
	{
		let lStartPos_pt = this._gameScreen.gameField.getGunPosition(data.seatId);

		let lTargetEnemyId_int = data.requestEnemyId;
		if (lTargetEnemyId_int < 0 || isNaN(lTargetEnemyId_int))
		{
			let lNonFakeEnemiesIds_int_arr = ShotResultsUtil.extractNonFakeEnemies(data);
			lTargetEnemyId_int = this._gameScreen.gameField.getFirstEverExistedEnemyIdFromTheList(lNonFakeEnemiesIds_int_arr);
		}
		let lEndPos_pt = this._gameScreen.gameField.getEnemyPosition(lTargetEnemyId_int);
		if (!lEndPos_pt)
		{
			callback(null, 0);
			return;
		}

		let lWeapon_w = this._gameScreen.gameField.getSeat(data.seatId, true).weaponSpotView.gun; //Weapon.js
		let weaponScale = lWeapon_w.i_getWeaponScale();

		this.view.i_showFire(data, lStartPos_pt, lEndPos_pt, callback, weaponScale);
	}

	_onSomeBeamBasicAnimationCompleted(aEvent_obj)
	{
		let lShotData_obj = aEvent_obj.shotData;
		if (lShotData_obj.rid !== -1)
		{
			let seat = this._gameScreen.gameField.getSeat(lShotData_obj.seatId, true);
			let lWeapon_w = seat.weaponSpotView.gun; //Weapon.js
			if (!lWeapon_w.gun.isShotState) 
			{
				this.emit(FlameThrowersController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED);
			}
			else
			{
				lWeapon_w.once(Weapon.EVENT_ON_GUN_SHOT_COMPLETED, this._onGunShotCompleted, this);
				lWeapon_w.once(Weapon.EVENT_ON_GUN_RESET, () => {
					lWeapon_w.off(Weapon.EVENT_ON_GUN_SHOT_COMPLETED, this._onGunShotCompleted, this, true);
				} );
			}
		}
	}

	_onGunShotCompleted(event)
	{
		this.emit(FlameThrowersController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED);
	}

	_clearAll()
	{
		this.info.i_clearAll();
		this.view.i_clearAll();	
	}
}

export default FlameThrowersController;