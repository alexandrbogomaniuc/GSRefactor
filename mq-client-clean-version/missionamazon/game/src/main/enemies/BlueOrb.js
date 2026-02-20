import SpineEnemy from './SpineEnemy';
import { DIRECTION, STATE_WALK, STATE_STAY } from './Enemy';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import ElectrifiedOrb from '../animation/electrified_orb/ElectrifiedOrb';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const ORB_FX_SCALE = 1;
const MOVE_DISTANCES = [38, 12, -14, 0, -14, 38, -28, 6, -12, 20, 14, 38, -14, 10, -14, 22, -6];
const MOVE_SPEED = 1.3*FRAME_RATE;

class BlueOrb extends SpineEnemy
{
	static get EVENT_ORB_CALLOUT_CREATED()			{return "EVENT_ORB_CALLOUT_CREATED";}
	//override
	get isTurnState()
	{
		return false;
	}

	//override
	get isCritter()
	{
		return true;
	}

	constructor(params)
	{
		super(params);

		this._fElectrifiedOrbDeathCompleted_bln = false;
		this._fBaseDeathCompleted_bln = false;
		this._fIsCalloutAwaiting_bl = true

		this._fElectrifiedOrb_eo = this.container.addChild(new ElectrifiedOrb());
		this._fElectrifiedOrb_eo.zIndex = 200;
		this._fElectrifiedOrb_eo.scale.set(ORB_FX_SCALE);

		this._fIsFrozen_bl && (this._fElectrifiedOrb_eo.visible = false);
		this._fFirstMoveIndex_int = (this.id%3 * 4) % MOVE_DISTANCES.length;

		this._fRecoveryMoveDistance_num = 0;
		this._fRecoveryMoveProgress_num = 1;

		this._playElectrifiedOrbWeegle();
	}



	// override
	isCollision(aX_num, aY_num)
	{
		let lOffset_obj = this._getOffset();
		let lPosition_obj = {
			x: this.position.x + lOffset_obj.x,
			y: this.position.y + lOffset_obj.y
		};

		let lRadius_num = Math.sqrt(Math.pow(aX_num - lPosition_obj.x, 2) + Math.pow(aY_num  - lPosition_obj.y, 2));
		return lRadius_num <= this._getHitRectHeight();
	}

	__onBeforeNewTrajectoryApplied()
	{
		this._fRecoveryMoveDistance_num = this._getMovingCurrentY();
		this._fRecoveryMoveProgress_num = 0;
	}

	_getMovingProgress()
	{
		if (!!this.nextTurnPoint)
		{
			let lCurrentTime_num = APP.gameScreen.currentTime;
			let lTimeFinish_num = this.nextTurnPoint.time;

			let lProgress_num = 0;
			let lMoveIndex_num = this._fFirstMoveIndex_int;
			if (lCurrentTime_num <= lTimeFinish_num)
			{
				let lTimeRest_num = lTimeFinish_num - lCurrentTime_num;
				if (
					this.prevTurnPoint
					&& (
						Utils.isEqualPoints(this.prevTurnPoint, this.nextTurnPoint)
						|| (
							this.prevTurnPoint.originalPoint && Utils.isEqualPoints(this.prevTurnPoint.originalPoint, this.nextTurnPoint)
						)
					)
				)
				{
					let lNextTrajectoryPoint_obj = this._findNextTrajectoryPoint() || this.nextTurnPoint;
					lTimeRest_num = lNextTrajectoryPoint_obj.time - this.nextTurnPoint.time;
				}

				let lMovesDuration_num = 0;
				let lCurMoveSpentDuration_num = 0;
				let i = this._fFirstMoveIndex_int;
				while (true)
				{
					let lCurMoveDuration = MOVE_SPEED*Math.abs(MOVE_DISTANCES[i]);
					if (lMovesDuration_num + lCurMoveDuration >= lTimeRest_num)
					{
						lMoveIndex_num = i;
						lCurMoveSpentDuration_num = lCurMoveDuration - (lTimeRest_num - lMovesDuration_num);
						lProgress_num = lCurMoveSpentDuration_num / lCurMoveDuration;
						break;
					}

					lMovesDuration_num += lCurMoveDuration;

					i++;
					if (i >= MOVE_DISTANCES.length)
					{
						i = 0;
					}
				}
			}

			return {timeProgress: lProgress_num, moveIndex: lMoveIndex_num};
		}

		return null;
	}

