import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import CryogunsInfo from '../../../../model/uis/weapons/cryogun/CryogunsInfo';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameScreen from '../../../../main/GameScreen';
import GameField from '../../../../main/GameField';
import { WEAPONS } from '../../../../../../shared/src/CommonConstants';
import CryogunsView from '../../../../view/uis/weapons/cryogun/CryogunsView';
import { SCENE_WIDTH, SCENE_HEIGHT } from '../../../../config/Constants';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import GameSoundsController from '../../../sounds/GameSoundsController';
import EnemyFreezingManager from './EnemyFreezingManager';

class CryogunsController extends SimpleUIController {

	static get CENTER_TARGET_POINT() 									{ return {x: SCENE_WIDTH/2, y: SCENE_HEIGHT/2} };
	static get EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED()		{ return 'EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED'; }
	static get EVENT_FREEZE_ENEMY()										{ return 'EVENT_FREEZE_ENEMY'; }
	static get EVENT_UNFREEZE_ENEMY() 									{ return 'EVENT_UNFREEZE_ENEMY'; }
	static get WEAPON_ACTION_RADIUS()									{ return 200; } //https://jira.dgphoenix.com/browse/DRAG-697

	constructor()
	{
		super(new CryogunsInfo(), new CryogunsView());

		this._gameScreen = null;

		this._fEnemyFreezingManagers_efm_obj = null;
	}

	i_isEnemyFrozen(aEnemyId_int)
	{
		if (this.enemyFreezingManagersObj[aEnemyId_int])
		{
			return true;
		}
		return false;
	}

	__init()
	{
		super.__init();

		this._gameScreen = APP.currentWindow;

		this._gameScreen.once(GameScreen.EVENT_ON_READY, this._onGameScreenReady, this);

		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_UNPAUSED, this._onRoomUnpaused, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_FIELD_CLEARED, this._onRoomFieldCleared, this);
		this._gameScreen.on(GameScreen.EVENT_ON_TRAJECTORIES_UPDATED, this._onEnemiesTrajectoriesUpdated, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_INFO_UPDATED, this._onRoomInfoUpdated, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		this.view.on(CryogunsView.EVENT_ON_BEAM_ANIMATION_COMPLETED, this._onSomeBeamAnimationCompleted, this);
	}

