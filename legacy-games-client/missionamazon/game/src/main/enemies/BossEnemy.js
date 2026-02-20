import SpineEnemy from './SpineEnemy';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { STATE_DEATH, STATE_TURN, DIRECTION, STATE_WALK, STATE_STAY } from './Enemy';
import { ENEMIES } from '../../../../shared/src/CommonConstants';
import Enemy from './Enemy';
import { Sprite } from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';

const HEALTH_STATE_NORMAL 	= 0;
const HEALTH_STATE_WEAK 	= 1;

const WEAK_HEALTH_THRESHOLD = 0.2;

export const STATE_CALL 	= 'call';

const WEAK_ANIMATION_IDEAL_DURATION = 2500;
const TURN_DURATION = 317;

class BossEnemy extends SpineEnemy
{

	constructor(params)
	{
		super(params);

		this._fWeakTransitionInProgress_bl = false;
		this._fIsWeakTurningNeeded_bl = false; //faster turning for call-animation

		if (this._fHealthState_int === undefined)
		{
			this._fHealthState_int = HEALTH_STATE_NORMAL;
		}

		this._fPauseTimeMarker_num = undefined;
		this._fFreezingMarker_num = undefined;

		//DEBUG...
		// this._fKeyDownHandler_func = this._keyDownHandler.bind(this);
		// window.addEventListener("keydown", this._fKeyDownHandler_func, false);
		//...DEBUG
	}

	_updateSpeed()
	{
		this.spineSpeed = this.getSpineSpeed();
		this.spineView.view.state.timeScale = this.spineSpeed;

		if (this.spineView.playing)
		{
			this.spineView.updatePosition();
		}
	}

	//override
	getSpineSpeed()
	{
		if (this.state === STATE_CALL)
		{
			let animationLast = this._getCurrentCallAnimationLength();
			let lTurnDurationBeforeCall_ms_num = this._fIsWeakTurningNeeded_bl ? TURN_DURATION : 0;
			return animationLast/(WEAK_ANIMATION_IDEAL_DURATION - lTurnDurationBeforeCall_ms_num*2);
		}

		if (this.state === STATE_TURN && this._fWeakTransitionInProgress_bl)
		{
			return 1;
		}
		return super.getSpineSpeed();
	}

	_getCurrentCallAnimationLength()
	{
		throw new Error(`No Call state found for boss with the name ${this.name}`);
	}

	_invalidateStates()
	{
		this._invalidateHealthState(true);

		super._invalidateStates();
	}

	//DEBUG...
	// _keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode  == 32)
	// 	{
	// 		this.energy = 0.14;
	// 		this._invalidateHealthState();
	// 	}
	// }
	//...DEBUG

	set energy(aValue_num)
	{
		if (this.energy != aValue_num && !isNaN(aValue_num))
		{
			super.energy = aValue_num;
			if (!this.isHealthStateWeak)
			{
				this._invalidateHealthState();
			}
		}
	}

	get energy()
	{
		return super.energy;
	}

	get isHealthStateWeak()
	{
		return this.healthState == HEALTH_STATE_WEAK;
	}

	set healthState(aHealthState_int)
	{
		this._fHealthState_int = aHealthState_int;
	}

	get healthState()
	{
		return this._fHealthState_int;
	}

	_invalidateHealthState(aImmediately_bl = false)
	{
		let totalBossHP = this.fullEnergy;
		let healthPercent = this.energy / totalBossHP;
		if (healthPercent <= WEAK_HEALTH_THRESHOLD && !this.isHealthStateWeak && !this.isFrozen && !this._fWeakTransitionInProgress_bl)
		{
			this._startWeakState(aImmediately_bl);
		}
	}

	_startWeakState(aImmediately_bl = false)
	{
		this.healthState = HEALTH_STATE_WEAK;
		if (!aImmediately_bl)
		{
			//stop and play CALL animation
			if (this.spineView && this.spineView.view && this.spineView.view.state)
			{
				this.spineView.removeAllListeners();
			}
			if (!this._isImmidiateCallAllowed)
			{
				//wait for TURN complete
				this.stateListener = {complete: (e) =>{
					this.spineView && this.spineView.stop();
					this._setCall();
				}};
				this.spineView.view.state.addListener(this.stateListener);
			}
			else
			{
				this._setCall();
			}
		}
		else
		{
			if (this._isImmidiateWeakStateUpdateAllowed)
			{
				this.changeTextures(this.state);
			}
		}
	}

	get _isImmidiateCallAllowed()
	{
		return this.state != STATE_TURN;
	}

	get _isImmidiateWeakStateUpdateAllowed()
	{
		return this.state === STATE_WALK;
	}

	_getCallAnimationName()
	{
		return this.direction.substr(3) + '_midlife';
	}

	//override
	showBossAppearance(aSequence_arr, aInitialParams_obj)
	{
		super.showBossAppearance(aSequence_arr, aInitialParams_obj);

		this.setStay();
	}

	//override
	_freeze(aIsAnimated_bl = true)
	{
		// if (!this._fIsFrozen_bl && !isNaN(this._fPauseTimeMarker_num))
		// {
		// 	this._fFreezingMarker_num = (new Date()).getTime();
		// }
		super._freeze(aIsAnimated_bl);
	}