	_findNextTrajectoryPoint()
	{
		if (!this.nextTurnPoint)
		{
			return null;
		}

		let lNextTurnPoint_obj = this.nextTurnPoint.time;
		let lMinTimeDelta = Number.MAX_VALUE;
		let lTrajectoryPoints_obj_arr = this.trajectory.points;

		let lNextTrajectoryPoint_obj = null;

		for (let lPoint_obj of lTrajectoryPoints_obj_arr)
		{
			if (lPoint_obj.time > lNextTurnPoint_obj)
			{
				let lDifference_num = Math.abs(lNextTurnPoint_obj - lPoint_obj.time);
				if (lDifference_num < lMinTimeDelta)
				{
					lMinTimeDelta = lDifference_num;
					lNextTrajectoryPoint_obj = lPoint_obj;
				}
			}
		}

		return lNextTrajectoryPoint_obj;
	}

	_getMovingCurrentY()
	{
		let lMoveProgress_obj = this._getMovingProgress();
		if (!lMoveProgress_obj)
		{
			return 0;
		}

		let lMoveHeight_num = MOVE_DISTANCES[lMoveProgress_obj.moveIndex];
		let lMoveTimeProgress_num = lMoveProgress_obj.timeProgress;
		let lYOffsetProgress_num = 0;

		if (lMoveTimeProgress_num <= 0.5) // up state
		{
			lYOffsetProgress_num = lMoveTimeProgress_num * 2;
		}
		else if (lMoveTimeProgress_num > 0.5 && lMoveTimeProgress_num <= 1) // down state
		{
			lYOffsetProgress_num = (1-lMoveTimeProgress_num)*2;
		}

		let lResultY_num = lYOffsetProgress_num * lMoveHeight_num;

		return this._fRecoveryMoveDistance_num * (1 - this._fRecoveryMoveProgress_num) + lResultY_num * this._fRecoveryMoveProgress_num;
	}

	_getOffset()
	{
		let lOffset_obj = super._getOffset();
		let lYOffset_num = this._getMovingCurrentY();

		lOffset_obj.y += lYOffset_num;

		return lOffset_obj;
	}

	//override
	tick()
	{
		super.tick();

		// if (this._fIsCalloutAwaiting_bl)
		// {
		// 	this._fIsCalloutAwaiting_bl = false;
		// 	this.emit(BlueOrb.EVENT_ORB_CALLOUT_CREATED);
		// }

		if(
			!this.isFrozen &&
			!this.isStayState &&
			this._fRecoveryMoveProgress_num < 1
		)
		{
			this._fRecoveryMoveProgress_num += 0.01;

			if(this._fRecoveryMoveProgress_num > 1)
			{
				this._fRecoveryMoveProgress_num = 1;
			}
		}
	}

	//override
	static getDirection()
	{
		return this.direction;
	}

	//override
	_isTrajectoryTurnPointCondition()
	{
		return false;
	}

	//override
	_calculateDirection()
	{
		return DIRECTION.LEFT_DOWN;
	}

	//override
	getTurnDirection()
	{
		return this.direction;
	}

	//override
	_getPossibleDirections()
	{
		return [0];
	}

	//override
	_isRotationOnChangeViewRequired()
	{
		return false;
	}

	//override
	_getHitRectHeight()
	{
		return 70;
	}

	//override
	_getHitRectWidth()
	{
		return 70;
	}

	//override
	changeShadowPosition()
	{
		this.shadow.alpha = 0;
	}

	//override...
	__isSimpleCollisionEnemy()
	{
		return true;
	}

	//override
	_calculateAnimationName(stateType)
	{
		let animationName = '';

		switch (stateType)
		{
			case STATE_STAY:
			case STATE_WALK:
				animationName = this.getWalkAnimationName();
				break;
		}

		return animationName;
	}

