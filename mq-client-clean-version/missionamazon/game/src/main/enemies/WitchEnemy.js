import SpineEnemy from './SpineEnemy';
import Enemy from './Enemy';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { STATE_DEATH, STATE_TURN, DIRECTION, STATE_STAY, STATE_IDLE, SPINE_SCALE } from './Enemy';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { ENEMY_DIRECTION } from '../../config/Constants';

const STATE_TELEPORT_START 	= 'teleport_start';
const STATE_TELEPORT_FINISH = 'teleport_finish';

const START_TELEPORT_DELAY = 14;
const FINISH_TELEPORT_DELAY = 72.2; 
const FINISH_TELEPORT_DELAY_OFFSET = 10;

class WitchEnemy extends SpineEnemy 
{
	static get EVENT_ON_WITCH_TELEPORT()	{ return 'eventOnWitchTeleport'; }

	constructor(aParams_obj)
	{
		super(aParams_obj);

		this._fTimeToChangeDirection_bl = false;
	}

	//override
	_initView()
	{
		this.container.addChild(this._generateSpineView(this.imageName + this.direction.substr(3)));
		this._onSpineViewChanged();
		this.spineView.scale.set(SPINE_SCALE);
		this.spineView.view.state.timeScale = this.spineSpeed;
		this.spineView.position.set(this.spineViewPos.x, this.spineViewPos.y);
		this.spineView.zIndex = 3;
		this.container.alpha = 0; // to appear after first teleportation finish
		this.state = STATE_IDLE;
		this._InitTeleportStart();
		this.changeTextures(this.state);
	}

	//SOUNDS...
	get _teleportOpenSoundAssetName()
	{
		return "teleport_open";
	}

	_playTeleportOpenSound()
	{
		APP.soundsController.play(this._teleportOpenSoundAssetName);
	}

	get _teleportCloseSoundAssetName()
	{
		return "teleport_close";
	}

	_playTeleportCloseSound()
	{
		APP.soundsController.play(this._teleportCloseSoundAssetName);
	}
	//...SOUNDS

	//override
	_invalidateStates()
	{
		this._firstPointTeleportHandler();

		super._invalidateStates();
	}

	//override
	updateTrajectory(aTrajectory_obj)
	{
		super.updateTrajectory(aTrajectory_obj);

		this._firstPointTeleportHandler();
	}
 
