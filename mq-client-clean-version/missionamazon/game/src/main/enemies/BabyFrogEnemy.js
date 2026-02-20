import JumperEnemy from './JumperEnemy';
import { DIRECTION } from './Enemy';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class BabyFrogEnemy extends JumperEnemy {

	get isBabyFrog()
	{
		return true;
	}

	//override
	get turnPostfix()
	{
		return "";
	}

	//override
	get _maxJumpDistance()
	{
		return 138+20; // 115+20
	}

	//override
	get _prepareToJumpPercents()
	{
		return 0.27;
	}

	//override
	get _landingPercents()
	{
		return 0.17;
	}

	//override
	get _animationTimeScale()
	{
		return 0.833;
	}

	//override
	changeShadowPosition()
	{
		this.shadow.position.set(0, -this._fJumpOffsetY_num);
		this.shadow.scale.set(this.getScaleCoefficient() * 4);
	}

	//override
	_getBaseWalkAnimationName()
	{
		return "walk";
	}

	//override
	getScaleCoefficient()
	{
		return 0.18;
	}

	//override
	getSpineSpeed()
	{
		if (this.isTurnState)
		{
			return 1.2;
		}
		else
		{
			return 1;
		}
	}

	_calcHeightShiftPercent(aCurAnimPercent_num, aStartJumpPercent_num, aEndJumpPercent_num)
	{
		let curJumpPercent = (aCurAnimPercent_num - aStartJumpPercent_num) / (aEndJumpPercent_num - aStartJumpPercent_num);
		let jumpTopPercent = 0.6;
		
		if (curJumpPercent <= jumpTopPercent)
		{
			return curJumpPercent/jumpTopPercent;
		}
		else
		{
			return 1-(curJumpPercent-jumpTopPercent)/(1-jumpTopPercent);
		}
	}

	_getHitRectWidth()
	{
		return 45;
	}

	_getHitRectHeight()
	{
		return 40;
	}

	_getExplodeIceScaleCoefficient()
	{
		return this.getScaleCoefficient() * 3;
	}

	_resetLocalCenterOffset()
	{
		return {x: 0, y: -10};
	}

	_calcLocalCenterOffset()
	{
		if (!this.spineView) return {x: 0, y: 0};

		//update current offset point
		let lIsYOffsetNeeded_bln = Boolean(this.direction == DIRECTION.RIGHT_UP || this.direction == DIRECTION.LEFT_UP);
		let lYOffset_num = 0;

		let lXOffset_num = 0;
		switch (this.direction)
		{
			case DIRECTION.RIGHT_UP:
			case DIRECTION.RIGHT_DOWN:
				lXOffset_num = 15;
				break;
			case DIRECTION.LEFT_UP:
				lXOffset_num = -10;
				break;
		}

		let lAnimationTiming_num = this.spineView.view.state.tracks[0].animationLast;
		let timeScale = this.spineView.view.state.timeScale;
		let lPer_num =  (lAnimationTiming_num / timeScale) / 1.7; //[0..1]

		if (lIsYOffsetNeeded_bln)
		{
			let x = lPer_num;
			if (x < 0.2)
			{
				x = -1;
			}
			else if (x > 0.7)
			{
				x = 1;
			}
			else
			{
				const delta = 1 - 0.7 + 0.2;
				//convert [0.2..0.7] --> [0..1]
				x = (x - 0.2) / delta;
				//convert [0..1] --> [-1..1]
				x = (x - 0.5) * 2;
			}
			lYOffset_num = 25 * (Math.pow(x, 2)) - 30;
		}

		return {x: lXOffset_num, y: lYOffset_num};
	}

	//override
	_forceHVRisingUpIfRequired()
	{
		this._onHVEnemyReadyToGo();
	}

	_prepareTrajectory()
	{
		if (this.isBabyFrog && this.startPosition)
		{
			this.trajectory.points[0].x = this.startPosition.x;
			this.trajectory.points[0].y = this.startPosition.y - 1; // to guarantee baby frogs to be beside the smoke
			this.trajectory.points[0].time = APP.gameScreen.currentTime;

			if (this.trajectory.points[1] && Math.abs(this.trajectory.points[1].time - this.trajectory.points[0].time) < 300)
			{
				this.trajectory.points[1].time = this.trajectory.points[0].time + 300;
			}

		}
	}

	//override
	_onJumpFinish()
	{
		this._fLandingCounter_int++;

		if (this._fLandingCounter_int === 1) // && this.params.needShowSound )
		{
			let soundName = "small_frog_land";
			APP.soundsController.play(soundName);
		}
	}
}

export default BabyFrogEnemy;