import Enemy from './Enemy';
import JumpingEnemy from './JumpingEnemy';
import { STATE_WALK, STATE_TURN, STATE_STAY, STATE_DEATH, DIRECTION, TURN_DIRECTION } from './Enemy';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameScreen from '../GameScreen';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

// Default speed for Jumper spine animation, based on current movement speed of Jumper.
const JUMPER_DEFAULT_SPEED = 1;
class JumperEnemy extends JumpingEnemy
{
	constructor(aParams_obj)
	{
		super(aParams_obj);

		this._fIsStartAnimationPlayed_bl = false;
		this._fLandingActionsCompleted_bl = false;
		this._fLandingCounter_int = 0;
		this._fJumpCounter_num = 0;
		this._fNextJumpCoundCount_num = Utils.random(3, 5);
		this._fJumpOffsetY_num = 0;
	}

	_createView()
	{
		// Jumper animation speed changes dynamically, so we need to store it in variable.
		this._fBeforeFreezeCurrentPath_pt_arr = null;
		this._fCryogunsController_cgs = APP.currentWindow.cryogunsController;
		this._fLocalCenterOffset_obj = null;
		this._fJumpCounter_num = 0;
		this._fNextJumpCoundCount_num = Utils.random(3, 5);

		super._createView();
	}

	get _maxJumpDistance()
	{
		return 333+20;
	}

	get _prepareToJumpPercents()
	{
		return 0.14; //it was 0.14
	}

	get _landingPercents()
	{
		return 0.24; //(1-0.76); //it was 0.24
	}

	get _jumpSettings()
	{
		return {
			Start: this._prepareToJumpPercents,
			End: 1-this._landingPercents,
			Height: this._maxJumpDistance/2
		}		
	}

	get _animationTimeScale()
	{
		return 1.1667;
	}

	//override
	_calcWalkAnimationName(aDirection_str)
	{
		return super._calcWalkAnimationName(aDirection_str, this._getBaseWalkAnimationName());
	}

	_getBaseWalkAnimationName()
	{
		return "jump";
	}

	//override
	getScaleCoefficient()
	{
		return 1*1.23;
	}

	//override
	_onAppearingSuspicion()
	{
		super._onAppearingSuspicion();

		if (!this._isAppearingInProgress)
		{
			this._prepareTrajectory();
		}

		this._jumperTrajectoryUpdate(false, this._isAppearingInProgress ? 3 : 1);
	}

	/**
	 * Prepares trajectory from server:
	 * - Throws away useless points, if they are out of timeline;
	 * - Updates start point time to current to be sure that animation and time would be synced.
	 */
	_prepareTrajectory()
	{
		while (this.trajectory.points.length)
		{
			let lPoint1_obj = this.trajectory.points[0];
			let lPoint2_obj = this.trajectory.points[1];

			if (lPoint1_obj && lPoint2_obj)
			{
				if (lPoint2_obj.time < APP.gameScreen.currentTime)
				{
					this.trajectory.points.shift();
				}
				else
				{
					let lCurrentPosition_obj = APP.gameScreen.getEnemyPositionInTheFuture(this.id);
					if (lCurrentPosition_obj)
					{
						this.trajectory.points[0].time = APP.gameScreen.currentTime;
						this.trajectory.points[0].x = lCurrentPosition_obj.x;
						this.trajectory.points[0].y = lCurrentPosition_obj.y;
					}
					return;
				}
			}
			else
			{
				return;
			}
		}
	}

