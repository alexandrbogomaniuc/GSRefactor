import BossEnemy from './BossEnemy';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Enemy from './Enemy';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import DeathFxAnimation from '../animation/death/DeathFxAnimation';
import { ENEMY_DIRECTION } from '../../config/Constants';
import SpiderBossDeathFxAnimation from '../../view/uis/custom/bossmode/death/SpiderBossDeathFxAnimation';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { STATE_WALK } from './Enemy';
import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

export const STATE_IDLE 		= 'idle';
export const STATE_PRE_DEATH 	= 'pre_death';

const SHADOW_ALPHA = 1;

const SMOKE_CONTAINER_TYPES = { BACK: "back", FRONT: "front"}
const SMOKES_SETTINGS = {
	[ENEMY_DIRECTION.LEFT_UP]: 		[
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, pos: {x: -30, y: -80}, scaleMult: 1.08, emptyPreFrames: 2},
										{containerType: SMOKE_CONTAINER_TYPES.BACK,	pos: {x: 5, y: -40}, scaleMult: 1.08, emptyPreFrames: 2},
										{containerType: SMOKE_CONTAINER_TYPES.BACK,	pos: {x: 20, y: -50}, scaleMult: 1.45, emptyPreFrames: 15}	
									],
	[ENEMY_DIRECTION.LEFT_DOWN]: 	[
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, pos: {x: -50, y: -80}, scaleMult: 1.08, emptyPreFrames: 2},
										{containerType: SMOKE_CONTAINER_TYPES.BACK,	pos: {x: -15, y: -40}, scaleMult: 1.08, emptyPreFrames: 2},
										{containerType: SMOKE_CONTAINER_TYPES.BACK,	pos: {x: 0, y: -50}, scaleMult: 1.45, emptyPreFrames: 15}
									],
	[ENEMY_DIRECTION.RIGHT_UP]: 	[
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, pos: {x: 30, y: -80}, scaleMult: 1.08, emptyPreFrames: 2},
										{containerType: SMOKE_CONTAINER_TYPES.BACK,	pos: {x: -5, y: -40}, scaleMult: 1.08, emptyPreFrames: 2},
										{containerType: SMOKE_CONTAINER_TYPES.BACK,	pos: {x: -20, y: -50}, scaleMult: 1.45, emptyPreFrames: 15}	
									],
	[ENEMY_DIRECTION.RIGHT_DOWN]: 	[
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, pos: {x: 50, y: -80}, scaleMult: 1.08, emptyPreFrames: 2},
										{containerType: SMOKE_CONTAINER_TYPES.BACK,	pos: {x: 15, y: -40}, scaleMult: 1.08, emptyPreFrames: 2},
										{containerType: SMOKE_CONTAINER_TYPES.BACK,	pos: {x: 0, y: -50}, scaleMult: 1.45, emptyPreFrames: 15}	
									]
}

const WEAK_WALK_ANIMATIONS_COUNT = 2;

class SpiderBossEnemy extends BossEnemy 
{
	static get EVENT_ON_SPIDER_LANDED ()	{ return "EVENT_ON_SPIDER_LANDED" };

	constructor(params)
	{
		super(params);
		this._fSpiderWeb_sw = null;
		this._fSpiderDeathSmokes_arr = [];
		this._fSpineDelayFix_tmr = null;
		this.visible = false;
	}

	_initView()
	{
		this._weakWalkIndex = Utils.random(1, WEAK_WALK_ANIMATIONS_COUNT);

		super._initView();
	}

	//override
	get turnPostfix()
	{
		return "";
	}

	//override
	getSpineSpeed()
	{
		if (this.state === STATE_IDLE)
		{
			return 1;
		}
		else if (this.state === STATE_PRE_DEATH)
		{
			return 0.5;
		}

		let lBaseSpeed_num = 0.05;
		return this.speed * this.getScaleCoefficient() * lBaseSpeed_num;
	}

	get _customSpineTransitionsDescr()
	{
		return [
					{from: "<PREFIX>idle", to: "<PREFIX>walk", duration: 0.2}
				];
	}

	//override
	_calculateAnimationLoop(stateType)
	{
		if (stateType === STATE_PRE_DEATH)
		{
			return false;
		}

		return super._calculateAnimationLoop(stateType);
	}

	//override
	setSpineViewPos()
	{
		let pos = {x: 0, y: -20};
		this.spineViewPos = pos;
	}

	//override
	changeZindex()
	{
		super.changeZindex();
		if(!this.direction)
		{
			return;
		}
		switch (this.direction)
		{
			case ENEMY_DIRECTION.LEFT_DOWN:
			case ENEMY_DIRECTION.LEFT_UP:
				this.zIndex += 15;
				break;
			case ENEMY_DIRECTION.RIGHT_UP:
				this.zIndex += 25;
				break;
			case ENEMY_DIRECTION.RIGHT_DOWN:
				this.zIndex += 25;
				break;
		}
	}

