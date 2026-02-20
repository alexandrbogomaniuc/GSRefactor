import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import HvEnemyInfo from '../../../model/uis/hv_enemies/HvEnemyInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Enemy from '../../../main/enemies/Enemy';
import GameScreen from '../../../main/GameScreen';
import GameField from '../../../main/GameField';

class HvEnemyController extends SimpleController {

	static get EVENT_TIME_TO_CREATE_HV_ENEMY() 	{return 'EVENT_TIME_TO_CREATE_HV_ENEMY';}
	static get EVENT_SET_HV_ENEMY_TIME_OFFSET() {return 'EVENT_SET_HV_ENEMY_TIME_OFFSET';}

	constructor (aEnemyData_obj) {
		super(new HvEnemyInfo(aEnemyData_obj));
	}

	__init()
	{
		super.__init();

		this._gameScreen = APP.currentWindow;
		this._gameScreen.on(GameScreen.EVENT_ON_HV_ENEMY_PARENT_HIT, this._onHvEnemyParentHit, this);
		this._gameScreen.on(GameScreen.EVENT_ON_TRAJECTORIES_UPDATED, this._onTrajectoriesUpdated, this);

		this._gameScreen.gameField.on(GameField.EVENT_ON_NEW_ENEMY_CREATED, this._onNewEnemyCreated, this);

		this._fParentEnemy_enm = null;
		this._fHvEnemy_enm = null;

		this._fIsTrajectoryUpdated_bl = false;
		let lInfo_hei = this.i_getInfo();
		this._fParentEnemy_enm = this._gameScreen.gameField.getExistEnemy(lInfo_hei.parentEnemyId) || this._gameScreen.gameField.getDeadEnemy(lInfo_hei.parentEnemyId);
		if (!this._fParentEnemy_enm)
		{
			this._startRisingHvEnemyImmediately();
		}
		else
		{
			this._fParentEnemy_enm.on(Enemy.EVENT_ON_DEATH_ANIMATION_EXPLOSION_STARTED, this._onParentEnemyDeathAnimationBonesFellDown, this);
		}
	}

	destroy()
	{
		if (this._gameScreen)
		{
			this._gameScreen.off(GameScreen.EVENT_ON_TRAJECTORIES_UPDATED, this._onTrajectoriesUpdated, this);

			this._gameScreen.gameField && this._gameScreen.gameField.off(GameField.EVENT_ON_NEW_ENEMY_CREATED, this._onNewEnemyCreated, this);
			this._gameScreen.gameField && this._gameScreen.gameField.off('showEnemyHit', this._onParentEnemyHitAnimationStarted, this);
			this._gameScreen = null;
		}

		if (this._fParentEnemy_enm)
		{
			this._fParentEnemy_enm.off(Enemy.EVENT_ON_ENEMY_VIEW_REMOVING, this._onParentEnemyViewRemoving, this);
			this._fParentEnemy_enm.off(Enemy.EVENT_ON_ENEMY_START_DYING, this._onParentEnemyStartDying, this);
			this._fParentEnemy_enm.off(Enemy.EVENT_ON_DEATH_ANIMATION_BONES_FELL_DOWN, this._onParentEnemyDeathAnimationBonesFellDown, this);
			this._fParentEnemy_enm.off(Enemy.EVENT_ON_DEATH_ANIMATION_EXPLOSION_STARTED, this._onParentEnemyDeathAnimationBonesFellDown, this);
			this._fParentEnemy_enm.off(Enemy.EVENT_ON_DEATH_ANIMATION_STARTED, this._onParentEnemyDeathAnimationBonesFellDown, this);
		}

		if (this._fHvEnemy_enm)
		{
			this._fHvEnemy_enm.off(Enemy.EVENT_ON_HV_ENEMY_READY_TO_GO, this._onHvEnemyReadyToGo, this);
		}

		this._fParentEnemy_enm = null;
		this._fHvEnemy_enm = null;

		super.destroy();
	}

	_onHvEnemyParentHit(aEvent_ue)
	{
		let lInfo_hei = this.i_getInfo();

		// currently there is only frog and there is no hvEnemyId
		// if (aEvent_ue.hvEnemyId == lInfo_hei.enemyId)
		{
			this._gameScreen.off(GameScreen.EVENT_ON_HV_ENEMY_PARENT_HIT, this._onHvEnemyParentHit, this);

			lInfo_hei.parentEnemyId = aEvent_ue.parentEnemyId;
			// start listening for Death animation
			let lParentEnemy_enm = this._gameScreen.gameField.getExistEnemy(lInfo_hei.parentEnemyId);
			//if the parent enemy already off the screen - use its last position
			if (!lParentEnemy_enm || lParentEnemy_enm.isDestroyed)
			{
				//throw new Error(`No parent enemy with id = ${lInfo_hei.parentEnemyId} found for High Volatility enemy with id = ${lInfo_hei.enemyId}`);
				lInfo_hei.parentEnemyLastPosition = this._gameScreen.gameField.getEnemyLastPosition(lInfo_hei.parentEnemyId);
				lInfo_hei.parentEnemyLastAngle = 0;
				this._startRisingHvEnemyImmediately();
				return;
			}

			lParentEnemy_enm.once(Enemy.EVENT_ON_ENEMY_START_DYING, this._onParentEnemyStartDying, this);
			lParentEnemy_enm.once(Enemy.EVENT_ON_ENEMY_VIEW_REMOVING, this._onParentEnemyViewRemoving, this);
			
			lInfo_hei.timeCreated = APP.currentWindow.currentTime;
			if (lParentEnemy_enm.isCritter)
			{
				lParentEnemy_enm.once(Enemy.EVENT_ON_DEATH_ANIMATION_STARTED, this._onParentEnemyDeathAnimationBonesFellDown, this);
			}
			else
			{
				lParentEnemy_enm.once(Enemy.EVENT_ON_DEATH_ANIMATION_BONES_FELL_DOWN, this._onParentEnemyDeathAnimationBonesFellDown, this);
			}
			this._fParentEnemy_enm = lParentEnemy_enm;
		}
	}