	_firstPointTeleportHandler()
	{
		let freezed = false;
		//is freezed
		if (
				Utils.isEqualPoints(this.trajectory.points[0], this.trajectory.points[1])
				&& !this.trajectory.points[0].teleport
				&& !this.trajectory.points[0].invulnerable
				&& !this.trajectory.points[1].invulnerable
				&& !this.trajectory.points[2].teleport
			)
		{
			freezed = true;
			this.trajectory.points.shift();
			this.trajectory.points.shift();

			this.alpha = this.alpha > 0.5 ? 1 : 0; //to show/hide frozen shaman during teleportation
		}

		let lPrevPoint_obj = this._getCurrentTrajectoryPoint(-1);
		let lPrevPointIndexInTrajectory_int = this._getPrevPointIndexInTrajectory(lPrevPoint_obj);
		// for lasthand
		if (this.trajectory.points[lPrevPointIndexInTrajectory_int].teleport)
		{
			this._fIsTeleportingInProgress_bl = true;
			this._fAppearingPoint_obj = this.trajectory.points[lPrevPointIndexInTrajectory_int + 3];
			this._fFinishTeleportingPoint_obj = this.trajectory.points[lPrevPointIndexInTrajectory_int + 5];
		}
		else if (this.trajectory.points[lPrevPointIndexInTrajectory_int].invulnerable && this.trajectory.points[lPrevPointIndexInTrajectory_int + 1].invulnerable && this.trajectory.points[lPrevPointIndexInTrajectory_int + 2] && this.trajectory.points[lPrevPointIndexInTrajectory_int + 2].invulnerable)
		{
			if (!freezed) this.alpha = 0;
			this._fIsTeleportingInProgress_bl = true;
			this._fAppearingPoint_obj = this.trajectory.points[lPrevPointIndexInTrajectory_int + 2];
			this._fFinishTeleportingPoint_obj = this.trajectory.points[lPrevPointIndexInTrajectory_int + 4];
		}
		else if (this.trajectory.points[lPrevPointIndexInTrajectory_int].invulnerable && this.trajectory.points[lPrevPointIndexInTrajectory_int + 1].invulnerable && this.trajectory.points[lPrevPointIndexInTrajectory_int + 2] && Utils.isEqualPoints(this.trajectory.points[lPrevPointIndexInTrajectory_int + 1], this.trajectory.points[lPrevPointIndexInTrajectory_int + 2]))
		{
			if (!freezed) this.alpha = 0;
			this._fIsTeleportingInProgress_bl = true;
			this._fAppearingPoint_obj = this.trajectory.points[lPrevPointIndexInTrajectory_int + 1];
			this._fFinishTeleportingPoint_obj = this.trajectory.points[lPrevPointIndexInTrajectory_int + 3];
		}
		else if (this.trajectory.points[lPrevPointIndexInTrajectory_int].invulnerable && this.trajectory.points[lPrevPointIndexInTrajectory_int + 2] && Utils.isEqualPoints(this.trajectory.points[lPrevPointIndexInTrajectory_int + 1], this.trajectory.points[lPrevPointIndexInTrajectory_int + 2]))
		{
			if (!freezed) this.alpha = 0;
			this._fIsTeleportingInProgress_bl = true;
			this._fAppearingPoint_obj = this.trajectory.points[lPrevPointIndexInTrajectory_int];
			this._fFinishTeleportingPoint_obj = this.trajectory.points[lPrevPointIndexInTrajectory_int + 2];
		}
		else if (Utils.isEqualPoints(this.trajectory.points[lPrevPointIndexInTrajectory_int], this.trajectory.points[lPrevPointIndexInTrajectory_int + 1]) && !this.trajectory.points[lPrevPointIndexInTrajectory_int + 1].teleport && !this.trajectory.points[lPrevPointIndexInTrajectory_int].invulnerable && !this.trajectory.points[lPrevPointIndexInTrajectory_int + 1].invulnerable)
		{
			// do nothing
		}
		else
		{
			this._fIsTeleportingInProgress_bl = false;
			this._fFinishTeleportingPoint_obj = null;
		}
		if (this.state === STATE_IDLE) this.alpha = 1; // to avoid wrong logic hiding
		if (this.state === STATE_STAY) this.container.alpha = 1; // to always show frozen after init hiding
	}

	_getPrevPointIndexInTrajectory(aPrevPoint_obj)
	{
		if (!this.trajectory || !this.trajectory.points || !this.trajectory.points.length) return 0;

		for (let i =  0; i < this.trajectory.points.length; i++)
		{
			if(Utils.isEqualPoints(this.trajectory.points[i], aPrevPoint_obj))
			{
				return i;
			}
		}

		return 0;
	}

	//override
	_calculateAnimationLoop(stateType)
	{
		let animationLoop = true;

		switch(stateType)
		{
			case STATE_DEATH:
			case STATE_TURN:
			case STATE_TELEPORT_START:
			case STATE_TELEPORT_FINISH:
			{
				animationLoop = false;
				break;
			}
		}

		return animationLoop;
	}

	//override
	setDeath(aIsInstantKill_bl = false, aPlayerWin_obj = null)
	{
		Sequence.destroy(Sequence.findByTarget(this));
		this.alpha = 1;
		super.setDeath(aIsInstantKill_bl, aPlayerWin_obj);	
	}

	//override
	_calculateAnimationName(stateType)
	{
		if (stateType === STATE_TELEPORT_START)
		{
			return this._getTeleportStartAnimationName();
		}
		if (stateType === STATE_TELEPORT_FINISH)
		{
			return this._getTeleportFinishAnimationName();
		}
		if (stateType === STATE_IDLE)
		{
			return this._getIdleAnimationName();
		}

		return super._calculateAnimationName(stateType);
	}