	//override
	changeShadowPosition()
	{
		let x = 0, y = 0, scale = 1.9, alpha = SHADOW_ALPHA;

		this.shadow.position.set(x, y);
		this.shadow.scale.set(scale);
		this.shadow.alpha = alpha;
	}

	//override
	getScaleCoefficient()
	{
		return 1.3;
	}

	//override
	getLocalCenterOffset()
	{
		let pos = {x: 0, y: -20};
		return pos;
	}

	//override
	_getHitRectHeight()
	{
		return 120;
	}

	//override
	_getHitRectWidth()
	{
		return 190;
	}

	//override
	_getCallAnimationName()
	{
		return this.direction.substr(3) + '_hit';
	}

	//override
	_calcWalkAnimationName(aDirection_str)
	{
		let lWeakIndex_int = this._weakWalkIndex;
		let lWalkAnimationSuffix_str = this.isHealthStateWeak ? 'walk_damage'+lWeakIndex_int : "walk";
		return super._calcWalkAnimationName(aDirection_str, lWalkAnimationSuffix_str);
	}

	//override
	showBossAppearance(aSequence_arr, aInitialParams_obj)
	{
		this.visible = true;
		super.showBossAppearance(aSequence_arr, aInitialParams_obj);

		this._addSpiderWeb();

		let moveSequence = this.bossAppearanceDelta.sequences[0];
		moveSequence.once(Sequence.EVENT_ON_SEQUENCE_PLAYING_COMPLETED, this._onSpiderLanded, this);

		this.shadow.alpha = 0;
		this.shadow.fadeTo(SHADOW_ALPHA, 8*FRAME_RATE, Easing.sine.easeOut);
		this.shadow.visible = true;
	}

	_onSpiderLanded(event)
	{
		this._hideSpiderWeb();

		this.emit(SpiderBossEnemy.EVENT_ON_SPIDER_LANDED);

		this.setIdleIfPossible();
	}

	_addSpiderWeb()
	{
		let lWeb_sprt = this.container.addChild(APP.library.getSpriteFromAtlas('boss_mode/spider/spiderweb'));
		lWeb_sprt.scale.set(1.25, 1.25);
		lWeb_sprt.anchor.set(0.5, 1);
		lWeb_sprt.zIndex = 2;

		let lWebBounds_r = lWeb_sprt.getBounds();

		let lWebMask = this.container.addChild(new PIXI.Graphics);
		lWebMask.beginFill(0xff0000).drawRect(-lWebBounds_r.width/2, -lWebBounds_r.height, lWebBounds_r.width/2, lWebBounds_r.height).endFill();
		lWeb_sprt.mask = lWebMask;

		this._fSpiderWeb_sw = lWeb_sprt;
	}

	_hideSpiderWeb()
	{
		let lWeb_sprt = this._fSpiderWeb_sw;

		let lWebBounds_r = lWeb_sprt.getBounds();
		lWeb_sprt.moveTo(lWeb_sprt.x, lWebBounds_r.height, 15*FRAME_RATE, Easing.sine.easeInOut, this._onSpiderWebHidden.bind(this));
	}

	_onSpiderWebHidden()
	{
		if (this._fSpiderWeb_sw)
		{
			this._fSpiderWeb_sw.destroy();
			this._fSpiderWeb_sw = null;
		}
	}

	//override
	_generateBossAppearanceMask()
	{
		return null;
	}

	//override
	_applyBossStartParams(aInitialParams_obj)
	{
		let scaleParams = aInitialParams_obj.scale;

		this.bossAppearanceDelta = {x:aInitialParams_obj.x, y:aInitialParams_obj.y, scale: {x:scaleParams.x, y:scaleParams.y}};

		this.updateOffsets();
		
		this.container.scale.set(scaleParams.x, scaleParams.y);
	}

	updateScales()
	{
		let scale = {x: 1, y: 1};
		if (this.bossAppearanceDelta !== null && this.bossAppearanceDelta.scale !== undefined)
		{
			scale = {x: this.bossAppearanceDelta.scale.x, y: this.bossAppearanceDelta.scale.y};
		}

		this.container.scale.set(scale.x, scale.y);
	}

	//override
	tick(delta)
	{
		this.updateScales();
		
		super.tick(delta);
	}

	//override
	resetBossAppearance()
	{
		super.resetBossAppearance();

		if (this._fSpiderWeb_sw)
		{
			this._fSpiderWeb_sw.destroy();
			this._fSpiderWeb_sw = null;
		}
	}

	//idle state...
	get isIdleState()
	{
		return this.state == STATE_IDLE;
	}

	//override
	get isWalkTriggerAllowedState()
	{
		return this.isStayState || this.isIdleState;
	}

	setIdleIfPossible()
	{
		if (this.isFrozen)
		{
			return;
		}

		this.setIdle();
	}

	setIdle()
	{
		this.changeTextures(STATE_IDLE);
		this.spineView.view.state.addListener({ complete: (e) =>{
			this.changeTextures(STATE_WALK)
			}
		});
	}
	