	_onNewEnemyCreated(aEvent_obj)
	{
		let lInfo_hei = this.i_getInfo();
		if (aEvent_obj.enemyId == lInfo_hei.enemyId)
		{
			this._gameScreen.gameField.off(GameField.EVENT_NEW_ENEMY_CREATED, this._onNewEnemyCreated, this);
			let lHvEnemy_enm = this._gameScreen.gameField.getExistEnemy(lInfo_hei.enemyId);
			if (!lHvEnemy_enm)
			{
				throw new Error(`No High Volatility enemy with id = ${lInfo_hei.enemyId} found on the screen`);
			}
			this._fHvEnemy_enm = lHvEnemy_enm;
			if (this._fHvEnemy_enm.isBabyFrog)
			{
				this._onHvEnemyReadyToGo();
			}
			else
			{
				lHvEnemy_enm.once(Enemy.EVENT_ON_HV_ENEMY_READY_TO_GO, this._onHvEnemyReadyToGo, this);
			}
		}
	}

	_startRisingHvEnemyImmediately()
	{
		let lInfo_hei = this.i_getInfo();
		this.emit(HvEnemyController.EVENT_TIME_TO_CREATE_HV_ENEMY, {
			enemyId: lInfo_hei.enemyId, 
			enemyData: lInfo_hei.enemyData, 
			timeCreated: lInfo_hei.timeCreated,
			position: lInfo_hei.parentEnemyLastPosition,
			angle: lInfo_hei.parentEnemyLastAngle
		});
	}

	_onParentEnemyViewRemoving(aEvent_obj)
	{	
		// in case the parent enemy went off screen before dying animation got started
		let lInfo_hei = this.i_getInfo();
		// lInfo_hei.timeCreated = APP.currentWindow.currentTime;

		lInfo_hei.parentEnemyLastPosition = aEvent_obj.position;
		lInfo_hei.parentEnemyLastAngle = aEvent_obj.angle;

		this._gameScreen.gameField.once('showEnemyHit', this._onParentEnemyHitAnimationStarted, this);
	}

	_onParentEnemyHitAnimationStarted(aEvent_obj)
	{
		let lInfo_hei = this.i_getInfo();

		if (aEvent_obj.id == lInfo_hei.parentEnemyId)
		{
			//time to create HvEnemy
			this.emit(HvEnemyController.EVENT_TIME_TO_CREATE_HV_ENEMY, {
				enemyId: lInfo_hei.enemyId, 
				enemyData: lInfo_hei.enemyData, 
				timeCreated: lInfo_hei.timeCreated,
				position: lInfo_hei.parentEnemyLastPosition,
				angle: lInfo_hei.parentEnemyLastAngle
			});
		}
	}

	_onParentEnemyStartDying()
	{
		this._fParentEnemy_enm.off(Enemy.EVENT_ON_ENEMY_VIEW_REMOVING, this._onParentEnemyViewRemoving, this);
	}

	_onParentEnemyDeathAnimationBonesFellDown(aEvent_obj)
	{
		// time to create hv enemy
		let lInfo_hei = this.i_getInfo();

		//time to create HvEnemy
		this.emit(HvEnemyController.EVENT_TIME_TO_CREATE_HV_ENEMY, {
			enemyId: lInfo_hei.enemyId, 
			enemyData: lInfo_hei.enemyData, 
			timeCreated: lInfo_hei.timeCreated,
			position: aEvent_obj.position,
			angle: aEvent_obj.angle
		});
	}

	_onHvEnemyReadyToGo()
	{
		this._gameScreen && this._gameScreen.off(GameScreen.EVENT_ON_TRAJECTORIES_UPDATED, this._onTrajectoriesUpdated, this);
		
		let lInfo_hei = this.i_getInfo();
		lInfo_hei.timeAdded = APP.currentWindow.currentTime;

		let lTimeOffset_num = this._fHvEnemy_enm.isBabyFrog ? 0 :lInfo_hei.timeAdded - lInfo_hei.timeCreated;

		if (this._fIsTrajectoryUpdated_bl)
		{
			//if trajrectory has been updated while HV enemy was rising
			this.info.enemyData.trajectory.points[0].time = this._gameScreen.currentTime;
		}

		this.emit(HvEnemyController.EVENT_SET_HV_ENEMY_TIME_OFFSET, {enemyId: lInfo_hei.enemyId, timeOffset: lTimeOffset_num});
	}

	_onTrajectoriesUpdated(aEvent_obj)
	{
		this._gameScreen && this._gameScreen.off(GameScreen.EVENT_ON_TRAJECTORIES_UPDATED, this._onTrajectoriesUpdated, this);

		let lInfo_hei = this.i_getInfo();
		let trajectory = aEvent_obj.data.trajectories[lInfo_hei.enemyId];
		if (!trajectory)
		{
			//console.log(`[Y] trajectory for ${lInfo_hei.enemyId} is not found`);
			return;
		}
		let enemyData = lInfo_hei.enemyData;
		enemyData.trajectory = trajectory;
		enemyData.speed = trajectory.speed;
		lInfo_hei.enemyData = enemyData;

		this._fIsTrajectoryUpdated_bl = true;
	}

}

export default HvEnemyController;