	//override
	_calculateDirection()
	{
		if (!this._fTimeToChangeDirection_bl)
		{
			if (
					this.direction === ENEMY_DIRECTION.RIGHT_UP
					|| this.direction === ENEMY_DIRECTION.RIGHT_DOWN
				)
			{
				return ENEMY_DIRECTION.RIGHT_DOWN;
			}

			return ENEMY_DIRECTION.LEFT_DOWN;
		}
		
		this._fTimeToChangeDirection_bl = false;

		if (this.direction === ENEMY_DIRECTION.RIGHT_DOWN)
		{
			return ENEMY_DIRECTION.LEFT_DOWN;
		}

		return ENEMY_DIRECTION.RIGHT_DOWN;
	}

	//override
	changeView()
	{
		this.direction = this._calculateDirection();

		this._checkDenyFire();
		!this._fIsFrozen_bl && this._witchViewUpdateHandler();
	}

	_witchViewUpdateHandler( aAfterTeleportFinished = false )
	{
		if (aAfterTeleportFinished || (this._isTeleportRequired() && !this._fIsTeleportingInProgress_bl && !this._fIsDeathActivated_bl) )
		{
			this._fIsTeleportingInProgress_bl = true;
			
			this.changeTextures(STATE_TELEPORT_START);
			this.container.alpha = 1;

			if (this.alpha === 1)
			{
				this._fTeleportStartAnimationTimer_t && this._fTeleportStartAnimationTimer_t.destructor();
				this._fTeleportStartAnimationTimer_t = new Timer(this._triggerTeleportStartAnimation.bind(this), (START_TELEPORT_DELAY / this.spineView.view.state.timeScale) * FRAME_RATE);
			}
			
			this._fTeleportFinishAnimationTimer_t && this._fTeleportFinishAnimationTimer_t.destructor();
			this._fTeleportFinishAnimationTimer_t = new Timer(this._finishTeleportation.bind(this), (FINISH_TELEPORT_DELAY + FINISH_TELEPORT_DELAY_OFFSET) * FRAME_RATE);
		}
	}

	_checkDenyFire()
	{
		this.isFireDenied = this.alpha !== 1;
	}

	//override
	_freeze(aIsAnimated_bl = true)
	{
		this.alpha = this.alpha > 0.5 ? 1 : 0; //to show/hide frozen shaman during teleportation
		
		super._freeze(aIsAnimated_bl);
		this._pauseAnimation();
	}

	//override
	_unfreeze(aIsAnimated_bl = true)
	{
		super._unfreeze(aIsAnimated_bl);
		this._resumeAnimation();
	}

	//override
	_resumeAfterUnfreeze()
	{
		this._destroyFrozenSprites();

		if (this.spineView && this.spineView.view && this.spineView.view.state)
		{
			this._resumeSpineAnimationAfterUnfreeze();
		}

		if (this.currentBombBounceDelta && this.currentBombBounceDelta.sequence && this.currentBombBounceDelta.sequence.paused)
		{
			this.currentBombBounceDelta.sequence.resume();
		}
	}

	//override
	_resumeSpineAnimationAfterUnfreeze()
	{
		this.state = undefined;

		if (!this._fFreezAfterLasthand_bl)
		{
			this.spineView.removeAllListeners();
			this.stateListener = { complete: () => {
				this._restoreStateBeforeFreeze();
				this.stateListener = null;
			}};
			this.spineView.view.state.addListener(this.stateListener);
		}
		else
		{
			this.setStay();
			this._fFreezAfterLasthand_bl = false;
		}

		this.spineView.play();
	}

	//override
	_restoreStateBeforeFreeze()
	{
		this.changeTextures(STATE_IDLE);
	}

	//override
	setWalk()
	{
		return;
	}

	_pauseAnimation()
	{
		this._fTeleportStartAnimationTimer_t && this._fTeleportStartAnimationTimer_t.pause();
		this._fTeleportFinishAnimationTimer_t && this._fTeleportFinishAnimationTimer_t.pause();

		this._fAlphaSequence_seq && this._fAlphaSequence_seq.pause();

		APP.gameScreen.gameField.pauseTeleportAnimation();
	}

