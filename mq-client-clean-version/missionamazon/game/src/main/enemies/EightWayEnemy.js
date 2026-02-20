import SpineEnemy from './SpineEnemy';
import { DIRECTION, TURN_DIRECTION, STATE_DEATH, STATE_TURN } from './Enemy';
import { ENEMIES, FRAME_RATE} from '../../../../shared/src/CommonConstants';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ProfilingInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/profiling/ProfilingInfo';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';

const TRIPPLE_SHADOWS_POSITIONS = {			
	[DIRECTION.LEFT_DOWN]: {
		shadow2: 	{x: -194.81, 	y: -290.99},
		shadow3: 	{x: 406.89, 	y: 0}
	},
	[DIRECTION.RIGHT_DOWN]: {
		shadow2: 	{x: 194.81, 	y: -290.99},
		shadow3: 	{x: -406.89, 	y: 0}
	},
	[DIRECTION.RIGHT_UP]: {		//should be corrected, not applied in trajectories now
		shadow2: 	{x: 61.5, 		y: 299.58},
		shadow3: 	{x: -365.06, 	y: -113.09}
	},
	[DIRECTION.LEFT_UP]: {		//should be corrected, not applied in trajectories now
		shadow2: 	{x: 61.5, 		y: 299.58},
		shadow3: 	{x: -365.06, 	y: -113.09}
	},
	[DIRECTION.DOWN]: {			//should be corrected, not applied in trajectories now
		shadow2: 	{x: -387.74, 	y: 218.98},
		shadow3: 	{x: 373.67, 	y: 126.57}
	},
	[DIRECTION.RIGHT]: {
		shadow2: 	{x: -164.79, 	y: -142.5},
		shadow3: 	{x: -87.89, 	y: 157.99}
	},
	[DIRECTION.UP]: { 			//should be corrected, not applied in trajectories now
		shadow2: 	{x: 429.31, 	y: 42.43},
		shadow3: 	{x: -431.81, 	y: 127.72}
	},
	[DIRECTION.LEFT]: { 
		shadow2: 	{x: 87.89, 		y: -157.99},
		shadow3: 	{x: 164.79, 	y: 142.5}
	}
}

class EightWayEnemy extends SpineEnemy
{
	constructor(params)
	{
		super(params);

		this._fTurnOrder_num = 0;
		this.shadow2 = null;
		this.shadow3 = null;
		this.ShadowPositionChange_seq = null;
		this.ShadowPositionChange2_seq = null;
	}

	getSpineSpeed()
	{
		let lSpeed_num = 0.15;
		let lSpineSpeed_num = lSpeed_num * this.currentTrajectorySpeed;

		return lSpineSpeed_num;
	}

	//override...
	__isSimpleCollisionEnemy()
	{
		return true;
	}

	//override
	_calcCurrentTrajectorySpeed()
	{
		if (this.name === ENEMIES.RedAnt || this.name === ENEMIES.BlackAnt)
		{
			if (!!this.prevTurnPoint && !!this.nextTurnPoint)
			{
				let lDeltaTime = this.nextTurnPoint.time - this.prevTurnPoint.time;
				let lDistance = Utils.getDistance(this.nextTurnPoint, this.prevTurnPoint)

				if (~~lDistance == 0)
				{
					return 0;
				}
				
				let lCurTrajectorySpeed = +(lDistance/lDeltaTime*1000/11.45).toFixed(2);

				return lCurTrajectorySpeed;
			}
		}

		return super._calcCurrentTrajectorySpeed();
		
	}

	playDeathSound()
	{
		if (Math.random() > 0.5) return;

		let randomSoundIndex = 1;
		let soundName = "";
		// let lowProfile = APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM);

		if (!!soundName)
		{
			APP.soundsController.play(soundName);
		}		
	}

	//override
	addShadow()
	{
		super.addShadow();
		if ((this.name == ENEMIES.BlackAnt || this.name == ENEMIES.RedAnt) && !this.shadow2) //pack of 3
		{
			let lPositions = TRIPPLE_SHADOWS_POSITIONS[this.direction];
			let lShadowPosition = this.shadow.view.position;
			this.shadow2 = this.shadow.addChild(this._createShadow());
			this.shadow3 = this.shadow.addChild(this._createShadow());
			this.shadow2.position.set(lShadowPosition.x + lPositions.shadow2.x*0.3, lShadowPosition.y + lPositions.shadow2.y*0.3);
			this.shadow3.position.set(lShadowPosition.x + lPositions.shadow3.x*0.3, lShadowPosition.y + lPositions.shadow3.y*0.3);
		}
	}