	_onGameScreenReady(aEvent_obj)
	{
		this._gameScreen.gameField.on(GameField.EVENT_ON_GAME_FIELD_SCREEN_CREATED, this._onGameFieldScreenCreated, this);
		this._gameScreen.gameField.on(GameField.EVENT_SHOW_FIRE, this._onTimeToShowFire, this);
		this._gameScreen.gameField.on(GameField.EVENT_ON_DEATH_ANIMATION_STARTED, this._onEnemyDeathAnimationStarted, this);
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

	get enemyFreezingManagersObj()
	{
		return this._fEnemyFreezingManagers_efm_obj || (this._fEnemyFreezingManagers_efm_obj = new Object());
	}

	_onEnemiesTrajectoriesUpdated(aEvent_obj)
	{
		let lFreezeTimeMs_int = aEvent_obj.data.freezeTime;
		let lTrajectories_obj = aEvent_obj.data.trajectories;

		let lFreezingEnemiesIds_int_arr = Object.keys(lTrajectories_obj).map(function(aEnemyId_str) {
 			return Number(aEnemyId_str);
		});

		if (lFreezeTimeMs_int > 0)
		{
			let lIsAnimated_bl = true;
			for (let enemyId of lFreezingEnemiesIds_int_arr)
			{
				this._freezeEnemy(enemyId, lFreezeTimeMs_int);
			}
		}
		else
		{
			for (let enemyId of lFreezingEnemiesIds_int_arr)
			{
				this._unfreezeEnemyIfRequired(enemyId);
			}
		}
	}

	_freezeEnemy(aEnemyId_int, aFreezeTimeMs_int, aIsAnimated_bl = true)
	{
		let enemyId = aEnemyId_int;
		let lFreezeTimeMs_int = aFreezeTimeMs_int;
		let lIsAnimated_bl = aIsAnimated_bl;
		let lEnemyFreezingManager_efm = this.enemyFreezingManagersObj[enemyId];
		if (!lEnemyFreezingManager_efm)
		{
			lEnemyFreezingManager_efm = new EnemyFreezingManager(enemyId);
			lEnemyFreezingManager_efm.on(EnemyFreezingManager.EVENT_ON_FREEZING_TIMER_COMPLETED, this._onEnemyFreezingTimerCompleted, this);
			this.enemyFreezingManagersObj[enemyId] = lEnemyFreezingManager_efm;
		}
		lEnemyFreezingManager_efm.i_activateFreezing(lFreezeTimeMs_int, lIsAnimated_bl);
		this.emit(CryogunsController.EVENT_FREEZE_ENEMY, {enemyId: enemyId, isAnimated: lIsAnimated_bl});
	}

	_unfreezeEnemyIfRequired(aEnemyId_int)
	{
		let enemyId = aEnemyId_int;
		let lEnemyFreezingManager_efm = this.enemyFreezingManagersObj[enemyId];
		if (lEnemyFreezingManager_efm)
		{
			this._unfreezeEnemy(enemyId);
		}
	}

	_unfreezeEnemy(aEnemyId_int)
	{
		let enemyId = aEnemyId_int;
		this.emit(CryogunsController.EVENT_UNFREEZE_ENEMY, {enemyId: enemyId});
		if (APP.soundsController.isSoundPlaying('cryo_gun_unfreeze'))
		{
			APP.soundsController.stop('cryo_gun_unfreeze');
		}
		APP.soundsController.play('cryo_gun_unfreeze', false);
		this._destroyEnemyFreezingManager(enemyId);
	}

	_onEnemyFreezingTimerCompleted(aEvent_obj)
	{
		let enemyId = aEvent_obj.enemyId;
		this._unfreezeEnemy(enemyId);
	}

	_destroyEnemyFreezingManager(aEnemyId_int)
	{
		let enemyId = aEnemyId_int;
		let lEnemyFreezingManager_efm = this.enemyFreezingManagersObj[enemyId];
		if (lEnemyFreezingManager_efm)
		{
			lEnemyFreezingManager_efm.destroy();
			delete this.enemyFreezingManagersObj[enemyId];
		}
	}

	_onRoomInfoUpdated(aRoomInfo_obj)
	{
		let lFreezeTime_obj = aRoomInfo_obj.freezeTime;
		if (lFreezeTime_obj)
		{
			this._activateFreezing(lFreezeTime_obj, false);
		}
	}

	_activateFreezing(aFreezeTime_obj, aIsAnimated_bl = true)
	{
		let lFreezeTime_obj = aFreezeTime_obj;
		for (let enemyId in lFreezeTime_obj)
		{
			let freezeTime = lFreezeTime_obj[enemyId];
			this._freezeEnemy(Number(enemyId), freezeTime, aIsAnimated_bl);
		}
	}

	_deactivateFreezing()
	{
	}

	_onTimeToShowFire(aEvent_obj)
	{
		let data = aEvent_obj.data;
		let callback = aEvent_obj.callback;

		if (data.usedSpecialWeapon === WEAPONS.CRYOGUN || data.usedSpecialWeapon === WEAPONS.CRYOGUN_POWER_UP)
		{
			this._showFire(data, callback);
		}
	}

	_onEnemyDeathAnimationStarted(aEvent_obj)
	{
		let enemyId = aEvent_obj.enemyId;
		let lEnemyFreezingManager_efm = this.enemyFreezingManagersObj[enemyId];
		if (lEnemyFreezingManager_efm)
		{
			this._destroyEnemyFreezingManager(enemyId); // if any
		}
	}

	_showFire(data, callback)
	{
		let lStartPos_pt = this._gameScreen.gameField.getGunPosition(data.seatId);
		let lEndPos_pt = new PIXI.Point(data.x, data.y);
		let lWeapon_w = this._gameScreen.gameField.getSeat(data.seatId, true).weaponSpotView.gun;
		let weaponScale = lWeapon_w.i_getWeaponScale();

		this.view.i_showFire(data, lStartPos_pt, lEndPos_pt, weaponScale, callback);
	}

	_onSomeBeamAnimationCompleted(aEvent_obj)
	{
		let lShotData_obj = aEvent_obj.shotData;
		if (lShotData_obj.rid !== -1)
		{
			this.emit(CryogunsController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED);
		}
	}

	_clearAll()
	{
		this.info.i_clearAll();
		this.view.i_clearAll();

		if (this._fEnemyFreezingManagers_efm_obj)
		{
			for (const enemyId in this._fEnemyFreezingManagers_efm_obj)
			{
				let lEnemyFreezingManager_efm = this._fEnemyFreezingManagers_efm_obj[enemyId];
				lEnemyFreezingManager_efm.destroy();
			}
			this._fEnemyFreezingManagers_efm_obj = null;
		}	
	}
}

export default CryogunsController;