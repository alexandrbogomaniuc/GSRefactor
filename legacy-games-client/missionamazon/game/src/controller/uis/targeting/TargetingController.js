import TargetingInfo from '../../../model/uis/targeting/TargetingInfo';
import GameScreen from '../../../main/GameScreen';
import { ROUND_STATE } from '../../../model/state/GameStateInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import TargetingView from '../../../view/uis/targeting/TargetingView';
import Game from '../../../Game';
import { WEAPONS } from '../../../../../shared/src/CommonConstants';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import FireSettingsController from '../fire_settings/FireSettingsController';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import AutoTargetingSwitcherController from './AutoTargetingSwitcherController';

const TARGET_FIRE_START_DELAY = 500;

class TargetingController extends SimpleUIController {

	static get EVENT_ON_TARGET_UPDATED() 		{ return 'EVENT_ON_TARGET_UPDATED';}
	static get EVENT_ON_TARGET_RESET() 			{ return 'EVENT_ON_TARGET_RESET';}
	static get EVENT_ON_PAUSE_STATE_UPDATED() 	{ return 'EVENT_ON_PAUSE_STATE_UPDATED';}

	constructor() {
		super(new TargetingInfo(), new TargetingView());

		this._gameScreen = null;

		this._fIsLobbySecondaryScreenActive_bl = false;
		this._fIsDialogActivated_bl = false;
		this._fIsFireSettingsActivated_bl = false;
		this._fIsChooseWeaponScreenActivated_bln = false;

		this._fWeaponsController_wsc = null;
		this._fWeaponsInfo_wsi = null;

		this._fFireSettingsController_fssc = null;
		this._fCurTargetPreference_int = undefined;
		this._fIsUserSelectedEnemy_bl = false;
		this._fUserSelectionTime_int = undefined;
	}