	/**
	 * Updates trajectory for Jumper enemy;
	 * New points should be added, to force enemy stay, when he prepares to jump and land.
	 *
	 * @param {Boolean} aIsFirstPointTurn_bln - Tells if enemy should turn on the first point.
	 * @default false
	 * @param {Int} aSkipPoints_num - Amount of first points to keep as they are.
	 * @default 1
	 */
	_jumperTrajectoryUpdate(aIsFirstPointTurn_bln = false, aSkipPoints_num = 1)
	{
		let lPoints_arr = this.trajectory.points;
		let lNewPoints_arr = [];

		// Add first point as it is (or more, if skip is needed).
		for (let i = 0; i < aSkipPoints_num; ++i)
		{
			let lTurns_num = this._getTurnsCount(lPoints_arr[i-1],lPoints_arr[i],lPoints_arr[i+1]);
			let lIsTurn_bln = Boolean((aIsFirstPointTurn_bln && (i == 0)) || !!lTurns_num);

			let x0 = lPoints_arr[i].x;
			let y0 = lPoints_arr[i].y;
			let t0 = lPoints_arr[i].time;
			lNewPoints_arr.push({x: x0, y: y0, time: t0, end: true, turn: lIsTurn_bln});
		}

		// Go through pairs of points. Standart enemies walk between them and turns in point.
		let lStartIndex_int = aSkipPoints_num - 1;
		for (let k = lStartIndex_int; k < lPoints_arr.length - 1 /* Skip last point */; ++k)
		{
			let lPoint1_obj = lPoints_arr[k];
			let lPoint2_obj = lPoints_arr[k+1];

			let lMaxDistance_num = this._maxJumpDistance;
			let lRealDistance_num = this._getDistance(lPoint1_obj, lPoint2_obj);
			// Server distance: 17-22 ceils. One ceil = 11.45px. Client distance: 192-240px. New Jumps should always be 1;
			let lNewJumps_num = Math.ceil(lRealDistance_num / lMaxDistance_num);//[Y]TODO here is always 1 jump
			
			let lTurns_num = this._getTurnsCount(lPoints_arr[k-1],lPoints_arr[k],lPoints_arr[k+1]);
			// If distance is too short, no need to stop. Turn animations and slipping would close that pass, animation just be slowed down.
			if (lNewJumps_num == 0)
			{
				//console.log("[Y] SLIDE!");
				let x0 = lPoint2_obj.x;
				let y0 = lPoint2_obj.y;
				let t0 = lPoint2_obj.time;
				lNewPoints_arr.push({x: x0, y: y0, time: t0, turn: !!lTurns_num, end: true, short: true});
			}
			else
			{
				// tk, xk, yk - start points. td, xd, yd - one step.
				let tk = lPoint1_obj.time;
				let td = (lPoint2_obj.time - tk) / lNewJumps_num;
				let xk = lPoint1_obj.x;
				let xd = (lPoint2_obj.x - xk) / lNewJumps_num;
				let yk = lPoint1_obj.y;
				let yd = (lPoint2_obj.y - yk) / lNewJumps_num;

				// Percents of animation states in timeline.
				// Calculated from animation timing in '_jumperTickHandler' method with little deviations, picked up on eye.
				//[Y]TODO turnes amount > 1
				let lPreparePercents_num = this._prepareToJumpPercents;
				let lLandingPercents_num = this._landingPercents;
				let lJumpPercents_num = 1 - lLandingPercents_num;

				// Generate new points in current distance.
				for (let i = 0; i < lNewJumps_num; ++i)
				{
					// There will be turn in last point, additional time will be needed for that path.
					let lIsTurn_bln = Boolean(i === lNewJumps_num-1) && !!lTurns_num;

					// This steps needed to start turning before enemy start walk.
					let lMicroStepX_num = (i===0 ? Math.sign(xd) : 0);
					let lMicroStepY_num = (i===0 ? Math.sign(yd) : 0);

					// Wait point for preparing to jump.
					let x0 = xk + xd*(i) + lMicroStepX_num;
					let y0 = yk + yd*(i) + lMicroStepY_num;
					let t0 = tk + td*(i+lPreparePercents_num);

					// Jump point.
					let x1 = xk + xd*(i+1);
					let y1 = yk + yd*(i+1);
					let t1 = tk + td*(i+lJumpPercents_num);

					// Wait point for landing.
					let x2 = xk + xd*(i+1);
					let y2 = yk + yd*(i+1);
					let t2 = tk + td*(i+1);

					lNewPoints_arr.push({x: x0, y: y0, time: t0});
					lNewPoints_arr.push({x: x1, y: y1, time: t1});
					lNewPoints_arr.push({x: x2, y: y2, time: t2, turn: lIsTurn_bln, end: true});
				}
			}
		}

		// Update points. this.trajectory links to GameScreen enemy trajectory, so we change it both here.
		this.trajectory.points = lNewPoints_arr;
	}

