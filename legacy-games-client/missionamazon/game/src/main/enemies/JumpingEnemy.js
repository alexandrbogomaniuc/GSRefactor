import SpineEnemy from './SpineEnemy';
import { STATE_WALK, STATE_TURN} from './Enemy';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

const SPAWN_POINT = {x: 960, y: 0}

class JumpingEnemy extends SpineEnemy
{
	constructor(params)
	{
		super(params);
	}

	_invalidateStates()
	{
		this._onAppearingSuspicion();
		super._invalidateStates();
	}

	//APPEARANCE...
	_onAppearingSuspicion()
	{
		if (
				Utils.isEqualPoints(this.trajectory.points[0], this.trajectory.points[1]) && (this.trajectory.points[0].time < this.trajectory.points[1].time)
				&&  !this._fCryogunsController_cgs.i_isEnemyFrozen(this.id)
			)
		{
			//appearance jumping is needed
			this._startAppearing();
		}
	}

	_resetAppearing()
	{
		this._fIsAppearing_bl = false;
		this._fAppearanceDuration_num = undefined;
		this._fAppearanceStartTime_num = undefined;
		this._fAppearingPercent_num = undefined;
		this._fFreezePostponedFunction_func = null;
		this._resetJumping();
	}

	_startAppearing()
	{
		this._fIsAppearing_bl = true;
		this._fAppearingPercent_num = 0;
		this._fFreezePostponedFunction_func = null;
		this._updateAppearanceTrajectory();
	}

	get _isAppearingInProgress()
	{
		return this._fIsAppearing_bl;
	}

	_updateAppearanceTrajectory()
	{
		let lSpawnPoint_pt = {x: SPAWN_POINT.x, y: SPAWN_POINT.y, time: this.trajectory.points[0].time}
		this.trajectory.points.unshift(lSpawnPoint_pt);
		//[spawnPoint, landingPoint, startWalkingPoint, ...]

		this._fAppearanceTurnFlag_bln = true;

		let lPrevPoint_pt = this.trajectory.points[0];
		let lNextPoint_pt = this.trajectory.points[2];
		this._fAppearanceDuration_num = lNextPoint_pt.time - lPrevPoint_pt.time;
		this.trajectory.points[1].time = this._fAppearanceDuration_num/3 + this.trajectory.points[0].time
		this._fAppearanceStartTime_num = lPrevPoint_pt.time;
		this.initialDirection = null;

		this.position.set(lSpawnPoint_pt.x, lSpawnPoint_pt.y);
		this.appearancePositionUpdated = true; //position was updated but GameScreen already thinks it is previous - we need to notify it
	}

	_updateAppearanceAnimation(aAnimationScale_num = 1)
	{
		if (	!this.spineView ||
				!this.spineView.view ||
				!this.spineView.view.state ||
				!this.spineView.view.state.tracks ||
				!this.spineView.view.state.tracks[0] ||
				!this.parent
			)
		{
			return;
		}

		this.appearancePositionUpdated = false;

		// Need to do this once
		if ((this.state === STATE_TURN) && this._fAppearanceTurnFlag_bln)
		{
			this._fAppearanceTurnFlag_bln = false;
			this.changeTextures(STATE_WALK, false);
		}

		this.spineView.stop();
		let lAnimationTiming_num = this.spineView.view.state.tracks[0].animationLast;

		let lCurrentTimeDiff_num = APP.currentWindow.currentTime - this._fAppearanceStartTime_num;
		let lPer_num = Math.min(1, lCurrentTimeDiff_num/this._fAppearanceDuration_num);
		let lAnimationCoef_num = lPer_num * 0.6 + 0.4;
		this._fAppearingPercent_num = lPer_num; // current percent of appearing completion
		let lIsInvalidated_bl = this._invalidatePostponed();

		if (lPer_num >= 1)
		{
			this._resetAppearing();
			this.setWalk();
			return;
		}

		if (lIsInvalidated_bl)
		{
			return;
		}

		let timeScale = this.spineView.view.state.timeScale;
		this.spineView.updatePosition(lAnimationTiming_num > 0 ? -lAnimationTiming_num : 0);
		this.spineView.updatePosition(1 * timeScale * lAnimationCoef_num * aAnimationScale_num);


	}

	_invalidatePostponed()
	{
		return this._freezeSuspicion();
	}
	//...APPEARANCE

	_resetJumping()
	{
		this.changeView();
	}

	//override
	changeSpineView(type, noChangeFrame)
	{
		if (this._isAppearingInProgress && this.spineView)
		{
			return;
		}
		super.changeSpineView(type, noChangeFrame);
	}

	//override
	getSpineSpeed()
	{
		if (this._isAppearingInProgress) return 1;

		return super.getSpineSpeed();
	}

	tick(delta)
	{
		super.tick(delta);

		if (this._isAppearingInProgress && !this.isFrozen)
		{
			this._updateAppearanceAnimation();
		}
	}

	//override
	updateTrajectory(aTrajectory_obj)
	{
		if (this._isAppearingInProgress)
		{
			//check if [0] and [1] points are equal
			if (Utils.isEqualPoints(aTrajectory_obj.points[0], aTrajectory_obj.points[1]))
			{
				//it means this is freezing while appearing in progress - we need to update points so that the flying animation wouldn't break
				aTrajectory_obj.points.shift();
				aTrajectory_obj.points.shift();
				aTrajectory_obj.points.unshift(this.trajectory.points[0], this.trajectory.points[1], this.trajectory.points[2]);
			}
		}
		this.trajectory = aTrajectory_obj;
		this.speed = aTrajectory_obj.speed || this.speed;
		this.changeView();
	}

	//override
	_freeze(aIsAnimated_bl = true)
	{
		if (this._fAppearingPercent_num < 0.5)
		{
			this._fFreezePostponedFunction_func = super._freeze.bind(this, aIsAnimated_bl);
			return;
		}
		this._fStartFreezeTime_num = (new Date()).getTime();
		super._freeze(aIsAnimated_bl);
	}

	_freezeSuspicion()
	{
		if (this._fFreezePostponedFunction_func && this._fAppearingPercent_num >= 0.5)
		{
			this._fFreezePostponedFunction_func();
			this._fFreezePostponedFunction_func = null;
			return true;
		}
		return false;
	}
}

export default JumpingEnemy;