	//override
	_unfreeze(aIsAnimated_bl = true)
	{
		let lFreezingDelta_num = 0;
		if (!isNaN(this._fFreezingMarker_num))
		{
			lFreezingDelta_num = (new Date()).getTime() - this._fFreezingMarker_num;
			this._fFreezingMarker_num = undefined;

			if (!isNaN(this._fPauseTimeMarker_num)) //freezing in the middle of call animation
			{
				this._fPauseTimeMarker_num = this._fPauseTimeMarker_num + lFreezingDelta_num;
				let lDelta_num = (new Date()).getTime() - this._fPauseTimeMarker_num;
				let pt = {x: this.x, y: this.y};
				pt.y += this.getCurrentFootPointPosition().y;
				pt.x += this.getCurrentFootPointPosition().x;
				this.emit(Enemy.EVENT_ON_ENEMY_ADD_TRAJECTORY_POINT, {enemyId: this.id, x: pt.x, y: pt.y, timeOffset: -lDelta_num});
				this._fPauseTimeMarker_num = (new Date()).getTime();
			}
		}

		super._unfreeze(aIsAnimated_bl);
		this._invalidateHealthState();
	}

	_pauseBossWalking()
	{
		if (!isNaN(this._fPauseTimeMarker_num)) return; // already paused

		this._fPauseTimeMarker_num = (new Date()).getTime();

		this.emit(Enemy.EVENT_ON_ENEMY_PAUSE_WALKING, {enemyId: this.id});
	}

	_resumeBossWalking()
	{
		let delta = 0;
		if (!isNaN(this._fPauseTimeMarker_num))
		{
			delta = (new Date()).getTime() - this._fPauseTimeMarker_num;
			this._fPauseTimeMarker_num = undefined;
		}

		let pt = {x: this.x, y: this.y};
		pt.y += this.getCurrentFootPointPosition().y;
		pt.x += this.getCurrentFootPointPosition().x;
		this.emit(Enemy.EVENT_ON_ENEMY_ADD_TRAJECTORY_POINT, {enemyId: this.id, x: pt.x, y: pt.y, timeOffset: -delta});

		this.emit(Enemy.EVENT_ON_ENEMY_RESUME_WALKING, {enemyId: this.id, timeOffset: delta});
	}

	_setCall()
	{
		if (!this._fWeakTransitionInProgress_bl)
		{
			this._fWeakTransitionInProgress_bl = true;
			this._pauseBossWalking();
		}

		if (this.spineView)
		{
			this.spineView.removeAllListeners();
		}

		this.changeTextures(STATE_CALL);
		this._triggerCallSound();
		this.stateListener = {complete: (e) =>{
			this.spineView && this.spineView.stop();
			this._onWeakTransitionCompleted();
		}};


		if (this.spineView && this.spineView.view.state && this.spineView.view.state)
		{
			this.spineView.view.state.timeScale = this.getSpineSpeed();
			this.spineView.view.state.addListener(this.stateListener);
		}
	}

	_onWeakTransitionCompleted()
	{
		this._fWeakTransitionInProgress_bl = false;
		this._fIsWeakTurningNeeded_bl = false;
		this.setWalk();
		this._resumeBossWalking();

		this.spineSpeed = this.getSpineSpeed();
		this.spineView.view.state.timeScale = this.spineSpeed;
	}

	_triggerCallSound()
	{
		switch (this.name)
		{
			case ENEMIES.ApeBoss:
				APP.soundsController.play('mq_boss_ape_midlife');
			break;
			case ENEMIES.GolemBoss:
				APP.soundsController.play('mq_boss_golem_midlife');
			break;
			case ENEMIES.SpiderBoss:
				APP.soundsController.play('mq_boss_spider_midlife');
			break;
		}
	}

	//override
	getStepTimers()
	{
		let timers = [];

		if (this.state == STATE_WALK)
		{
			switch (this.name)
			{
				case ENEMIES.ApeBoss:
					timers = [ {time: this.isHealthStateWeak? 0.23 : 0}, {time: this.isHealthStateWeak ? 1.52 : 0.6} ];
				break;

				case ENEMIES.GolemBoss:
					timers = [ {time: this.isHealthStateWeak ? 0.2 : 0}, {time: this.isHealthStateWeak ? 2.03 : 1.5} ];
				break;
			}
		}

		this._stepsAmount = timers.length;

		return timers;
	}

	//override
	_calculateAnimationName(stateType)
	{
		if (stateType === STATE_CALL)
		{
			return this._getCallAnimationName();
		}

		return super._calculateAnimationName(stateType);
	}

	//override
	_calculateAnimationLoop(stateType)
	{
		let animationLoop = true;

		switch(stateType)
		{
			case STATE_DEATH:
			case STATE_TURN:
			case STATE_CALL:
			{
				animationLoop = false;
				break;
			}
		}

		return animationLoop;
	}

	//override
	changeView()
	{
		if (this._fWeakTransitionInProgress_bl)
		{
			return;
		}
		super.changeView();
	}

	//override
	changeSpineView(type, noChangeFrame)
	{
		super.changeSpineView(type, noChangeFrame);

		this.changeShadowPosition();

		if (isNaN(this._fWalkStartOffset_num) && this._fWalkStartOffset_num > 0)
		{
			this.spineView.updatePosition(this._fWalkStartOffset_num);
			this._fWalkStartOffset_num = undefined;
		}
	}

	//override
	changeShadowPosition()
	{
		this.shadow.scale.set(1.7);
	}

	//override
	_restoreStateBeforeFreeze()
	{
		if (this._fWeakTransitionInProgress_bl)
		{
			this._onWeakTransitionCompleted();
		}
		else
		{
			this.setWalk();
		}
	}

	//override
	destroy()
	{
		this._fWalkStartOffset_num = undefined;
		this._fWeakTransitionInProgress_bl = false;
		this._fPauseTimeMarker_num = undefined;
		this._fFreezingMarker_num = undefined;

		//DEBUG...
		//window.removeEventListener("keydown", this._fKeyDownHandler_func, false);
		//...DEBUG
		super.destroy();
	}
}

export default BossEnemy;