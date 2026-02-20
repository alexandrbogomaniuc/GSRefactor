import Enemy, { STATE_GROW, STATE_IDLE } from './Enemy';
import { STATE_WALK, STATE_IMPACT, STATE_DEATH, STATE_TURN, STATE_STAY, SPINE_SCALE, DIRECTION, TURN_DIRECTION } from './Enemy';
import { ENEMIES } from '../../../../shared/src/CommonConstants';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import SpineEnemyShadow from './shadows/SpineEnemyShadow';

export const FOOT_STEPS_POSITIONS =
{
};

const MIN_TURN_SPEED = 0.8;

class SpineEnemy extends Enemy
{
	static get EVENT_STATE_CHANGED ()			{ return "onEnemyStateChanged" };
	static get EVENT_ON_FOOT_STEP_OCCURRED ()	{ return "EVENT_ON_FOOT_STEP_OCCURRED" };
	static get EVENT_ON_ENEMY_START_DYING ()	{ return Enemy.EVENT_ON_ENEMY_START_DYING }

	constructor(params)
	{
		super(params);
		this._fCurrentAnimationName_str = undefined;
		this._fGlowFX_sprts = null;
		this._fFreezeGround2_sprt = null;
		this._fFreezeGround3_sprt = null;
	}

	get currentAnimationName()
	{
		return this._fCurrentAnimationName_str;
	}

	get minTurnSpeed()
	{
		return MIN_TURN_SPEED;
	}

	//override...
	getSpineSpeed()
	{
		let lSpeed_num = 0.2;
		switch (this.name)
		{
			case ENEMIES.Jaguar:
				lSpeed_num = 0.16;
				break;
			case ENEMIES.Skullbreaker:
			case ENEMIES.SkullboneOrb:
				lSpeed_num = 0.27;	//0.27 * 1.5;
				break;
			case ENEMIES.Wasp:
			case ENEMIES.YellowWasp:
			case ENEMIES.VioletWasp:
			case ENEMIES.Firefly:
				lSpeed_num = 0.11;
				break;
			case ENEMIES.SnakeStraight:
				lSpeed_num = this.randomizedSpineSpeed;
				break;
			case ENEMIES.RedAnt:
			case ENEMIES.BlackAnt:
				lSpeed_num = 0.15;
				break;
			case ENEMIES.Scorpion:
				lSpeed_num = 0.2;
				break;
		}

		let lSpineSpeed_num = lSpeed_num * this.currentTrajectorySpeed;

		return lSpineSpeed_num;
	}

	get randomizedSpineSpeed() // for snakes only
	{
		return this._randomizedSpineSpeed ? this._randomizedSpineSpeed : this._randomizedSpineSpeed = Math.random() * 0.07 + 0.07;
	}

	setViewPos()
	{
		let pos = {x: 0, y: 0};
		this.viewPos = pos;
	}

	_getSpineViewOffset()
	{
		let pos = {x: 0, y: 0};
		switch (this.name)
		{
			case ENEMIES.Jaguar:
				pos = {x: 5, y: 20};
				break;
			case ENEMIES.Skullbreaker:
				pos = {x: 0, y: 10};
				break;
		}
		return pos;
	}

	setSpineViewPos()
	{
		let pos = {x: 0, y: 0};
		this.spineViewPos = pos;
	}

	setStay()
	{
		this.state = STATE_STAY;
		this._stopSpinePlaying();
	}

	calcMeshesHull(optMeshNames)
	{
		if (!this.spineView)
		{
			return null;
		}

		return this.spineView.calcMeshesHull(optMeshNames);
	}

	_onDebugTintUpdated()
	{
		this.tintColor = APP.gameDebuggingController.debugTintColor;
		this.tintIntensity = APP.gameDebuggingController.debugTintIntencity;
		super._onDebugTintUpdated();
	}

	//override protected
	_untint()
	{
		this.spineView && this.spineView.untint();
	}

	//override protected
	_updateTint()
	{
		if (this.tintColor !== undefined)
		{
			this.spineView && this.spineView.tintIt(this.tintColor, this.tintIntensity);
		}
	}

