import SpineEnemy from './SpineEnemy';
import { STATE_WALK, STATE_TURN, DIRECTION } from './Enemy';
import Enemy from './Enemy';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { ENEMIES } from '../../../../shared/src/CommonConstants';

const JUMP_PROBABILITY = 0.5;
const JUMP_MIN_Y_POSITION = 280;
const JUMP_MIN_ANIMATION_LENGTH = 1330; // frames
const JUMP_DEFAULT_SPEED = 1.33;// full jump animation with spineSpeed=1 plays for 1330ms

const SPAWN_POINT = {x: 960, y: 0}

const MIN_RUNNING_SPEED = 8.5;
const MIN_RUNNING_TURN_SPEED = 4;
const MIN_WALKING_TURN_SPEED = 1.5;

class RunWithJumpEnemy extends SpineEnemy {

	constructor(params)
	{
		super(params);
		this._fIsJumping_bl = false;
	}

	get isWalkTypeRunning()
	{
		return this.speed > MIN_RUNNING_SPEED;
	}

	//override
	get minTurnSpeed()
	{
		return this.isWalkTypeRunning ? MIN_RUNNING_TURN_SPEED : MIN_WALKING_TURN_SPEED;
	}

	//override
	getImageName(aName_str)
	{
		let lImageName_str;

		switch (aName_str)
		{
			case ENEMIES.SkullboneRunner:
				lImageName_str = 'enemies/skullbone/Skullbone';
			break;

			default: throw new Error('imageName is undefined for ' + aName_str);
		}

		return lImageName_str;
	}

	//override
	getScaleCoefficient()
	{
		let lCoef_num = 1;

		switch (this.name)
		{
			case ENEMIES.SkullboneRunner:
				lCoef_num = 1*1.15;
			break;
		}

		return lCoef_num;
	}

	//override
	playDeathSound()
	{
		if (Math.random() > 0.5)
		{
			switch (this.name)
			{
				case ENEMIES.SkullboneRunner:
					APP.soundsController.play("death_skullbone");
				break;
			}
		}
	}

	//override
	_getHitRectWidth()
	{
		let lHitWidth_num = 0;

		switch (this.name)
		{
			case ENEMIES.SkullboneRunner:
				lHitWidth_num = 60;
			break;
		}

		return lHitWidth_num;
	}

	//override
	_getHitRectHeight()
	{
		let lHitHeight_num = 0;

		switch (this.name)
		{
			case ENEMIES.SkullboneRunner:
				lHitHeight_num = 120;
			break;
		}

		return lHitHeight_num;
	}

	//override
	changeShadowPosition()
	{
		if (!this.isWalkTypeRunning)
		{
			return super.changeShadowPosition();
		}

		let x = 0, y = 0, scale = 1;

		switch(this.direction)
		{
			case DIRECTION.LEFT_UP:
				x = 8;
				y = 4;
			break;
			case DIRECTION.RIGHT_UP:
				x = 8;
				y = 5;
			break;
			case DIRECTION.RIGHT_DOWN:
				x = -6;
				y = 1;
			break;
		}

		this.shadow.position.set(x, y);
		this.shadow.scale.set(scale);
	}

	_resetJumping()
	{
		this._fIsJumping_bl = false;
		this.changeView();
	}

	//override
	_calcWalkAnimationName(aDirection_str)
	{
		let lWalkingAnimationName_str = this._fIsJumping_bl ? "jump" : this.isWalkTypeRunning ? "run" : "walk";
		return super._calcWalkAnimationName(aDirection_str, lWalkingAnimationName_str);
	}

	//override
	getSpineSpeed()
	{
		if (this._fIsJumping_bl)
			return this.jumpingSpineSpeed;

		let lSpeed_num = 0.2;
		switch (this.name)
		{
			case ENEMIES.SkullboneRunner:
				if (this.isWalkTypeRunning)
				{
					lSpeed_num = 0.05;
				}
				else
				{
					lSpeed_num = 0.14;
				}
			break;
		}

		return lSpeed_num * this.speed;
	}

	get jumpingSpineSpeed()
	{
		return 0.12 * this.speed;
	}

	//override
	changeTextures(type, noChangeFrame)
	{
		this.spineView && this.spineView.view && (this.spineView.view.state.onChange = null);

		if (type === STATE_WALK)
		{
			// jump if possible
			this._fIsJumping_bl = this.isJumpPossible
		}

		super.changeTextures(type, noChangeFrame);

		if (type === STATE_WALK)
		{
			this.spineView.view.state.onComplete = ((e) => {
				this.changeTextures(type, noChangeFrame)}
			);
		}
	}

	get isJumpPossible()
	{
		let lMinJumpAnimationDuration_num = JUMP_MIN_ANIMATION_LENGTH / this.jumpingSpineSpeed; //= ms
		let lTimeTillNextTurn_num = (this.nextTurnPoint ? this.nextTurnPoint.time : APP.currentWindow.currentTime) - APP.currentWindow.currentTime;
		return this.isWalkTypeRunning && (
				Math.random() > (1 - JUMP_PROBABILITY)
				&& this.getGlobalPosition().y > JUMP_MIN_Y_POSITION
				&& lTimeTillNextTurn_num > lMinJumpAnimationDuration_num
			);
	}

	//override
	getLocalCenterOffset()
	{
		if (!this._fIsJumping_bl || !this.spineView)
		{
			return super.getLocalCenterOffset();
		}

		let lAnimationTiming_num = Math.max(0, this.spineView.view.state.tracks[0].animationLast * 1000);
		let timeScale = this.spineView.view.state.timeScale;
		let lAnimationLength_num = JUMP_MIN_ANIMATION_LENGTH;
		let lPercent_num = lAnimationTiming_num/ lAnimationLength_num;

		if (lPercent_num < 0.2 || lPercent_num > 0.7) return super.getLocalCenterOffset();
		lPercent_num = Math.min(1, Math.max(0, lPercent_num - 0.2) * 100/50);
		let lCoef_num = (lPercent_num > 0.5 ? (1 - lPercent_num) : lPercent_num) * 2;

		let pos = super.getLocalCenterOffset();
		pos.y -= 175*lCoef_num;
		return pos;
	}

	//override
	updateTrajectory(aTrajectory_obj)
	{
		this.trajectory = aTrajectory_obj;
		this.speed = aTrajectory_obj.speed || this.speed;
		if (this._fIsJumping_bl)
		{
			if (!this.isFrozen)
			{
				//speed up current jumping animation
				this.spineView.view.state.timeScale *= 4;
			}
			//wait for jumping animation completion
			this.spineView.view.state.onComplete = ((e) => {
				this._resetJumping();
			});
		}
		else
		{
			this.changeView();
		}
	}
}

export default RunWithJumpEnemy;