	/**
	 * Calculates count of turns between two paths.
	 * @param {Object} aPointPrev_obj - previous path point
	 * @param {Object} aPointCurrent_obj - current path point
	 * @param {Object} aPointNext_obj - next path point
	 */
	_getTurnsCount(aPointPrev_obj, aPointCurrent_obj, aPointNext_obj)
	{
		let lCount_num = 0;

		if (!aPointPrev_obj || !aPointCurrent_obj || !aPointNext_obj) return 0;

		let lPrevAngle_num = this._getNormalizedAngle(aPointPrev_obj, aPointCurrent_obj);
		let lPrevDirection_str = Enemy.getDirection(lPrevAngle_num);

		let lNextAngle_num = this._getNormalizedAngle(aPointCurrent_obj, aPointNext_obj);
		let lNextDirection_str = Enemy.getDirection(lNextAngle_num);

		if (lPrevDirection_str != lNextDirection_str)
		{
			lCount_num = 1;

			switch (lPrevDirection_str)
			{
				case DIRECTION.RIGHT_DOWN:
					if (lNextDirection_str == DIRECTION.LEFT_UP) lCount_num = 2;
				break;
				case DIRECTION.LEFT_DOWN:
					if (lNextDirection_str == DIRECTION.RIGHT_UP) lCount_num = 2;
				break;
				case DIRECTION.LEFT_UP:
					if (lNextDirection_str == DIRECTION.RIGHT_DOWN) lCount_num = 2;
				break;
				case DIRECTION.RIGHT_UP:
					if (lNextDirection_str == DIRECTION.LEFT_DOWN) lCount_num = 2;
				break;
			}
		}

		return lCount_num;
	}

	_getNormalizedAngle(p1, p2)
	{
		return GameScreen.normalizeAngle(Math.atan2(p2.y - p1.y, p2.x - p1.x));
	}

	_getDistance(p1, p2)
	{
		let aa = p2.x-p1.x;
		let bb = p2.y-p1.y;
		return Math.sqrt(aa*aa+bb*bb);
	}

	/**
	 * Updates spine animation speed for current distance.
	 */
	_jumperViewUpdateHandler()
	{
		let lPoints_arr = this.trajectory.points;
		let lPrevTurnPointIndex_num = 0;

		for (; lPrevTurnPointIndex_num < lPoints_arr.length; ++lPrevTurnPointIndex_num)
		{
			let lPoint_pt = lPoints_arr[lPrevTurnPointIndex_num];
			if (Utils.isEqualTrajectoryPoints(lPoint_pt, this.prevTurnPoint)) break;
			//if (lPoint_pt == this.prevTurnPoint) break;
		}

		// Calculate special points with 'end' flag for current distance.
		let lPrevPoint_obj = this._getPreviousPoint(lPrevTurnPointIndex_num);
		let lNextPoint_obj = this._getNextPoint(lPrevTurnPointIndex_num+1);

		// If we already calculated speed for this distance or there are no points, skip it.
		if (lPrevPoint_obj && lNextPoint_obj && !Utils.isEqualTrajectoryPoints(this._fLastJumperPos_obj, lPrevPoint_obj))
		{
			this._fLastJumperPos_obj = lPrevPoint_obj;

			if (this.state === STATE_WALK)
			{
				if (!this._isAppearingInProgress)
				{
					//play animation
					this.changeTextures(STATE_WALK);
				}
			}
		}
	}