	_turnShadow(aDirection = this.direction)
	{
		if (!this.shadow2)
		{
			if (this.shadow.children[1])
			{
				this.shadow2 = this.shadow.children[1];
				this.shadow3 = this.shadow.children[2];
			}
			else 
			{
				this.addShadow()
			}
		}

		let lPositions = TRIPPLE_SHADOWS_POSITIONS[aDirection];
		let lShadowPosition = this.shadow.view.position;
		let lPosition_seq = [
			{tweens: [{prop: 'x', to: this.shadow2.position.x}, {prop: 'y', to: this.shadow2.position.y}]},
			{tweens: [{prop: 'x', to: lShadowPosition.x + lPositions.shadow2.x*0.3}, {prop: 'y', to: lShadowPosition.y + lPositions.shadow2.y*0.3}], duration: 5.5 * FRAME_RATE}
		];
		let lPosition_seq2 = [
			{tweens: [{prop: 'x', to: this.shadow3.position.x}, {prop: 'y', to: this.shadow3.position.y}]},
			{tweens: [{prop: 'x', to: lShadowPosition.x + lPositions.shadow3.x*0.3}, {prop: 'y', to: lShadowPosition.y + lPositions.shadow3.y*0.3}], duration: 5.5 * FRAME_RATE}
		];
		this.ShadowPositionChange_seq = Sequence.start(this.shadow2.position, lPosition_seq);
		this.ShadowPositionChange2_seq = Sequence.start(this.shadow3.position, lPosition_seq2);
	}

	//override
	_resumeAfterUnfreeze()
	{
		super._resumeAfterUnfreeze();
		this.ShadowPositionChange_seq && this.ShadowPositionChange_seq.resume();
		this.ShadowPositionChange2_seq && this.ShadowPositionChange2_seq.resume();
	}

	//override
	_freeze(aIsAnimated_bl = true)
	{
		super._freeze(aIsAnimated_bl);
		if (!this._fIsFrozen_bl)
		{
			this.ShadowPositionChange_seq && this.ShadowPositionChange_seq.pause();
			this.ShadowPositionChange2_seq && this.ShadowPositionChange2_seq.pause();
		}
	}

	_getFreezeGroundScaleCoef()
	{
		return 0.9;
	}

	setViewPos()
	{
		this.viewPos = {x: 0, y: 0};
	}

	changeShadowPosition()
	{
		let lPos_obj = {x: 1, y: 2};
		let lScale_num = 0.4;
		let lAlpha_num = 0.72;

		this.shadow.position.set(lPos_obj.x, lPos_obj.y);
		this.shadow.scale.set(lScale_num);
		this.shadow.alpha = lAlpha_num;
	}

	getLocalCenterOffset()
	{
		return {x: 0, y: -6};
	}

	static getDirection(angle)
	{
		let direction = DIRECTION.RIGHT;
		if (angle > Math.PI*2) angle -= Math.PI*2;

		if (angle > Math.PI*1/8) direction = DIRECTION.RIGHT_DOWN;
		if (angle > Math.PI*3/8) direction = DIRECTION.DOWN;
		if (angle > Math.PI*5/8) direction = DIRECTION.LEFT_DOWN;
		if (angle > Math.PI*7/8) direction = DIRECTION.LEFT;
		if (angle > Math.PI*9/8) direction = DIRECTION.LEFT_UP;
		if (angle > Math.PI*11/8) direction = DIRECTION.UP;
		if (angle > Math.PI*13/8) direction = DIRECTION.RIGHT_UP;
		if (angle > Math.PI*15/8) direction = DIRECTION.RIGHT;

		return direction
	}

	getTurnDirection(direction)
	{
		this._calculateTurnOrder(direction);

		let d1 = +this.direction.slice(3);
		let d2 = +direction.slice(3);

		let lDir_str = "";
		if (d1 >= 180)
		{
			if (d2 >= 180)
			{
				if (d2 > d1)
				{
					lDir_str = TURN_DIRECTION.CCW;
				}
				else
				{
					lDir_str = TURN_DIRECTION.CW;
				}
			}
			else if (d1 - d2 > 180)
			{
				lDir_str = TURN_DIRECTION.CCW;
			}
			else
			{
				lDir_str = TURN_DIRECTION.CW;
			}
		}
		else
		{
			if (d2 < 180)
			{
				if (d2 > d1)
				{
					lDir_str = TURN_DIRECTION.CCW;
				}
				else
				{
					lDir_str = TURN_DIRECTION.CW;
				}
			}
			else if (d2 - d1 > 180)
			{
				lDir_str = TURN_DIRECTION.CW;
			}
			else
			{
				lDir_str = TURN_DIRECTION.CCW;
			}
		}

		return lDir_str;
	}