	_initView()
	{
		this.container.addChild(this._generateSpineView(this.imageName + this._calculateSpineSpriteNameSuffix(this.state)));
		this._onSpineViewChanged();
		this._updateTint();

		if (this.tintColor !== undefined)
		{
			if(this.spineView)
			{
				this.spineView.tintIt(this.tintColor, this.tintIntensity);
			}
		}


		this.spineView.scale.set(SPINE_SCALE);

		let lWalkAnimationName_str = this._calculateAnimationName(STATE_WALK);
		this._fCurrentAnimationName_str = lWalkAnimationName_str;

		if(this.spineView)
		{
			this.spineView.setAnimationByName(0, lWalkAnimationName_str, true);
			this._startSpinePlaying();
			this.spineView.view.state.timeScale = this.spineSpeed;
			this.spineView.position.set(
				this.spineViewPos.x,
				this.spineViewPos.y);
			this.spineView.zIndex = 3;
		}

		this.stepTimers = this.getStepTimers();

		this.state = STATE_WALK;
		this._curAnimationState = STATE_WALK;

		this.changeTextures(this.state);

		//DEBUG...
		// this.hitCircle = this.container.addChild(new PIXI.Graphics());
		// let color = 0xff0000;//this.getColor();
		// let alpha = 0.5;
		// this.hitCircle.clear()
		// 	.beginFill(color, alpha)
		// 	.drawCircle(this.footPoint.position.x, this.footPoint.position.y, 5);
		// this.hitCircle.zIndex = 3;
		//...DEBUG
	}

	changeView()
	{
		if (this.checkInitialTurn())
		{
			return;
		}
		let direction = this._calculateDirection();
		if (this._isRotationOnChangeViewRequired(direction))
		{
			this.turnDirection = this.getTurnDirection(direction);
			this.direction = direction;

			if (this.state == STATE_DEATH) return;
			if (this.state == STATE_STAY) return;

			if (this.isFastTurnEnemy)
			{
				this._fastTurn();
			}
			else
			{
				this.changeTextures(STATE_TURN);
				if (this.spineView && this.spineView.view)
				{
					this.spineView.view.state.onComplete = (e) => {
						this._stopSpinePlaying();
						this.endTurn();
					};
				}
				else
				{
					this.setWalk();
				}
			}
		}

		if (this.isWalkState)
		{
			let lSpeedDelta_num = (this.spineView && this.spineView.view) ? Math.abs(this.spineView.view.state.timeScale - this.getSpineSpeed()) : 0;
			//need update speed?
			if (lSpeedDelta_num > 0.01 || this._isSpineFrameSyncRequired)
			{
				this.spineSpeed = this.getSpineSpeed();
				this.setWalk(); //guarantees re-creation of spineView & updating its speed
			}
		}
		else if (this.isStayState)
		{
			this._stopSpinePlaying();
		}
		else
		{
			this._startSpinePlaying();
		}
		this.changeFootPointPosition();
		this.changeShadowPosition();
		this.changeInstaMarkPosition();

		this._fPrevUpdateAccurateTime_num = APP.gameScreen.accurateCurrentTime;
	}

	//override
	_createShadow()
	{
		switch (this.name)
		{
			case ENEMIES.ApeBoss:
			//case ENEMIES.SnakeStraight:
				return new SpineEnemyShadow(this);
			break;
			default:
				return super._createShadow();
		}
	}

	dissolveShadow()
	{
		if (this.shadow)
		{
			this.shadow.fadeTo(0, 100);
			this.shadow.scaleTo(0, 100, null, () => {
				this.shadow && this.shadow.destroy();
			})
		}
	}

	get _isSpineFrameSyncRequired()
	{
		if (!this.isWalkState)
		{
			return false;
		}

		let currentTime = APP.gameScreen.accurateCurrentTime;
		let lPoints = this.trajectory.points;
		let lPrevUpdateTime = this._fPrevUpdateAccurateTime_num;
		let lPrevRealTurnPoint = this._calcPrevRealTurnPoint(currentTime);
		let lPrevRealTurnTime_num = lPrevRealTurnPoint ? lPrevRealTurnPoint.time : undefined;

		if (
				lPrevRealTurnTime_num !== undefined
				&& lPrevUpdateTime !== undefined
				&& lPrevRealTurnTime_num > lPrevUpdateTime
			)
		{
			return true;
		}

		return false;
	}

	_stopSpinePlaying()
	{
		this.spineView && this.spineView.stop();
		if (this.shadow instanceof SpineEnemyShadow)
		{
			this.shadow.i_stopSpinePlaying();
		}
	}