	_playElectrifiedOrbWeegle()
	{
		if (this._fElectrifiedOrb_eo && this._fElectrifiedOrb_eo.transform)
		{
			let l_seq = [
				{
					tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(0, 10) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(0, 10) }], duration: 10 * FRAME_RATE, onfinish: () =>
					{
						this._playElectrifiedOrbWeegle()
					}
				}
			];

			Sequence.start(this._fElectrifiedOrb_eo, l_seq);
		}
	}

	//override
	_playSimpleEnemyDeathFxAnimation(aIsInstantKill_bl)
	{
		this._playDeathFxAnimation(aIsInstantKill_bl);
	}

	//override
	setDeath(aIsInstantKill_bl = false, aPlayerWin_obj = null)
	{
		this.spineView && Sequence.destroy(Sequence.findByTarget(this.spineView));

		super.setDeath(aIsInstantKill_bl, aPlayerWin_obj);
	}

	//override
	_playDeathFxAnimation(aIsInstantKill_bl)
	{
		super._playDeathFxAnimation(aIsInstantKill_bl);

		if (!aIsInstantKill_bl && this._fElectrifiedOrb_eo)
		{
			this._fElectrifiedOrb_eo.once(ElectrifiedOrb.EVENT_ON_ANIMATIONS_DESTROYED, this._onDeathFxAnimationEnded, this);
			this._fElectrifiedOrb_eo.once(ElectrifiedOrb.EVENT_ON_DISAPPEAR_ANIMATION_ENDED, this._onDeathFxAnimationEnded, this);

			this._fElectrifiedOrb_eo.onDeathRequired();
		}
		else
		{
			if (aIsInstantKill_bl && this._fElectrifiedOrb_eo)
			{
				this._fElectrifiedOrb_eo.destroy();
			}

			this._fElectrifiedOrbDeathCompleted_bln = true;
		}
	}

	_onDeathFxAnimationEnded()
	{
		this._fElectrifiedOrb_eo.off(ElectrifiedOrb.EVENT_ON_ANIMATIONS_DESTROYED, this._onDeathFxAnimationEnded, this);
		this._fElectrifiedOrb_eo.off(ElectrifiedOrb.EVENT_ON_DISAPPEAR_ANIMATION_ENDED, this._onDeathFxAnimationEnded, this);

		this._fElectrifiedOrbDeathCompleted_bln = true;
		this._validateDeath();
	}

	onDeathFxAnimationCompleted()
	{
		this._fBaseDeathCompleted_bln = true;
		this._validateDeath();
	}

	_validateDeath()
	{
		if (this._fElectrifiedOrbDeathCompleted_bln && this._fBaseDeathCompleted_bln)
		{
			this._onAllDeathFxCompleted();
		}
	}

	_onAllDeathFxCompleted()
	{
		super.onDeathFxAnimationCompleted();
	}

	//override
	_freezeIfRequired()
	{
		//disable freeze
	}

	/*_playDeathFxAnimation(aIsInstantKill_bl)
	{
		console.log("_playDeathFxAnimation="+aIsInstantKill_bl);
		if (aIsInstantKill_bl)
		{
			this._fElectrifiedOrb_eo && this._fElectrifiedOrb_eo.destroy();
		}

		super._playDeathFxAnimation(aIsInstantKill_bl);
	}*/

	//override
	_calcWalkAnimationName()
	{
		return '0_walk';
	}

	//override
	destroy(purely)
	{
		this.spineView && Sequence.destroy(Sequence.findByTarget(this.spineView));
		this._fElectrifiedOrb_eo && Sequence.destroy(Sequence.findByTarget(this._fElectrifiedOrb_eo));
		//this._fElectrifiedOrb_eo && this._fElectrifiedOrb_eo.destroy();

		super.destroy(purely);

		this._fElectrifiedOrbDeathCompleted_bln = null;
		this._fBaseDeathCompleted_bln = null;
		this._fTrajectoryPoints_obj_arr = null;
	}
}

export default BlueOrb;