	_updateSpineSpeed()
	{
		this.spineSpeed = this.getSpineSpeed();
		this.spineView && (this.spineView.view.state.timeScale = this.spineSpeed);
	}

	/**
	 * Returns previous point on timeline with special flag 'end'.
	 *
	 * @param {int} index - index of previous position (point in that index could be without flag)
	 */
	_getPreviousPoint(index)
	{
		let lPoints_arr = this.trajectory.points;
		for (let i = index; i >= 0; --i)
		{
			if (lPoints_arr[i] && lPoints_arr[i].end) return lPoints_arr[i];
		}

		return null;
	}

	/**
	 * Returns next point on timeline with special flag 'end'.
	 *
	 * @param {int} index - index of next position (point in that index could be without flag)
	 */
	_getNextPoint(index)
	{
		let lPoints_arr = this.trajectory.points;
		for (let i = index; i < lPoints_arr.length; ++i)
		{
			if (lPoints_arr[i] && lPoints_arr[i].end) return lPoints_arr[i];
		}

		return null;
	}

	/**
	 * Changes speed to default for turn animation and restores to appropriate speed for jump, when animation ends.
	 */
	_changeSpeedToDefaultOnTurn()
	{
		this._updateSpineSpeed();

		this.spineView && (this.spineView.view.state.onComplete = (()=>this._updateSpineSpeed()));
	}

	/**
	 * Calculate points for all current distance. From previous to next 'end' point.
	 */
	_getCurrentPathPoints()
	{
		let lPoints_arr = this.trajectory.points;
		let lPathPoints_arr = [];
		let lPrevTurnPointIndex_num = 0;

		for (; lPrevTurnPointIndex_num < lPoints_arr.length; ++lPrevTurnPointIndex_num)
		{
			let lPoint_pt = lPoints_arr[lPrevTurnPointIndex_num];
			if (lPoint_pt == this.prevTurnPoint) break;
			if (Utils.isEqualTrajectoryPoints(lPoint_pt, this.prevTurnPoint)) break;
		}

		if (lPrevTurnPointIndex_num === lPoints_arr.length)
		{
			lPrevTurnPointIndex_num = lPoints_arr.length - 1;
			lPrevTurnPointIndex_num = 0;
		}
		else
		{
			for (; lPrevTurnPointIndex_num >= 0; --lPrevTurnPointIndex_num)
			{
				if (lPoints_arr[lPrevTurnPointIndex_num].end) break;
			}
		}
		// Remember points from the previous 'end' to the next one.
		if (lPoints_arr[lPrevTurnPointIndex_num] && lPoints_arr[lPrevTurnPointIndex_num].time)
		{
			lPathPoints_arr.push(Object.assign({}, lPoints_arr[lPrevTurnPointIndex_num]));
		}
		for (let i = lPrevTurnPointIndex_num+1; i < lPoints_arr.length; ++i)
		{
			lPathPoints_arr.push(Object.assign({}, lPoints_arr[i]));
			if (lPoints_arr[i].end)
			{
				break;
			}
		}

		return lPathPoints_arr;
	}