	_startSpinePlaying()
	{
		if(
			this.spineView &&
			this.spineView.view
			)
		{
			this.spineView.play();
		}

		if (this.shadow instanceof SpineEnemyShadow)
		{
			this.shadow.i_startSpinePlaying();
		}
	}

	_fastTurn()
	{
		this.setWalk();
	}

	_isRotationOnChangeViewRequired(targetDirection)
	{
		return (targetDirection != this.direction);
	}

	endTurn(aInitial_bl = false)
	{
		if (this._fIsDeathActivated_bl) // we were waiting for endTurn to proceed with the Death animation
		{
			this.setDeath();
		}
		else
		{
			if (this.isBoss)
			{
				if (aInitial_bl && (this.isFireDenied || !this.trajectoryPositionChangeInitiated))
				{
					return; // stay still while fire is denied or trajectory keeps unchanged point (for boss) ; fireDenied property is set only for Boss
				}
			}

			if (this._isHVRisingUpInProgress)
			{
				return;
			}

			this.setWalk();
		}
	}

	changeTextures(type)
	{
		this.changeSpineView(type);

		if (type == STATE_TURN)
		{
			this.emit("turn");
		}
	}

	changeSpineView(type)
	{
		if (type === undefined)
		{
			throw new Error('SpineEnemy :: changeSpineView >> type = undefined');
		}

		let x = this.spineViewPos.x;
		let y = this.spineViewPos.y;
		let lNewSpineName_str = this.imageName;
		let animationName = '';
		let animationLoop = true;
		let scale = SPINE_SCALE;
		let scaleCoefficient = this.getScaleCoefficient();

		if (type == STATE_DEATH)
		{
			if (this.isBoss)
			{
				this.destroy();
			}
			return;
		}

		if (type != STATE_TURN)
		{
			lNewSpineName_str += this._calculateSpineSpriteNameSuffix(type);
		}

		animationName = this._calculateAnimationName(type);
		animationLoop = this._calculateAnimationLoop(type);
		this._fAnimationName_str = animationName;

		if (type == STATE_TURN)
		{
			lNewSpineName_str = this._calculateSpineTurnName(animationName, this.imageName);
		}

		scale *= scaleCoefficient;

		let prevState = this.state;
		this.state = type;
		if (type !== STATE_STAY)
		{
			this._curAnimationState = type;
		}
		let newState = this.state;

		if (this.spineView)
		{
			if (this.spineView.view && this.spineView.view.state)
			{
				this.spineView.removeAllListeners();
				this.spineView.clearStateListeners();
				this.spineView.view.state.onComplete = null;
				this.stateListener = null;
				if (this.spineView.view.state.tracks && this.spineView.view.state.tracks[0])
				{
					this.spineView.view.state.tracks[0].onComplete = null;
				}
			}

			this._stopSpinePlaying();

			if (lNewSpineName_str !== this._fCurSpineName_str)
			{
				Sequence.destroy(Sequence.findByTarget(this.spineView));
				this.spineView.destroy();
				this.spineView = null;
				this._fCurSpineName_str = undefined;
			}
		}

		this.container.addChild(this.spineView || this._generateSpineView(lNewSpineName_str));

		if (this.tintColor !== undefined)
		{
			if(this.spineView)
			{
				this.spineView.tintIt(this.tintColor, this.tintIntensity);
			}
		}
		this._updateTint();

		if(this.spineView)
		{
			this.spineView.scale.set(scale);
		}

		try
		{
			this._fCurrentAnimationName_str = animationName;
			if ( this.state != STATE_IMPACT) this.spineView.setAnimationByName(0, animationName, animationLoop); // to avoid sliding
		}
		catch (e)
		{
			console.trace("[y] ERROR! Animation not found >>  ANIMATION NAME = " + animationName, ", type = " + type, "this.state = " + this.state);
		}

		this.spineSpeed = this.getSpineSpeed();

		if(this.spineView)
		{
			this.spineView.position.set(x, y);
			this.spineView.zIndex = 3;
			this.spineView.view.state.timeScale = this.isTurnState && this.spineSpeed < this.minTurnSpeed ? this.minTurnSpeed : this.spineSpeed;
		}

		let timers = {
						start: this._calcSpineViewStartTime(type),
						delay: this._calcSpineViewStartDelay(type)
					};


		if(this.spineView)
		{
			if (timers.start > 0)
			{
				if (animationLoop)
				{
					this.spineView.view.state.tracks[0].trackTime = timers.start;
				}
				else
				{
					this.spineView.view.state.tracks[0].animationStart = timers.start;
				}
			}
			this.spineView.view.state.tracks[0].delay = timers.delay;
		}

		if (type === STATE_STAY)
		{
			this._stopSpinePlaying();
		}
		else
		{
			this._startSpinePlaying();
		}

		if (prevState !== newState)
		{
			this.emit(SpineEnemy.EVENT_STATE_CHANGED, {prevState: prevState, newState: newState});
		}

		this.stepTimers = this.getStepTimers();
		if(this.spineView)
		{
			this.spineView.view.state.onComplete = ((e) => { this.stepTimers = this.getStepTimers(); });
		}

		this._onSpineViewChanged();
	}

