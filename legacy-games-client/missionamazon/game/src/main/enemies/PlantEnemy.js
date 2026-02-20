import { ENEMIES } from '../../../../shared/src/CommonConstants';
import { DIRECTION, SPINE_SCALE, STATE_DEATH, STATE_GROW, STATE_IDLE, STATE_STAY } from './Enemy';
import SpineEnemy from './SpineEnemy';


class PlantEnemy extends SpineEnemy
{
	static get EVENT_ON_ENEMY_START_DYING ()	{ return SpineEnemy.EVENT_ON_ENEMY_START_DYING }

	get isPlantEnemy()
	{
		return true;
	}

	i_playPreDeathAnimation()
	{
		this.isFireDenied = true;
		this._fDeathAnimationsCount_num = 0;
		this.state = this._curAnimationState = STATE_GROW;

		if(!this.spineView)
		{
			return;
		}

		this.changeSpineView(STATE_GROW, true);

		
		this.spineView.on("reverseAnimationCompleted", ()=>{

			if(this.spineView)
			{
				this.spineView.hide();
			}

			if (this._fDeathAnimationsCount_num === 0)
			{
				this.destroy();
			}
		});
		this.spineView.view.state.tracks[0].onComplete = null;
	}

	_initView()
	{
		super._initView();
		this.changeTextures(STATE_IDLE);
		this.__startIdleAnimations();
	}

	setWalk()
	{
		super.setWalk();
		this.changeTextures(STATE_IDLE);
	}

	__onSpawn()
	{
		if(!this.spineView)
		{
			return;
		}

		this.spineView.hide();
		this.__startGrowAnimation();
	}

	__startGrowAnimation()
	{	
		if(!this.spineView)
		{
			return;
		}

		this.spineView.show();
		this.state = this._curAnimationState = STATE_GROW;
		this.changeSpineView(STATE_GROW);
		this._startSpinePlaying();

		this.spineView.view.state.tracks[0].onComplete = this.__changeStateAfterGrowUp.bind(this);
	}

	__changeStateAfterGrowUp()
	{
		if(!this.spineView)
		{
			return;
		}

		this.spineView.view.state.tracks[0].onComplete = null;
		this.state = this._curAnimationState = STATE_IDLE;
		this.changeSpineView(STATE_IDLE);
		this.__startIdleAnimations();
	}

	__startIdleAnimations()
	{
		// to be overridden
	}
	
	get _isAppearingInProgress()
	{
		return this._fIsAppearing_bl;
	}

	//override
	endTurn()
	{
		// nothing to do
	}

	//override
	_calculateAnimationLoop(stateType)
	{
		let animationLoop = true;

		switch (stateType)
		{
			case STATE_GROW:
			case STATE_DEATH:
				animationLoop = false;
				break;
		}

		return animationLoop;
	}

	changeSpineView(type, aBeforeDeathReverse_bl = false)
	{
		if (type === undefined)
		{
			throw new Error('SpineEnemy :: changeSpineView >> type = undefined');
		}

		let animationName = this._calculateAnimationName(type);
		let animationLoop = this._calculateAnimationLoop(type);

		if (type == STATE_DEATH || this._fDeathflag_bl)
		{
			return;
		}
		if (aBeforeDeathReverse_bl) this._fDeathflag_bl = true; // no turning back after death animation started
		let prevState = this.state;
		let newState = this.state = type;
		if (type !== STATE_STAY)
		{
			this._curAnimationState = type;
		}

		!this.spineView && this.container.addChild(this._generateSpineView(this.imageName)); // generate spine view if it is not created yet
		
		if(this.spineView && this.spineView.view)
		{
			this.spineView.scale.set(SPINE_SCALE*this.getScaleCoefficient());
			this.spineView.position.set(this.spineViewPos.x, this.spineViewPos.y);
			this.spineView.zIndex = 3;
			this.spineView.view.state.timeScale = this.spineSpeed = this.getSpineSpeed();
			
			if (this.tintColor !== undefined)
			{
				this.spineView.tintIt(this.tintColor, this.tintIntensity);
			}
			this._updateTint();

			if (this.spineView.hasAnimation(animationName) && animationName !== this._fAnimationName_str)
			{
				this.spineView.setAnimationByName(0, animationName, animationLoop, aBeforeDeathReverse_bl);
				this._fAnimationName_str = animationName;
			}
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

		this._onSpineViewChanged();
	}

	// override
	getSpineSpeed()
	{
		let lSpeed_num = 0;
		switch (this.name) {
			case ENEMIES.CarnivorePlantRed:
			case ENEMIES.CarnivorePlantGreen:
				lSpeed_num = 1.2;
				break;
			case ENEMIES.PoisonPlantMaroonViolet:
			case ENEMIES.PoisonPlantYellowPurple:
				lSpeed_num = 0.75;
				break;
		}

		if (this.state == STATE_GROW)
		{
			lSpeed_num *= 2;
		}

		return lSpeed_num
	}

	//override
	_calcWalkAnimationName(aDirection_str)
	{
		switch (aDirection_str)
		{
			case DIRECTION.LEFT_UP:
			case DIRECTION.LEFT_DOWN:
			case DIRECTION.RIGHT_UP:
			case DIRECTION.RIGHT_DOWN:
				return STATE_IDLE;
		}
		throw new Error (aDirection_str + " is not supported direction.");
	}

	// override
	_calculateSpineSpriteNameSuffix()
	{
		return ''; //cause there is no suffix for plants
	}

	_calcCurrentTrajectorySpeed()
	{
		return this.spineSpeed;
	}

	//override
	changeShadowPosition()
	{
		let x = 0, y = 5, scale = 1.3;

		this.shadow.position.set(x, y);
		this.shadow.scale.set(scale);
	}

	//override
	getLocalCenterOffset()
	{
		let pos = {x: 0, y: -55};
		return pos;
	}

	_unfreeze(aIsAnimated_bl = true)
	{
		super._unfreeze(aIsAnimated_bl);
		this.changeTextures(STATE_IDLE);
	}

}

export default PlantEnemy;