	/**
	 * Handler for updated trajectory, sended from server.
	 *
	 * @param {Array} aCurrentPathPoints_arr - Points array for current Jumper path. Should be remembered before trajectory update.
	 */
	_handleUpdatedTrajectory(aCurrentPathPoints_arr)
	{
		let lIsFirstPointTurn_bln = true;
		let lPointsToSkip_num = 1;

		this.trajectory.points = this.removePointsDuplicates(this.trajectory.points);

		if (this._fIsFrozen_bl) // If enemy been frozen by Cryo Gun.
		{
			let lLastPointInCurrentPath_pt = this._fBeforeFreezeCurrentPath_pt_arr[3];
			let lFirstNewPoint_pt = this.trajectory.points[0];

			if (!lLastPointInCurrentPath_pt || !this.trajectory.points[1])
			{
				//Nothing to do
			}
			else if (Utils.isEqualPoints(lLastPointInCurrentPath_pt, lFirstNewPoint_pt)) // Jumper was frozen exactly in a turn Point
			{
				this.trajectory.points.shift(); // Remove first frozen point
			}
			else
			{
				let lFrozenPoints_pt_arr = this.trajectory.points.splice(0, 2);
				let lCurrentPathPoints_pt_arr = Utils.cloneArray(this._fBeforeFreezeCurrentPath_pt_arr);
				let lFrozenTime_num = lFrozenPoints_pt_arr[1].time - lFrozenPoints_pt_arr[0].time;
				lCurrentPathPoints_pt_arr[0].time = lCurrentPathPoints_pt_arr[0].time + lFrozenTime_num;
				lCurrentPathPoints_pt_arr[3].time = lCurrentPathPoints_pt_arr[3].time + lFrozenTime_num;
				this.trajectory.points.unshift(lCurrentPathPoints_pt_arr[0], lCurrentPathPoints_pt_arr[3]);

				if (this._fFirstFreezePoints_arr && this._fFirstFreezePoints_arr[2] && this.trajectory.points[2])
				{
					let lExtraFreezeTime_num = this.trajectory.points[2].time - this._fFirstFreezePoints_arr[2].time;
					this.trajectory.points[0].time += lExtraFreezeTime_num;
					this.trajectory.points[1].time += lExtraFreezeTime_num;
				}
			}

			if (!this._fFirstFreezePoints_arr)
			{
				this._fFirstFreezePoints_arr = Utils.cloneArray(this.trajectory.points);
			}
		}
		else // If enemy leaving field.
		{
			lPointsToSkip_num = 1;
			lIsFirstPointTurn_bln = aCurrentPathPoints_arr[0] ? aCurrentPathPoints_arr[0].turn : false;
		}

		this._jumperTrajectoryUpdate(lIsFirstPointTurn_bln, lPointsToSkip_num); // Updating new trajectory points.
	}

	removePointsDuplicates(points)
	{
		for (let i = 0; i < points.length - 1; ++i)
		{
			if (points[i].time == points[i+1].time)
			{
				points.splice(i+1, 1);
				--i;
			}
		}
		return points;
	}

	//OVERRIDE...
	updateTrajectory(aTrajectory_obj)
	{
		// Remember trajectory before update. It should be restored partially.
		let lPathPoints_arr = this._getCurrentPathPoints();

		super.updateTrajectory(aTrajectory_obj);
		this._handleUpdatedTrajectory(lPathPoints_arr);
	}

	_freeze(aIsAnimated_bl = true)
	{
		super._freeze(aIsAnimated_bl);
		if (!this._fBeforeFreezeCurrentPath_pt_arr)
		{
			this._fBeforeFreezeCurrentPath_pt_arr = this._getCurrentPathPoints();
		}
	}

	//override
	_addFreezeCover()
	{
		let lMaskPos_pt = this._fFreezeMask_sprt.position;

		let lFreezeCover_sprt = new Sprite();

		for (let i = -1; i < 2; i++)
		{
			for (let j = -1; j < 2; j++)
			{
				let lFreezing_sprt = new PIXI.heaven.Sprite(APP.library.getSprite('weapons/Cryogun/Freeze').textures[0]);
				lFreezing_sprt.anchor.set(0.2, 0.2);
				lFreezing_sprt.zIndex = this._fFreezeMask_sprt.zIndex+1;
				lFreezing_sprt.maskSprite = this._fFreezeMask_sprt;
				lFreezing_sprt.pluginName = 'batchMasked';
				lFreezeCover_sprt.addChild(lFreezing_sprt);
				lFreezing_sprt.position.set(160 * i, 160 * j);
			}
		}

		lFreezeCover_sprt.position.set(lMaskPos_pt.x, lMaskPos_pt.y);

		this._fFreezeBaseContainer_sprt.addChild(lFreezeCover_sprt);
		this._fFreezeCover_sprt = lFreezeCover_sprt;
		this._fFreezeMask_sprt.alpha = 0.24;
	}