	_calculateSpineTurnName(aAnimName_str, aSpineName_str)
	{
		let lDirAngles_arr = this._getPossibleDirections();

		let names = "";
		for (let lAngle_num of lDirAngles_arr)
		{
			let lResultSpineName_str = aSpineName_str + lAngle_num;
			let lDataAnimations_obj = APP.spineLibrary.getData(lResultSpineName_str).animations;

			for (let lAnim_obj of lDataAnimations_obj)
			{
				names += lAnim_obj.name + "; "
				if (lAnim_obj.name == aAnimName_str)
				{
					return lResultSpineName_str;
				}
			}
		}

		return 0;
	}

	_getPossibleDirections()
	{
		return [0, 90, 180, 270];
	}

	_calcSpineViewStartTime(type)
	{
		let lStartTime = 0;

		if (type == STATE_WALK)
		{
			let currentTime = APP.gameScreen.accurateCurrentTime;
			let lNextRealTurnTrajectoryPoint = this._calcNextRealTurnPoint(currentTime) || this.trajectory.points[this.trajectory.points.length-1];
			let walkTime = lNextRealTurnTrajectoryPoint.time - currentTime;
			let lTimeScale = this.spineView.view.state.timeScale;

			let lTrack = this.spineView.view.state.tracks[0];
			let lWalkCycleDuration = ~~(lTrack.animationEnd/lTimeScale*1000);

			let lCyclesAmount = Math.floor(walkTime/lWalkCycleDuration);

			lStartTime = (lWalkCycleDuration - (walkTime - lCyclesAmount*lWalkCycleDuration))*lTimeScale;
			lStartTime /= 1000;
		}

		return lStartTime;
	}

	_calcNextRealTurnPoint(aFromTime_num)
	{
		let lPoints = this.trajectory.points;
		for (let i=0; i<lPoints.length; i++)
		{
			let lPoint = lPoints[i];
			if (!!lPoint.isRealTurnPoint && lPoint.time > aFromTime_num)
			{
				return lPoint;
			}
		}

		return null;
	}

	_calcPrevRealTurnPoint(aFromTime_num)
	{
		let lPoints = this.trajectory.points;
		for (let i=lPoints.length-1; i>=0; i--)
		{
			let lPoint = lPoints[i];
			if (!!lPoint.isRealTurnPoint && lPoint.time < aFromTime_num)
			{
				return lPoint;
			}
		}

		return null;
	}

	_calcSpineViewStartDelay(type)
	{
		if (this.name == ENEMIES.Wasp && type == STATE_WALK)
		{
			return Utils.random(0, 200, false)/1000;
		}

		return 0;
	}

	_calculateSpineSpriteNameSuffix(stateType)
	{
		if (stateType == STATE_TURN)
		{
			return this.getTurnSuffix();
		}
		else
		{
			return this.direction.substr(3);
		}
	}

	_calculateAnimationName(stateType)
	{
		let animationName = '';

		switch (stateType)
		{
			case STATE_STAY:
			case STATE_WALK:
			case STATE_IMPACT:
				animationName = this.getWalkAnimationName();
				break;
			case STATE_TURN:
				animationName = this.getTurnAnimationName();
				break;
			case STATE_GROW:
			case STATE_IDLE:
				animationName = stateType;
				break;
		}

		return animationName;
	}

	_calculateAnimationLoop(stateType)
	{
		let animationLoop = true;

		switch (stateType)
		{
			case STATE_TURN:
				animationLoop = false;
				break;
		}

		return animationLoop;
	}

	get curAnimationTrack()
	{
		let animTrack = undefined;
		if (this.spineView && this.spineView.view && this.spineView.view.state && this.spineView.view.state.tracks[0])
		{
			animTrack = this.spineView.view.state.tracks[0];
		}

		return animTrack;
	}