	__init()
	{
		super.__init();

		this.view.on(TargetingView.EVENT_CROSSHAIRS_HIDDEN, this._tryToSelectNextTarget, this);

		this._gameScreen = APP.currentWindow;

		this._fGameStateInfo_gsi = this._gameScreen.gameStateController.info;

		this._fWeaponsController_wsc = APP.currentWindow.weaponsController;
		this._fWeaponsInfo_wsi = this._fWeaponsController_wsc.i_getInfo();

		this._gameScreen.on(GameScreen.EVENT_ON_TARGETING, this._onTargetingSuspicion, this);
		this._gameScreen.on(GameScreen.EVENT_ON_RESET_TARGET, this._onResetTarget, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_UNPAUSED, this._onRoomUnpaused, this);
		this._gameScreen.on(GameScreen.EVENT_ON_DIALOG_ACTIVATED, this._onDialogActivated, this);
		this._gameScreen.on(GameScreen.EVENT_ON_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
		this._gameScreen.on(GameScreen.EVENT_ON_BACK_TO_LOBBY_BUTTON_CLICKED, this._onBackToLobbyButtonClicked, this);
		this._gameScreen.on(GameScreen.EVENT_ON_KILLED_MISS_ENEMY, this._onEnemyKilledMiss, this);
		this._gameScreen.on(GameScreen.EVENT_ON_INVULNERABLE_ENEMY, this._onEnemyInvulnerableMiss, this);
		this._gameScreen.on(GameScreen.EVENT_ON_TARGET_ENEMY_IS_DEAD, this._onTargetEnemyIsDead, this);
		this._gameScreen.on(GameScreen.EVENT_ON_CHOOSE_WEAPON_SCREEN_CHANGED, this._onChooseWeaponScreenChanged, this);

		this._fFireSettingsController_fssc = this._gameScreen.fireSettingsController;
		this._fFireSettingsController_fssc.on(FireSettingsController.EVENT_SCREEN_ACTIVATED, this._onFireSettingsScreenActivated, this);
		this._fFireSettingsController_fssc.on(FireSettingsController.EVENT_SCREEN_DEACTIVATED, this._onFireSettingsScreenDeactivated, this);

		APP.on(Game.EVENT_ON_GAME_SECONDARY_SCREEN_ACTIVATED, this._onSecondaryScreenActivated, this);
		APP.on(Game.EVENT_ON_GAME_SECONDARY_SCREEN_DEACTIVATED, this._onSecondaryScreenDeactivated, this);

		let lAutoTargetingSwitcher_atsc = APP.currentWindow.autoTargetingSwitcherController;
		lAutoTargetingSwitcher_atsc.on(AutoTargetingSwitcherController.EVENT_ON_AUTO_TARGETING_SWITCHED, this._onAutoTargetingSwitched, this);
	}

	tick()
	{
		if (!this.view)
		{
			return;
		}

		this.view.tick();

		let lGameField_gf = this._gameScreen.gameField;
		let lTargetCrosshairsPos_pt = this.view.lastCrosshairsPos;
		let lNearestTowardTargetEnemy_e = lTargetCrosshairsPos_pt ? lGameField_gf.getNearestEnemy(lTargetCrosshairsPos_pt.x, lTargetCrosshairsPos_pt.y, 1) : null;

		if (APP.isBattlegroundGame)
		{
			if (
				this.info.isTargetEnemyDefined && 
				!this.isUserSelectedEnemy &&
				lNearestTowardTargetEnemy_e &&
				lNearestTowardTargetEnemy_e.id !== this.info.targetEnemyId
			)
			{
				this._resetTarget();
				this._tryToSelectNextTarget();
			}
			else if (this.info.isPaused && this._fUserSelectionTime_int !== undefined)
			{
				if (Date.now() - this._fUserSelectionTime_int >= TARGET_FIRE_START_DELAY)
				{
					this._fUserSelectionTime_int = undefined;
					this._validatePause();
				}
			}
		}
	}

	get isUserSelectedEnemy()
	{
		return this._fIsUserSelectedEnemy_bl;
	}

	_onTargetViewDestroyed()
	{
		this._onSelectNextTargetSuspicion();
	}

	_onSelectNextTargetSuspicion()
	{
		if (this._gameScreen.gameField.isGunLocked)
		{
			this._gameScreen.once(GameScreen.EVENT_ON_GUN_UNLOCKED, this._onCurrentGunUnlocked, this);
			return;
		}
		this._resetTarget();
		this._tryToSelectNextTarget();
	}

	_onCurrentGunUnlocked()
	{
		this._onSelectNextTargetSuspicion();
	}

	_onTargetingSuspicion(event)
	{
		this._fCurTargetPreference_int = undefined;
		this._fIsUserSelectedEnemy_bl = true;
		this._updateTargeting(event.targetEnemyId);

		if (APP.isBattlegroundGame)
		{
			this._fUserSelectionTime_int = Date.now();
			this._validatePause();
		}
	}

	_onResetTarget()
	{
		this._resetTarget();
		this._fCurTargetPreference_int = undefined;
	}

	_onEnemyKilledMiss(event)
	{
		if (this.info.isActive && this.info.targetEnemyId === event.enemyId)
		{
			this._resetTarget();
			this._tryToSelectNextTarget();
		}
	}

	_onEnemyInvulnerableMiss(event)
	{
		if (this.info.isActive && this.info.targetEnemyId === event.enemyId)
		{
			this._resetTarget();
			this._tryToSelectNextTarget();

			console.log("DEBUG: enemy id: " + event.enemyId + " is set invulnerable by server. Target dropped");
		}
	}

	_onTargetEnemyIsDead(event)
	{
		if (this.info.isActive && this.info.targetEnemyId === event.enemyId)
		{
			this._resetTarget();
			this._tryToSelectNextTarget();
		}
	}

	_onEnemyDestroyed(event)
	{
		if (this.info.isActive && (this._fWeaponsInfo_wsi.currentWeaponId === WEAPONS.ARTILLERYSTRIKE || this._fWeaponsInfo_wsi.currentWeaponId === WEAPONS.ARTILLERYSTRIKE_POWER_UP) && this.info.targetEnemyId === event.enemyId)
		{
			this._tryToSelectNextTarget();
		}
	}

	_onAutoTargetingSwitched(event)
	{
		if (event.on) // Clear current target
		{
			this.info.i_resetTarget();
			this._gameScreen.off(GameScreen.EVENT_ON_GUN_UNLOCKED, this._onCurrentGunUnlocked, this);
			this.view.updateTarget(null);
			
			this.emit(TargetingController.EVENT_ON_TARGET_RESET);
		}
		else
		{
			this.info.i_resetTarget();
			this.view.updateTarget(null);
			this.emit(TargetingController.EVENT_ON_TARGET_RESET);
		}
	}

	_updateTargeting(aTargetEnemyId_int)
	{
		this._resetNextTargetTimer();

		let lTargetEnemyId_int = aTargetEnemyId_int;

		if (this._fGameStateInfo_gsi.gameState != ROUND_STATE.PLAY
			|| APP.playerController.info.isObserver
			|| this._gameScreen.gameField.roundResultActive)
		{
			lTargetEnemyId_int = null;
		}

		this._addViewIfRequired();

		this.info.targetEnemyId = lTargetEnemyId_int;

		let lTargetEnemy_enm;
		if (lTargetEnemyId_int !== null && !isNaN(lTargetEnemyId_int))
		{
			lTargetEnemy_enm = this._gameScreen.gameField.getExistEnemy(lTargetEnemyId_int);
		}
		this.view.updateTarget(lTargetEnemy_enm);
		if (!lTargetEnemy_enm || !lTargetEnemy_enm.parent)
		{
			this.info.i_resetTarget();
			this._gameScreen.off(GameScreen.EVENT_ON_GUN_UNLOCKED, this._onCurrentGunUnlocked, this);
		}

		if (lTargetEnemy_enm)
		{
			// if (this._gameScreen.gameStateController.info.isBossSubround && !lTargetEnemy_enm.isBoss)
			// {
			// 	return; // Do not choose non enemy target during boss round
			// }

			this._fCurTargetPreference_int = APP.playerController.info.getEnemyPayouts(lTargetEnemy_enm.typeId).minPayout;
		}

		this.emit(TargetingController.EVENT_ON_TARGET_UPDATED, {targetEnemyId: this.info.targetEnemyId});
	}

	_addViewIfRequired()
	{
		if (this.view.parent)
		{
			return;
		}
		this.view.addToContainerIfRequired(this._gameScreen.gameField.targetingContainerInfo);
		this.view.on(TargetingView.EVENT_ON_TARGET_VIEW_DESTROYED, this._onTargetViewDestroyed, this);
	}

	_onRoomUnpaused()
	{
		this._updateTargeting(this.info.targetEnemyId);
	}

	_onSecondaryScreenActivated()
	{
		this._fIsLobbySecondaryScreenActive_bl = true;
		this._validatePause();
	}

	_onSecondaryScreenDeactivated()
	{
		this._fIsLobbySecondaryScreenActive_bl = false;
		this._validatePause();
	}

	_onFireSettingsScreenActivated()
	{
		this._fIsFireSettingsActivated_bl = true;
		this._validatePause();
	}

	_onFireSettingsScreenDeactivated()
	{
		this._fIsFireSettingsActivated_bl = false;
		this._validatePause();
	}

	_onDialogActivated()
	{
		this._fIsDialogActivated_bl = true;
		this._validatePause();
	}

	_onDialogDeactivated()
	{
		this._fIsDialogActivated_bl = false;
		this._validatePause();
	}

	_onChooseWeaponScreenChanged(aEvent_obj)
	{
		let lIsActive_bln = aEvent_obj.isActive;
		if (lIsActive_bln == this._fIsChooseWeaponScreenActivated_bln) return;

		this._fIsChooseWeaponScreenActivated_bln = lIsActive_bln;
		this._validatePause();
	}

	_validatePause()
	{
		let lIsPaused_bl =	this._fIsLobbySecondaryScreenActive_bl ||
							this._fIsDialogActivated_bl ||
							this._fIsFireSettingsActivated_bl ||
							this._fIsChooseWeaponScreenActivated_bln ||
							this._fUserSelectionTime_int !== undefined;

		if (this.info.isPaused !== lIsPaused_bl)
		{
			this.info.isPaused = lIsPaused_bl;
			this.emit(TargetingController.EVENT_ON_PAUSE_STATE_UPDATED);
		}
	}

	_onBackToLobbyButtonClicked()
	{
		this._clearAll();
	}

	_tryToSelectNextTarget()
	{
		if(APP.isAutoFireMode)
		{
			let lNextEnemyId_int = this._getNextMostValuableEnemyId();
			if (lNextEnemyId_int == null || lNextEnemyId_int < 0)
			{
				this._startNextTargetTimer();
				return;
			}
			this._updateTargeting(lNextEnemyId_int);
		}else{
			this._updateTargeting(null);
			return;
		}
	}

	_resetNextTargetTimer()
	{
		this.info.lookingForATarget = false;
		this._fNextTargetTimer_tmr && this._fNextTargetTimer_tmr.destructor();
		this._fNextTargetTimer_tmr = null;
	}

	_startNextTargetTimer()
	{
		this._resetNextTargetTimer();
		this.info.lookingForATarget = true;
		this._fNextTargetTimer_tmr = new Timer(this._tryToSelectNextTarget.bind(this), 100);
	}

	_getNextMostValuableEnemyId()
	{
		let lPlayerInfo_pi = APP.playerController.info;
		let lCurTargetPriorityType_int = lPlayerInfo_pi.targetPriority;
		let lCurTargetPreference_int = this._fCurTargetPreference_int;
		let lEnemies_obj_arr = this._gameScreen.getEnemies().slice();
		let lGameScreen_gs = this._gameScreen;
		let lGameField_gf = lGameScreen_gs.gameField;

		!APP.isBattlegroundGame && lEnemies_obj_arr.sort(
			function (a, b)
			{
				let lEnemyAPreference_int = lPlayerInfo_pi.getEnemyPayouts(a.typeId).maxPayout;
				let lEnemyBPreference_int = lPlayerInfo_pi.getEnemyPayouts(b.typeId).maxPayout;
				if (lCurTargetPriorityType_int == 2
					&& lCurTargetPreference_int !== undefined) // SAME
				{
					let lAPrefDelta = Math.abs(lEnemyAPreference_int - lCurTargetPreference_int);
					let lBPrefDelta = Math.abs(lEnemyBPreference_int - lCurTargetPreference_int);

					return (lAPrefDelta <= lBPrefDelta) ? -1 : 1;
				}
				else if (lCurTargetPriorityType_int == 1) // LOW
				{
					return lEnemyAPreference_int - lEnemyBPreference_int;
				}
				else //HIGH
				{
					return lEnemyBPreference_int - lEnemyAPreference_int;
				}
			}
		);

		lEnemies_obj_arr = lEnemies_obj_arr.filter(function(value) {
			let lEnemyId_int = value.id;
			let lEnemyView_enm = lGameField_gf.getExistEnemy(lEnemyId_int);
			
			return (value.life !== 0
				&& !value.killedMiss
				&& lEnemyView_enm
				&& lEnemyView_enm.parent
				&& !lEnemyView_enm.isFireDenied
				&& !lEnemyView_enm.invulnerable
				&& lEnemyView_enm.isTargetable());
		});

		if (lEnemies_obj_arr.length === 0)
		{
			return null;
		}

		if (APP.isBattlegroundGame)
		{
			let lLastPos_p = this.view.lastCrosshairsPos;

			let lNearestEnemy_obj = null;
			let lEnemiesIds_int_arr = [];
			for (let i=0; i<lEnemies_obj_arr.length; i++)
			{
				lEnemiesIds_int_arr.push(lEnemies_obj_arr[i].id);
			}

			if (!!lLastPos_p)
			{
				lNearestEnemy_obj = lGameField_gf.getNearestEnemy(lLastPos_p.x, lLastPos_p.y, 1, lEnemiesIds_int_arr);
			}
			else
			{
				let lGunPos_p = lGameField_gf.getGunPosition(lGameField_gf.seatId);
				if (!lGunPos_p)
				{
					return null;
				}

				lNearestEnemy_obj = lGameField_gf.getNearestEnemy(lGunPos_p.x, lGunPos_p.y, 2, lEnemiesIds_int_arr);
			}

			return lNearestEnemy_obj ? lNearestEnemy_obj.id : null;
		}

		lEnemies_obj_arr = lEnemies_obj_arr.filter(function(value, index, self) {
			return (value.typeId === self[0].typeId);
		});

		let n = lEnemies_obj_arr.length;
		let r = Utils.random(0, n-1);

		return lEnemies_obj_arr[r].id;
	}

	_resetTarget()
	{
		let lValidatePauseRequired_bl = this._fUserSelectionTime_int !== undefined;

		this._fIsBossTargetingRequired_bl = false;
		this._fIsUserSelectedEnemy_bl = false;
		this._fUserSelectionTime_int = undefined;
		this._updateTargeting(null);

		if (lValidatePauseRequired_bl)
		{
			this._validatePause();
		}
	}

	destroy()
	{
		this._resetNextTargetTimer();

		if (this._gameScreen)
		{
			this._gameScreen.off(GameScreen.EVENT_ON_TARGETING, this._onTargetingSuspicion, this);
			this._gameScreen.off(GameScreen.EVENT_ON_RESET_TARGET, this._onResetTarget, this);
			this._gameScreen.off(GameScreen.EVENT_ON_ROOM_UNPAUSED, this._onRoomUnpaused, this);
			this._gameScreen.off(GameScreen.EVENT_ON_DIALOG_ACTIVATED, this._onDialogActivated, this);
			this._gameScreen.off(GameScreen.EVENT_ON_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
			this._gameScreen.off(GameScreen.EVENT_ON_BACK_TO_LOBBY_BUTTON_CLICKED, this._onBackToLobbyButtonClicked, this);
			this._gameScreen.off(GameScreen.EVENT_ON_KILLED_MISS_ENEMY, this._onEnemyKilledMiss, this);
			this._gameScreen.off(GameScreen.EVENT_ON_GUN_UNLOCKED, this._onCurrentGunUnlocked, this);
			this._gameScreen = null;
		}

		APP.off(Game.EVENT_ON_GAME_SECONDARY_SCREEN_ACTIVATED, this._onSecondaryScreenActivated, this);
		APP.off(Game.EVENT_ON_GAME_SECONDARY_SCREEN_DEACTIVATED, this._onSecondaryScreenDeactivated, this);

		this._fGameStateInfo_gsi = null;
		this._fIsLobbySecondaryScreenActive_bl = undefined;
		this._fIsDialogActivated_bl = undefined;
		this._fIsFireSettingsActivated_bl = undefined;
		this._fIsChooseWeaponScreenActivated_bln = undefined;
		this._fIsUserSelectedEnemy_bl = undefined;
		this._fUserSelectionTime_int = undefined;

		super.destroy();
	}

	_clearAll()
	{
		this._resetNextTargetTimer();
		this._resetTarget();

		this._fCurTargetPreference_int = undefined;

		this._fIsLobbySecondaryScreenActive_bl = false;
		this._fIsDialogActivated_bl = false;
		this._fIsFireSettingsActivated_bl = false;
		this._fIsChooseWeaponScreenActivated_bln = false;
		this._fIsUserSelectedEnemy_bl = false;

		this.info.isPaused = false;
	}
}

export default TargetingController