	//override
	_calculateAnimationName(stateType)
	{
		if (stateType === STATE_IDLE)
		{
			return this._getIdleAnimationName();
		}
		else if (stateType === STATE_PRE_DEATH)
		{
			return this._getPreDeathAnimationName();
		}

		return super._calculateAnimationName(stateType);
	}

	_getIdleAnimationName()
	{
		return this.direction.substr(3) + '_idle';
	}
	//...idle state

	_getPreDeathAnimationName()
	{
		return this.direction.substr(3) + '_death';
	}

	//death...
	//override
	setDeathFramesAnimation(aIsInstantKill_bl = false, aPlayerWin_obj = null)
	{
		if(aIsInstantKill_bl)
		{
			super._playDeathFxAnimation(aIsInstantKill_bl);
		}
		else
		{
			this._deathInProgress = true;

			this._startDeathSpineAnim(aPlayerWin_obj);
			this._startDeathSmokesAnimation();

			let lEnemyPosition_pt = this.getGlobalPosition();
			lEnemyPosition_pt.x += this.getCurrentFootPointPosition().x;
			lEnemyPosition_pt.y += this.getCurrentFootPointPosition().y;
			this.emit(Enemy.EVENT_ON_DEATH_ANIMATION_STARTED, {position: lEnemyPosition_pt, angle: this.angle});
		}
	}

	_startDeathSpineAnim(aPlayerWin_obj)
	{
		this.spineView.view.state.onComplete = null;
		this.spineView.clearStateListeners();
		this.stateListener = null;

		this.changeTextures(STATE_PRE_DEATH);

		this.spineView.view.state.addListener({ complete: (e) =>{
														if (this.spineView)
														{
															this.spineView.stop();
															this.spineView.view.state.onComplete = null;
															this.spineView.clearStateListeners();
														}
														this._fSpineDelayFix_tmr = new Timer(this._onDeathSpineAnimCompleted.bind(this, aPlayerWin_obj), 100); // fix to prevent making fx before the last frame
													}
												});
	}

	_onDeathSpineAnimCompleted(aPlayerWin_obj)
	{
		this._fSpineDelayFix_tmr = null;
		this.dissolveShadow();
		this._stopSpinePlaying();
		this._playBossDeathFxAnimation(aPlayerWin_obj);
	}

	_startDeathSmokesAnimation()
	{
		if (!APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater)
		{
			return;
		}

		DeathFxAnimation.initTextures();

		let smokesSettings = SMOKES_SETTINGS[this.direction];
		for (let i=0; i<smokesSettings.length; i++)
		{
			let smokeSettings = smokesSettings[i];
			let smokeZIndex = smokeSettings.containerType === SMOKE_CONTAINER_TYPES.BACK ? this.spineView.zIndex-1 : this.spineView.zIndex+1;

			this._playSmokeEffect(smokeZIndex, smokeSettings.pos, smokeSettings.scaleMult, smokeSettings.emptyPreFrames);
		}
	}

	_playSmokeEffect(zIndex, positionDelta = {x: 0, y: 0}, scaleMult = 1, emptyPreFramesAmount=0)
	{
		let effect = this.container.addChild(Sprite.createMultiframesSprite(DeathFxAnimation.textures["smokePuff"], 4));
		if (emptyPreFramesAmount > 0)
		{
			let emptyFrames = [];
			for (let i=0; i<emptyPreFramesAmount; i++)
			{
				emptyFrames.push(PIXI.Texture.EMPTY);
			}
			effect.textures = emptyFrames.concat(effect.textures);
		}
		
		effect.blendMode = PIXI.BLEND_MODES.SCREEN;
		effect.scale.set(2*scaleMult);
		effect.position.set(positionDelta.x, positionDelta.y);
		effect.animationSpeed = 24/60;
		effect.play();
		effect.once('animationend', (e) => {
			let smokeIndex = this._fSpiderDeathSmokes_arr.indexOf(e.target);
			if (smokeIndex >= 0)
			{
				this._fSpiderDeathSmokes_arr.splice(smokeIndex, 1);
			}
			e.target.destroy();
		});

		this._fSpiderDeathSmokes_arr.push(effect);
	}

	_getBossDeathFxAnimationInstance(aPlayerWin_obj)
	{
		return new SpiderBossDeathFxAnimation(this.spineView, this.container, aPlayerWin_obj, this.direction, this.name);
	}
	//...death

	_startWeakState(aImmediately_bl = false)
	{
		aImmediately_bl = true;

		super._startWeakState(aImmediately_bl);
	}

	destroy(purely = false)
	{
		super.destroy(purely);

		this._fSpiderWeb_sw = null;
		this._fSpiderDeathSmokes_arr = null;
		this._fSpineDelayFix_tmr = null;
		this._weakWalkIndex = undefined;
	}
}

export default SpiderBossEnemy;