	_resumeAnimation()
	{
		this._fTeleportStartAnimationTimer_t && this._fTeleportStartAnimationTimer_t.resume();
		if (this._fTeleportFinishAnimationTimer_t)
		{
			this._fTeleportFinishAnimationTimer_t.timeout += 75; // to compensate small timelag due to pause and resume
			this._fTeleportFinishAnimationTimer_t.resume();
		} 
		this._fAlphaSequence_seq && this._fAlphaSequence_seq.resume();
		APP.gameScreen.gameField.resumeTeleportAnimation();
	}

	_finishTeleportation()
	{
		Sequence.destroy(Sequence.findByTarget(this));
		
		if (this._fIsDeathActivated_bl) return;

		this._fIsTeleportingInProgress_bl = false;

		this.changeTextures(STATE_TELEPORT_FINISH);
		this.container.alpha = 1;
		this._playTeleportCloseSound();
		let lNextPoint = this._getCurrentTrajectoryPoint(1);
		let lTeleportPosition = this.position;
		if (lNextPoint) // sometimes timers have small lag after freeze (due to pause and resume) and take wrong point to spawn teleport, or spawn it at trajectory end when they should not
		{
			lTeleportPosition = {x: lNextPoint.x, y: lNextPoint.y}; 
			APP.gameScreen.gameField.startTeleportAnimation(this, lTeleportPosition, this.getScaleCoefficient(), this.direction, 'FINISH');
		}
		this.stateListener = { complete: (e) => {
			this.spineView && this.spineView.stop();
			this.changeTextures(STATE_IDLE); // not perfect animation line up, maybe small delay should be implemented
		}};

		if (this.spineView && this.spineView.view.state && this.spineView.view.state)
		{
			this.spineView.view.state.addListener(this.stateListener);
		}

		let lAlpha_seq = [
			{tweens: [{prop: 'alpha', to: 0}]},
			{tweens: [{prop: 'alpha', to: 1}], duration: 2 * FRAME_RATE}
		];

		this._fAlphaSequence_seq = Sequence.start(this, lAlpha_seq);
	}

	_triggerTeleportStartAnimation()
	{
		Sequence.destroy(Sequence.findByTarget(this));
		if (this._fIsDeathActivated_bl) return;

		this._playTeleportOpenSound();
		APP.gameScreen.gameField.startTeleportAnimation(this, this.position, this.getScaleCoefficient(), this.direction, 'START');

		let lAlpha_seq = [
			{tweens: [{prop: 'alpha', to: 1}]},
			{tweens: [],					   duration: 10 * FRAME_RATE},
			{tweens: [{prop: 'alpha', to: 0}], duration: 3 * FRAME_RATE},// onfinish: () => { this.emit(Enemy.EVENT_ON_ENEMY_VIEW_REMOVING) } },
			{tweens: [{prop: '_fTimeToChangeDirection_bl', to: true, onfinish: () => { this.emit(WitchEnemy.EVENT_ON_WITCH_TELEPORT) } }]}
		];

		this._fAlphaSequence_seq = Sequence.start(this, lAlpha_seq);
	}

	_isTeleportRequired()
	{
		let lPrevTrajectoryPoint_obj = this._getCurrentTrajectoryPoint(-1);

		if (lPrevTrajectoryPoint_obj && lPrevTrajectoryPoint_obj.teleport)
		{
			return true;
		}

		return false;
	}

	_getCurrentTrajectoryPoint(aOffset = 0)
	{
		if (!this.trajectory || !this.trajectory.points || !this.trajectory.points.length) return false;

		let lCurrentDate_d = APP.gameScreen.currentTime;
		for (let i = 1; i < this.trajectory.points.length; i++)
		{
			if (this.trajectory.points[i].time >= lCurrentDate_d)
			{
				return this.trajectory.points[i + aOffset];
			}
		}
		return null;
	}