	_unfreeze(aIsAnimated_bl = true)
	{
		super._unfreeze(aIsAnimated_bl);
		this._fBeforeFreezeCurrentPath_pt_arr = null;
		this._fFirstFreezePoints_arr = null;
		this._updateSpineSpeed();
	}

	showBombBounce(angle, dist, mapZonePoints)
	{
		// Skip bounce for Jumper to avoid bugs with it's trajectory.
	}

	_resumeSpineAnimationAfterUnfreeze()
	{
		if (this.isTurnState)
		{
			super._resumeSpineAnimationAfterUnfreeze();
		}
		else
		{
			this.setWalk();
		}
	}

	getSpineSpeed()
	{
		if (this.isTurnState)
		{
			return 1.2;
		}
		else
		{
			return JUMPER_DEFAULT_SPEED;
		}
	}

	get _deathFxOffset()
	{
		return {x: 0, y: -this._fJumpOffsetY_num};
	}

	changeView()
	{
		// Update spine animation speed before we change view (turn, move etc.).
		this._jumperViewUpdateHandler();

		super.changeView();
	}

	changeTextures(type, noChangeFrame, switchView, checkBackDirection)
	{
		super.changeTextures(type, noChangeFrame, switchView, checkBackDirection);

		if (type == STATE_TURN)
		{
			this._changeSpeedToDefaultOnTurn();

			// Calculate turn default speed. Don't forget to comment this._changeSpeedToDefaultOnTurn() !
			// Should be calculated with default spine speed.
			// let lTurnStart_num = APP.gameScreen.currentTime;
			// this.spineView.view.state.onComplete = (()=>{console.log("{Jumper} Turn time:", APP.gameScreen.currentTime - lTurnStart_num)});
		}

		// Calculate turn default speed. Don't forget to comment this._changeSpeedToDefaultOnTurn() !
		// Should be calculated with default spine speed.
		if (type == STATE_WALK)
		{
			// let lWalkStart_num = APP.gameScreen.currentTime;
			// this.spineView.view.state.onComplete = (()=>{console.log("{Jumper} Walk time:", APP.gameScreen.currentTime - lWalkStart_num)});
		}
	}


	_getHitRectWidth()
	{
		return 80;
	}

	_getHitRectHeight()
	{
		return 140*0.85;
	}

	getLocalCenterOffset()
	{
		if (!this._fLocalCenterOffset_obj)
		{
			this._fLocalCenterOffset_obj = this._resetLocalCenterOffset();
		}
		switch (this.state)
		{
			case STATE_WALK:
				this._fLocalCenterOffset_obj = this._calcLocalCenterOffset();
				break;
			case STATE_TURN:
				this._fLocalCenterOffset_obj = this._resetLocalCenterOffset();
				break;
		}
		this._fLocalCenterOffset_obj.x *= 0.85;
		this._fLocalCenterOffset_obj.y *= 0.85;
		return this._fLocalCenterOffset_obj;
	}

	_resetLocalCenterOffset()
	{
		let lIsXOffsetNeeded_bln = Boolean(this.direction == DIRECTION.RIGHT_DOWN);
		let lYOffset_num = -65;

		return {x: lIsXOffsetNeeded_bln ? 10 : 0, y: lYOffset_num};
	}

	_calcLocalCenterOffset()
	{
		//update current offset point
		let lIsXOffsetNeeded_bln = Boolean(this.direction == DIRECTION.RIGHT_DOWN);
		let lYOffset_num = -65;

		return {x: lIsXOffsetNeeded_bln ? 10 : 0, y: lYOffset_num};
	}

	tick(delta)
	{
		super.tick(delta);

		if (!this._isAppearingInProgress)
		{
			this._jumperTickHandler();
		}
		else
		{
		}
	}