	updateSpineAnimation()
	{
		if (this.spineView && this.spineView.alpha != 0 && this.spineView.view && this.spineView.view.state && this.spineView.view.state.tracks[0])
		{
			let animTime = this.spineView.view.state.tracks[0].animationLast;
			if (this.lastAnimTime && animTime < this.lastAnimTime)
			{
				this.lastAnimTime = 0;
			}
			if (this.stepTimers.length > 0)
			{
				for (let i = 0; i < this.stepTimers.length; i ++)
				{
					if (animTime >= this.stepTimers[i].time && !this.lastAnimTime)
					{
						this._playStepSound();

						if (this.isBoss)
						{
							this.emit(Enemy.EVENT_ON_GROUNDSHAKE);
						}

						this.stepTimers.splice(i, 1);
						--i;

						if (this.stepTimers.length == 0)
						{
							this.lastAnimTime = animTime;
						}

						let curStepId = this._stepsAmount - this.stepTimers.length - 1;
						this._onFootStepOccured(curStepId);
					}
				}
			}
		}
	}

	_onFootStepOccured(curStepId)
	{
		this.emit(SpineEnemy.EVENT_ON_FOOT_STEP_OCCURRED, {stepId:curStepId});
	}

	_playStepSound()
	{
		let soundName = '';
		let soundIndex = 1;
		switch(this.name)
		{
			default:
				return;
			break;
		}

		APP.soundsController.play(soundName);
	}

	getFootStepPosition(stepId)
	{
		let pos = new PIXI.Point(0, 0);

		let lFootStepPositionsDescriptor = this._footStepPositionsDescriptor;
		if (!!lFootStepPositionsDescriptor[this.name])
		{
			let stepDescription = lFootStepPositionsDescriptor[this.name][this.direction][stepId];
			pos = new PIXI.Point(stepDescription.x, stepDescription.y);
		}

		return pos;
	}

	get _footStepPositionsDescriptor()
	{
		return FOOT_STEPS_POSITIONS;
	}

	changeFootPointPosition()
	{
		let x = 0, y = 0;

		this.footPoint.position.set(x, y);

		this._updateHitPointerRectangle();
	}

	getStepTimers()
	{
		let timers = [];

		this._stepsAmount = timers.length;

		return timers;
	}

	//override
	_getHitRectWidth()
	{
		let hitWidth = 0;
		switch (this.name)
		{
			case ENEMIES.Jaguar:
				hitWidth = 110*1.3*1.1;
				break;
			case ENEMIES.Skullbreaker:
			case ENEMIES.SkullboneOrb:
				hitWidth = 70;
				break;
			case ENEMIES.Wasp:
			case ENEMIES.YellowWasp:
			case ENEMIES.VioletWasp:
			case ENEMIES.Firefly:
				hitWidth = 62;
				break;
			case ENEMIES.SnakeStraight:
				hitWidth = 90*2*0.85;
				break;
			case ENEMIES.RedAnt:
			case ENEMIES.BlackAnt:
				hitWidth = 35*2*1.1;
				break;
			case ENEMIES.Scorpion:
				hitWidth = 70;
				break;
			case ENEMIES.Jumper:
				hitWidth = 50;
				break;
		}
		return hitWidth;
	}

	//override
	_getHitRectHeight()
	{
		let hitHeight = 0;
		switch (this.name)
		{
			case ENEMIES.Jaguar:
				hitHeight = 90*1.3*1.1;
				break;
			case ENEMIES.Skullbreaker:
			case ENEMIES.SkullboneOrb:
				hitHeight = 160;
				break;
			case ENEMIES.Wasp:
			case ENEMIES.YellowWasp:
			case ENEMIES.VioletWasp:
			case ENEMIES.Firefly:
				hitHeight = 62;
				break;
			case ENEMIES.SnakeStraight:
				hitHeight = 80*0.85;
				break;
			case ENEMIES.RedAnt:
			case ENEMIES.BlackAnt:
				hitHeight = 35*2*1.1;
				break;
			case ENEMIES.Jumper:
				hitHeight = 80;
				break;
			case ENEMIES.Scorpion:
				hitHeight = 70;
				break;
		}
		return hitHeight;
	}