	_calculateTurnOrder(direction)
	{
		this._fTurnOrder_num = 0;

		let d1 = +this.direction.slice(3);
		let d2 = +direction.slice(3);

		if (d1 >= 180)
		{
			if (d2 >= 180)
			{
				if (d2 > d1)
				{
					this._fTurnOrder_num = -1;
				}
				else
				{
					this._fTurnOrder_num = 1;
				}
			}
			else if (d1 - d2 > 180)
			{
				this._fTurnOrder_num = -1;
			}
			else
			{
				this._fTurnOrder_num = 1;
			}
		}
		else
		{
			if (d2 < 180)
			{
				if (d2 > d1)
				{
					this._fTurnOrder_num = -1;
				}
				else
				{
					this._fTurnOrder_num = 1;
				}
			}
			else if (d2 - d1 > 180)
			{
				this._fTurnOrder_num = 1;
			}
			else
			{
				this._fTurnOrder_num = -1;
			}
		}

		let lDiv_num = Math.abs(d2 - d1);
		if (lDiv_num > 45 && lDiv_num != 315)
		{
			this._fTurnOrder_num *= 2;
		}

		if (this._fTurnOrder_num > 2) this._fTurnOrder_num = 2;
		if (this._fTurnOrder_num < -2) this._fTurnOrder_num = -2;
		if (this._fTurnOrder_num == 0) this._fTurnOrder_num = 1;
	}

	getTurnAnimationName()
	{
		let lNextAngle_num = +this.direction.substr(3);
		let lDirAngles_arr = this._getPossibleDirections();

		let lPrevId_num = (lDirAngles_arr.indexOf(lNextAngle_num) + this._fTurnOrder_num) % lDirAngles_arr.length;
		if (lPrevId_num < 0) lPrevId_num = lDirAngles_arr.length + lPrevId_num;
		let lPrevAngle_num = lDirAngles_arr[lPrevId_num];

		let lAnimName_str = lPrevAngle_num + "_to_" + lNextAngle_num + "_turn";
		return lAnimName_str;
	}

	_calcWalkAnimationName(aDirection_str, aBaseWalkAnimationName_str = "walk")
	{
		let lSuffix_str = '0_';

		switch (aDirection_str)
		{
			case DIRECTION.LEFT_DOWN:	lSuffix_str = '0_';		break;
			case DIRECTION.DOWN:		lSuffix_str = '45_';	break;
			case DIRECTION.RIGHT_DOWN:	lSuffix_str = '90_';	break;
			case DIRECTION.RIGHT:		lSuffix_str = '135_';	break;
			case DIRECTION.RIGHT_UP:	lSuffix_str = '180_';	break;
			case DIRECTION.UP:			lSuffix_str = '225_';	break;
			case DIRECTION.LEFT_UP:		lSuffix_str = '270_';	break;
			case DIRECTION.LEFT:		lSuffix_str = '315_';	break;
		}

		return lSuffix_str + aBaseWalkAnimationName_str;
	}

	_calculateDirection(aOptAngle_num=undefined)
	{
		let lAngle_num = aOptAngle_num !== undefined ? aOptAngle_num : this.angle;
		
		return EightWayEnemy.getDirection(lAngle_num);
	}

	changeSpineView(type, noChangeFrame)
	{
		super.changeSpineView(type, noChangeFrame);

		if (this.state === STATE_TURN)
		{
			let lSpineName_str = this._calculateSpineTurnName(this._fAnimationName_str, this.imageName);
			let lAngle_str = "";
			for (let i = lSpineName_str.length-1; i >= 0; --i)
			{
				if (isNaN(lSpineName_str[i])) break;
				lAngle_str = lSpineName_str[i] + lAngle_str;
			}

			this.setSpineViewPos(+lAngle_str);
			this.spineView.position.set(this.spineViewPos.x, this.spineViewPos.y);
			if (this.name == ENEMIES.BlackAnt || this.name == ENEMIES.RedAnt)  this._turnShadow();
		}
		else if (this.state !== STATE_DEATH)
		{
			this.setSpineViewPos();
			this.spineView.position.set(this.spineViewPos.x, this.spineViewPos.y);
		}
	}

	_getPossibleDirections()
	{
		return [0, 45, 90, 135, 180, 225, 270, 315];
	}

	setSpineViewPos(aAngle_num)
	{
		let lPos_obj = {x: 0, y: 0};

		this.spineViewPos = lPos_obj;
	}

	_isDirectionStraight(aDir_str)
	{
		let lAngle_num = +aDir_str.substr(3);

		return this._isAngleStraight(lAngle_num);
	}

	_isAngleStraight(aAngle_num)
	{
		if (aAngle_num == 45 || aAngle_num == 135 || aAngle_num == 225 || aAngle_num == 315)
		{
			return true;
		}

		return false;
	}

	// tick()
	// {
	// 	super.tick();

	// 	if (this._fIsFrozen_bl || this._isHVRisingUpInProgress)
	// 	{
	// 		return;
	// 	}
	// 	else
	// 	{
	// 		this._destroyFrozenSprites();
	// 	}
	// }

	destroy(purely)
	{
		super.destroy(purely);

		this._fTurnOrder_num = null;
		this.shadow2 = null;
		this.shadow3 = null;
		this.ShadowPositionChange_seq = null;
		this.ShadowPositionChange2_seq = null;
	}
}

export default EightWayEnemy;