	getTurnSuffix()
	{
		let lFinalDirection_str = this.direction;
		let lTurnDirection_str = this.turnDirection;

		let lDirections_str_arr = [	DIRECTION.LEFT_DOWN,
									DIRECTION.RIGHT_DOWN,
									DIRECTION.RIGHT_UP,
									DIRECTION.LEFT_UP
									];

		let lFinalDirectionIndex_int = lDirections_str_arr.indexOf(lFinalDirection_str);

		let j = lTurnDirection_str == TURN_DIRECTION.CCW ? -1 : 1;
		let lPreviousDirection_ind = (lFinalDirectionIndex_int + j) % lDirections_str_arr.length;
		if (lPreviousDirection_ind < 0)
		{
			lPreviousDirection_ind = lDirections_str_arr.length + lPreviousDirection_ind;
		}
		let lPreviousDirection_str = lDirections_str_arr[lPreviousDirection_ind];

		return lPreviousDirection_str.substr(3);
	}

	_updateAppearanceAnimation(aAnimationScale_num = 1)
	{
		super._updateAppearanceAnimation(this._animationTimeScale);
	}
	//...OVERRIDE

	/**
	 * Updates Payout value animation to make it follow with enemy's jumps.
	 */
	_jumperTickHandler()
	{
		if (!this.spineView) return;

		if (this.state == STATE_STAY || this.state == STATE_DEATH)
		{
			return;
		}

		if (!this.spineView || !this.spineView.view || !this.spineView.view.state || !this.spineView.view.state.tracks || !this.spineView.view.state.tracks[0]) return;


		if (this.state === STATE_WALK)
		{
			this.spineView.stop();

			// Watch for animation ranges:
			let lAnimationTiming_num = this.spineView.view.state.tracks[0].animationLast;
			let lCurrentPathPoints_pt_arr = this._getCurrentPathPoints();
			if (lCurrentPathPoints_pt_arr.length < 4)
				return; //[Y]TODO to investigate why for the first time this array consists of only 1 point
			let lPrevPoint_pt = lCurrentPathPoints_pt_arr[0];
			let lNextPoint_pt = lCurrentPathPoints_pt_arr[3];
			let lTotalTimeDiff_num = lNextPoint_pt.time - lPrevPoint_pt.time;
			let lCurrentTimeDiff_num = APP.currentWindow.currentTime - lPrevPoint_pt.time;
			let lPer_num = Math.min(1, lCurrentTimeDiff_num/lTotalTimeDiff_num);
	
			this.spineView.view.state.tracks[0].trackTime = 0;
			
			let lNewPos_num = lPer_num * this._animationTimeScale;
			this.spineView.updatePosition(lNewPos_num);

			this._validateJumpFinish(lPer_num);
		}
	}

	_validateJumpFinish(aPer_num)
	{
		let JUMPER_PERCENTAGE_OF_ANIMATION_DURING_WHICH_THE_JUMP_ENDS = {start: (1-this._landingPercents)-0.01, finish: (1-this._landingPercents)+0.01}

		if (
				aPer_num > JUMPER_PERCENTAGE_OF_ANIMATION_DURING_WHICH_THE_JUMP_ENDS.start &&
				aPer_num < JUMPER_PERCENTAGE_OF_ANIMATION_DURING_WHICH_THE_JUMP_ENDS.finish &&
				!this._fLandingActionsCompleted_bl
			)
		{
			this._onJumpFinish();
			this._fLandingActionsCompleted_bl = true;
		}
		else if (aPer_num <= JUMPER_PERCENTAGE_OF_ANIMATION_DURING_WHICH_THE_JUMP_ENDS.start)
		{
			this._resetLandingActionsCompletion();
		}
	}

	_resetLandingActionsCompletion()
	{
		this._fLandingActionsCompleted_bl = false;
	}

	_onJumpFinish()
	{
		this._fLandingCounter_int++;
	}