	//override
	_addFreezeGround(aIsAnimated_bl = true)
	{
		super._addFreezeGround(aIsAnimated_bl);
		if (this.isCritter)
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

			if (!this.isFreezeGroundAvailable) return;

			this._fFreezeGround2_sprt = APP.library.getSprite('weapons/Cryogun/freeze_ground');
			this._fFreezeGround2_sprt.anchor.set(146/283, 82/195);
			this._fFreezeGround2_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
			this._fFreezeGround2_sprt.scale.set(this._getFreezeGroundScaleCoef());
			this.shadow2.addChild(this._fFreezeGround2_sprt);

			this._fFreezeGround3_sprt = APP.library.getSprite('weapons/Cryogun/freeze_ground');
			this._fFreezeGround3_sprt.anchor.set(146/283, 82/195);
			this._fFreezeGround3_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
			this._fFreezeGround3_sprt.scale.set(this._getFreezeGroundScaleCoef());
			this.shadow3.addChild(this._fFreezeGround3_sprt);

			if (aIsAnimated_bl)
			{
				this._fFreezeGround2_sprt.alpha = 0;
				this._fFreezeGround2_sprt.fadeTo(1, 15*2*16.7);
				this._fFreezeGround3_sprt.alpha = 0;
				this._fFreezeGround3_sprt.fadeTo(1, 15*2*16.7);
			}
		}
	}

	//override
	_destroyFrozenSprites()
	{
		super._destroyFrozenSprites();

		if (this._fFreezeGround2_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fFreezeGround2_sprt));
			this._fFreezeGround2_sprt.destroy();
		}
		if ( (this.shadow2) && (this.shadow2.children[1]) )
		{
			Sequence.destroy(Sequence.findByTarget(this.shadow2.children[1]));
			this.shadow2.children[1].destroy();
		}
		this._fFreezeGround2_sprt = null;

		if (this._fFreezeGround3_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fFreezeGround3_sprt));
			this._fFreezeGround3_sprt.destroy();
		}
		if ( (this.shadow3) && (this.shadow3.children[1]) )
		{
			Sequence.destroy(Sequence.findByTarget(this.shadow3.children[1]));
			this.shadow3.children[1].destroy();
		}
		this._fFreezeGround3_sprt = null;
	}

	//override
	getHitRectangle()
	{
		let rect = new PIXI.Rectangle();
		rect.width = 0;
		rect.height = 0;
		if (this.isScarab || this.isLocust)
		{
			return rect;
		}

		rect.width =  10;
		rect.height = Math.abs(80) / (this.isBoss ? 1.2 : 2);
		return rect;
	}

	get crosshairsOffsetPosition()
	{
		let lOffset_pt = {x: 0, y: 0};

		return lOffset_pt;
	}

	setDeath(aIsInstantKill_bl = false, aPlayerWin_obj = null)
	{
		super.setDeath(aIsInstantKill_bl, aPlayerWin_obj);

		this._fIsDeathActivated_bl = true;
		this.stateListener = null;

		this.setDeathFramesAnimation(aIsInstantKill_bl, aPlayerWin_obj);
		this.emit(Enemy.EVENT_ON_ENEMY_START_DYING, {bossName: this.name, enemy: this, isInstantKill: aIsInstantKill_bl});
	}

	setDeathFramesAnimation(aIsInstantKill_bl = false, aPlayerWin_obj = null)
	{
		this._deathInProgress = true;

		if (this.isBoss)
		{
			if (aIsInstantKill_bl)
			{
				this._playDeathFxAnimation(aIsInstantKill_bl);
			}
			else
			{
				this._stopSpinePlaying();
				this._playBossDeathFxAnimation(aPlayerWin_obj);
			}
		}
		else
		{
			if (!aIsInstantKill_bl)
			{
				this.playDeathSound();
			}

			this._forceHVRisingUpIfRequired();

			if ((Enemy.i_isLongDeathAnimationSupported(this.typeId) && !this.isFrozen) || (aPlayerWin_obj && !aPlayerWin_obj.playerWin) || !this._isKilledDeath())
			{
				this._playDeathFxAnimation(aIsInstantKill_bl);
			}
			else
			{
				this._playSimpleEnemyDeathFxAnimation(aIsInstantKill_bl);
			}
		}

		let lEnemyPosition_pt = this.getGlobalPosition();
		lEnemyPosition_pt.x += this.getCurrentFootPointPosition().x;
		lEnemyPosition_pt.y += this.getCurrentFootPointPosition().y;
		this.emit(Enemy.EVENT_ON_DEATH_ANIMATION_STARTED, {position: lEnemyPosition_pt, angle: this.angle});
	}

	_isKilledDeath()
	{
		return this.isDeathActivated && this.deathReason != 1;
	}

	_playSimpleEnemyDeathFxAnimation(aIsInstantKill_bl)
	{
		if (!this.spineView || !this.spineView.view) return;

		this._stopSpinePlaying();

		let l_seq = [
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(0, 5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(0, 5) }], duration: 2 * FRAME_RATE, onfinish: () => {this._playHitHighlightAnimation(3*FRAME_RATE)}},
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(0, 5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(0, 5) }], duration: 2 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(0, 5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(0, 5) }], duration: 2 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(0, 5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(0, 5) }], duration: 2 * FRAME_RATE, onfinish: () => {this._playHitHighlightAnimation(3*FRAME_RATE)}},
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(0, 5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(0, 5) }], duration: 2 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(0, 5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(0, 5) }], duration: 2 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(0, 5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(0, 5) }], duration: 2 * FRAME_RATE, onfinish: () => {this._playHitHighlightAnimation(3*FRAME_RATE)}},
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(0, 5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(0, 5) }], duration: 2 * FRAME_RATE, onfinish: () => {
				this._playDeathFxAnimation(aIsInstantKill_bl);
			}}
		];

		Sequence.start(this.spineView, l_seq);
	}

	changeShadowPosition()
	{
		let x = 0, y = 0, scale = 1, alpha = 1;
		switch (this.name)
		{
			case ENEMIES.Wasp:
			case ENEMIES.YellowWasp:
			case ENEMIES.VioletWasp:
			case ENEMIES.Firefly:
				alpha = 0.5;
				y = 100;
				scale = 0.4;
			break;
			case ENEMIES.RedAnt:
			case ENEMIES.BlackAnt:
				scale = 0.6*1.1;
				alpha = 0.8;
			break;
		}
		this.shadow.position.set(x, y);
		this.shadow.scale.set(scale);
		this.shadow.alpha = alpha;
	}

	getWalkAnimationName()
	{
		return this._calcWalkAnimationName(this.direction);
	}

	_calcWalkAnimationName(aDirection_str, aBaseWalkAnimationName_str = "walk")
	{
		switch (aDirection_str)
		{
			case DIRECTION.LEFT_UP:
				return '270_' + aBaseWalkAnimationName_str;
				break;
			case DIRECTION.LEFT_DOWN:
				return '0_' + aBaseWalkAnimationName_str;
				break;
			case DIRECTION.RIGHT_UP:
				return '180_' + aBaseWalkAnimationName_str;
				break;
			case DIRECTION.RIGHT_DOWN:
				return '90_' + aBaseWalkAnimationName_str;
				break;
		}
		throw new Error (aDirection_str + " is not supported direction.");
	}

	getTurnAnimationName()
	{
		let lDirectionsAngles_int_arr = [0, 90, 180, 270]; //CCW
		let lFinalAngle_num = Number(this.direction.substr(3));
		let lFinalAngleIndex_int = lDirectionsAngles_int_arr.indexOf(lFinalAngle_num);

		let j = this.turnDirection == TURN_DIRECTION.CCW ? -1 : 1;
		let lPreviousAngleIndex_int = (lFinalAngleIndex_int + j) % lDirectionsAngles_int_arr.length;
		if (lPreviousAngleIndex_int < 0)
		{
			lPreviousAngleIndex_int = lDirectionsAngles_int_arr.length + lPreviousAngleIndex_int;
		}
		let lPreviousAngle_num = lDirectionsAngles_int_arr[lPreviousAngleIndex_int];

		return (this.turnPrefix + lPreviousAngle_num + this.turnAnglesSeparator + lFinalAngle_num + this.turnPostfix); //i.e. 270_to_180_turn
	}

	get turnPostfix()
	{
		return "_turn";
	}

	get turnPrefix()
	{
		return "";
	}

	get turnAnglesSeparator()
	{
		return "_to_";
	}

	get curAnimationFrameTime()
	{
		let animTrack = this.curAnimationTrack;
		if (animTrack)
		{
			return animTrack.animationLast;
		}

		return undefined;
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

		if (lTurnDirection_str == TURN_DIRECTION.CW)
		{
			return lPreviousDirection_str.substr(3);
		}
		else if (lTurnDirection_str == TURN_DIRECTION.CCW)
		{
			return lFinalDirection_str.substr(3);
		}
		throw new Error ('Wrong turn direction ' + lTurnDirection_str);
	}

	getLocalCenterOffset()
	{
		let pos = {x: 0, y: 0};
		switch (this.name)
		{
			case ENEMIES.Jaguar:
				switch (this.direction)
				{
					case DIRECTION.LEFT_DOWN:
						pos.x = -10*1.3*1.1;
						pos.y = 10*1.3*1.1 - 40;
						break;
					case DIRECTION.LEFT_UP:
						pos.x = -10*1.3*1.1;
						pos.y = 10*1.3*1.1 - 40;
						break;
					case DIRECTION.RIGHT_DOWN:
						pos.x = 5*1.3*1.1;
						pos.y = 15*1.3*1.1 - 40;
						break;
					case DIRECTION.RIGHT_UP:
						pos.x = 0*1.3*1.1;
						pos.y = 10*1.3*1.1 - 40;
						break;
				}
				break;
			case ENEMIES.Skullbreaker:
			case ENEMIES.SkullboneOrb:
				switch (this.direction)
				{
					case DIRECTION.LEFT_DOWN:
						pos.x = -15;
						pos.y = -70;
						break;
					case DIRECTION.LEFT_UP:
						pos.x = -15;
						pos.y = -70;
						break;
					case DIRECTION.RIGHT_DOWN:
						pos.x = 0;
						pos.y = -70;
						break;
					case DIRECTION.RIGHT_UP:
						pos.x = -5;
						pos.y = -70;
						break;
				}
				break;
			case ENEMIES.Wasp:
			case ENEMIES.YellowWasp:
			case ENEMIES.VioletWasp:
			case ENEMIES.Firefly:
				pos = {x: 0, y: -15};
				break;
			case ENEMIES.SnakeStraight:
				switch (this.direction)
				{
					case DIRECTION.LEFT_DOWN:
						pos.x = 25*0.85;
						pos.y = -25*0.85;
						break;
					case DIRECTION.LEFT_UP:
						pos.x = 25*0.85;
						pos.y = 25*0.85;
						break;
					case DIRECTION.RIGHT_DOWN:
						pos.x = -25*0.85;
						pos.y = -25*0.85;
						break;
					case DIRECTION.RIGHT_UP:
						pos.x = -25*0.85;
						pos.y = 25*0.85;
						break;
				}
				break;
			case ENEMIES.RedAnt:
			case ENEMIES.BlackAnt:
				pos = {x: 0, y: 0};
				break;
			case ENEMIES.Scorpion:
				pos = {x: 0, y: 0};
				break;
			case ENEMIES.Jumper:
				switch (this.direction)
				{
					case DIRECTION.LEFT_DOWN:
						pos.x = 0;
						pos.y = -70;
						break;
					case DIRECTION.LEFT_UP:
						pos.x = 0;
						pos.y = -65;
						break;
					case DIRECTION.RIGHT_DOWN:
						pos.x = 0;
						pos.y = -70;
						break;
					case DIRECTION.RIGHT_UP:
						pos.x = 0;
						pos.y = -65;
						break;
				}
			case ENEMIES.SkullboneRunner:
				switch (this.direction)
				{
					case DIRECTION.LEFT_UP:
						pos.x = 15;
						pos.y = -60;
						break;
					case DIRECTION.RIGHT_DOWN:
						pos.x = 0;
						pos.y = -60;
						break;
					case DIRECTION.RIGHT_UP:
						pos.x = 25;
						pos.y = -60;
						break;
					case DIRECTION.LEFT_DOWN:
						pos.x = -20;
						pos.y = -60;
						break;
				}
				break;
		}
		return pos;
	}

	_onSpineViewChanged()
	{
		//to be overriden
	}

	tick()
	{
		super.tick();

		this.updateShadowSpine();
	}

	updateShadowSpine()
	{
		if (this.shadow instanceof SpineEnemyShadow)
		{
			this.shadow.i_update();
		}
	}

	destroy(purely = false)
	{
		this.stateListener = null;

		this.tintColor = undefined;
		this.tintIntensity = undefined;
		this._fGlowFX_sprts = null;
		this._fFreezeGround2_sprt = null;
		this._fFreezeGround3_sprt = null;
		this._fAnimationName_str = null;

		super.destroy(purely);
	}
}

export default SpineEnemy;