	_InitTeleportStart()
	{
		if (!this.trajectory || !this.trajectory.points || !this.trajectory.points.length) return;
		let lNextPoint = this._getCurrentTrajectoryPoint(1);
		if ( (lNextPoint)&& (!(lNextPoint.invulnerable)) ) return;
		let lReturn = false;
		let lFreezed = false;
		if (
				Utils.isEqualPoints(this.trajectory.points[0], this.trajectory.points[1])
				&& !this.trajectory.points[0].teleport
				&& !this.trajectory.points[0].invulnerable
				&& !this.trajectory.points[1].invulnerable
				&& !this.trajectory.points[2].teleport
			)	
			lFreezed = true; //is freezed
			let lCurrentPoint = this._getCurrentTrajectoryPoint();
			if (lCurrentPoint && lCurrentPoint.invulnerable) 
		{
			//this.container.alpha = 0;
			return;
		}
		else
		{
			let lTimeBeforeTeleport = lNextPoint.time - APP.gameScreen.currentTime;
			let lAnimationTime = (lFreezed)? 3390 : 390;
			if (lTimeBeforeTeleport < lAnimationTime)
			{
				this._fIsTeleportingInProgress_bl = true;
				this.changeTextures(STATE_TELEPORT_START);
				this.container.alpha = 1;
				if (lFreezed)
				{
					if (this.alpha === 1)
					{
						
						this._fTeleportStartAnimationTimer_t && this._fTeleportStartAnimationTimer_t.destructor();
						this._fTeleportStartAnimationTimer_t = new Timer(this._triggerTeleportStartAnimation.bind(this), ((START_TELEPORT_DELAY / this.spineView.view.state.timeScale) * FRAME_RATE) );
					}
				}
				return;
			}
		}
		return;
	}

	_checkTeleportFinishing()
	{
		if (this._fFinishTeleportingPoint_obj && Utils.isEqualTrajectoryPoints(this._fFinishTeleportingPoint_obj, this._getCurrentTrajectoryPoint(-1)))
		{
			this._fFinishTeleportingPoint_obj = null;
			this._fIsTeleportingInProgress_bl = false;
		}

		if (this._fAppearingPoint_obj && Utils.isEqualTrajectoryPoints(this._fAppearingPoint_obj, this._getCurrentTrajectoryPoint(-1)))
		{
			this._fTeleportFinishAnimationTimer_t && this._fTeleportFinishAnimationTimer_t.destructor();
			this._fTeleportFinishAnimationTimer_t = new Timer(this._finishTeleportation.bind(this), FINISH_TELEPORT_DELAY_OFFSET * FRAME_RATE);
			this._fAppearingPoint_obj = null;
		}
	}

	_getTeleportStartAnimationName()
	{
		return this.direction.substr(3) + '_(teleport_A)';
	}

	_getTeleportFinishAnimationName()
	{
		return this.direction.substr(3) + '_(teleport_B)';
	}

	_getIdleAnimationName()
	{
		return this.direction.substr(3) + '_idle';
	}

	//override
	getScaleCoefficient()
	{
		return 1.1;
	}

	//override
	getLocalCenterOffset()
	{
		let pos = {x: 0, y: -50};

		switch (this.direction)
		{
			case DIRECTION.LEFT_DOWN:
				pos.x = -15;
				break;
			case DIRECTION.RIGHT_DOWN:
				pos.x = 5;
				break;
			case DIRECTION.LEFT_UP:
			case DIRECTION.RIGHT_UP:
				pos.x = 0;
				break;
		}

		pos.x *= 1.2;
		pos.y *= 1.2;

		return pos;
	}

	//override
	getSpineSpeed()
	{
		let lSpeed_num  = 0.15;

		return lSpeed_num * this.speed;
	}

	//override
	_getHitRectWidth()
	{
		return 50 * 1.2;
	}

	//override
	_getHitRectHeight()
	{
		return 100 * 1.2;
	}

	//override
	tick(delta)
	{
		super.tick(delta);

		this._checkTeleportFinishing();
	}

	//override
	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this));

		this._fTeleportFinishAnimationTimer_t && this._fTeleportFinishAnimationTimer_t.destructor();
		this._fTeleportFinishAnimationTimer_t = null;

		this._fTeleportStartAnimationTimer_t && this._fTeleportStartAnimationTimer_t.destructor();
		this._fTeleportStartAnimationTimer_t = null;

		this._fIsTeleportingInProgress_bl = null;
		this._fFinishTeleportingPoint_obj = null;
		this._fAppearingPoint_obj = null;
		this._fAlphaSequence_seq = null;

		super.destroy();
	}
}

export default WitchEnemy;