	get _isJumpState()
	{
		if (this.state !== STATE_WALK) return false;

		let lCurrentPathPoints_pt_arr = this._getCurrentPathPoints();
		if (lCurrentPathPoints_pt_arr.length < 4) return false;

		let lPrevPoint_pt = lCurrentPathPoints_pt_arr[0];
		let lNextPoint_pt = lCurrentPathPoints_pt_arr[3];
		let lPer_num = (APP.currentWindow.currentTime - lPrevPoint_pt.time) / (lNextPoint_pt.time - lPrevPoint_pt.time);

		let jumpSettings = this._jumpSettings;
		let lStart_num = jumpSettings.Start;
		let lEnd_num = jumpSettings.End;

		if (lPer_num >= lStart_num && lPer_num <= lEnd_num)
		{
			return true;
		}

		return false;
	}

	changeZindex()
	{
		super.changeZindex();

		if(this._isJumpState)this.zIndex -= this.jumpOffset;
	}

	changeShadowPosition()
	{
		this.shadow.position.set(0, -this._fJumpOffsetY_num);
		this.shadow.scale.set(1);
	}

	get jumpOffset()
	{
		if (this.state !== STATE_STAY)
		{
			this._calculateHeightOfJump();
		}

		return this._fJumpOffsetY_num;
	}

	_calculateHeightOfJump()
	{
		this._fJumpOffsetY_num = 0;
		if (this.state !== STATE_WALK) return;

		let lCurrentPathPoints_pt_arr = this._getCurrentPathPoints();
		if (lCurrentPathPoints_pt_arr.length < 4) return;
		let lPrevPoint_pt = lCurrentPathPoints_pt_arr[0];
		let lNextPoint_pt = lCurrentPathPoints_pt_arr[3];
		let lPer_num = (APP.currentWindow.currentTime - lPrevPoint_pt.time) / (lNextPoint_pt.time - lPrevPoint_pt.time);

		let jumpSettings = this._jumpSettings;
		let lStart_num = jumpSettings.Start;
		let lEnd_num = jumpSettings.End;
		let lDistPercent_num = this._getDistance(lPrevPoint_pt, lNextPoint_pt) / this._maxJumpDistance;
		let lMaxHeight_num = jumpSettings.Height * lDistPercent_num;

		let lShiftY_num = 0;

		if (lPer_num >= lStart_num && lPer_num <= lEnd_num)
		{
			let lHeightPercent_num = this._calcHeightShiftPercent(lPer_num, lStart_num, lEnd_num);

			lHeightPercent_num = Math.sqrt(lHeightPercent_num);

			lShiftY_num = - lMaxHeight_num * lHeightPercent_num;
		}

		this._fJumpOffsetY_num = lShiftY_num;
	}

	_calcHeightShiftPercent(aCurAnimPercent_num, aStartJumpPercent_num, aEndJumpPercent_num)
	{
		let curJumpPercent = (aCurAnimPercent_num - aStartJumpPercent_num) / (aEndJumpPercent_num - aStartJumpPercent_num);
		let jumpTopPercent = 0.32;
		
		if (curJumpPercent <= jumpTopPercent)
		{
			return curJumpPercent/jumpTopPercent;
		}
		else
		{
			return 1-(curJumpPercent-jumpTopPercent)/(1-jumpTopPercent);
		}
	}

	_calculateAnimationLoop(stateType)
	{
		// let animationLoop = true;
		// switch (stateType)
		// {
		// 	case STATE_TURN:
		// 	case STATE_WALK:
		// 		animationLoop = false;
		// 		break;
		// }
		return true;
	}

	destroy()
	{
		super.destroy();

		this._fAllowUpdatePosition_bln = undefined;
		this._fBeforeFreezeCurrentPath_pt_arr = undefined;
		this._fLocalCenterOffset_obj = null;
		this._fIsStartAnimationPlayed_bl = undefined;
		this._fLandingActionsCompleted_bl = undefined;
		this._fJumpCounter_num = undefined;
		this._fNextJumpCoundCount_num = undefined;

		this._fJumpOffsetY_num = null;
	}
}

export default JumperEnemy;