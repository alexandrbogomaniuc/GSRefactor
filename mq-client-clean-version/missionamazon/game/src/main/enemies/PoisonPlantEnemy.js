import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { Sequence } from "../../../../../common/PIXI/src/dgphoenix/unified/controller/animation";
import { AtlasSprite, Sprite } from "../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import { APP } from "../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import ProfilingInfo from "../../../../../common/PIXI/src/dgphoenix/unified/model/profiling/ProfilingInfo";
import { FRAME_RATE } from "../../../../shared/src/CommonConstants";
import AtlasConfig from "../../config/AtlasConfig";
import PlantEnemy from "./PlantEnemy";

let vapor_textures;
function generate_vapor_textures()
{
	if(!vapor_textures)
	{
		vapor_textures = AtlasSprite.getFrames([APP.library.getAsset('enemies/plants/poison_vapor')], [AtlasConfig.PoisonPlantVapor], "");
	}
	return vapor_textures;
}

let streak_textures;
function generate_streak_textures()
{
	if(!streak_textures)
	{
		streak_textures = AtlasSprite.getFrames([APP.library.getAsset('enemies/plants/poison_streak')], [AtlasConfig.PoisonPlantStreak], "");
	}
	return streak_textures;
}

let sparkles_textures;
function generate_sparkles_textures()
{
	if(!sparkles_textures)
	{
		sparkles_textures = AtlasSprite.getFrames([APP.library.getAsset('enemies/plants/poison_sparkles')], [AtlasConfig.PoisonPlantSparkles], "");
	}
	return sparkles_textures;
}

class PoisonPlantEnemy extends PlantEnemy
{
	i_interruptPreDeathAnimationAndInstantDestroy()
	{
		Sequence.destroy(Sequence.findByTarget(this._fSpittedSmoke_spr));
		this._fSpittedSmoke_spr && this._fSpittedSmoke_spr.destroy();
		this._fSpittedSmoke_spr = null;
		Sequence.destroy(Sequence.findByTarget(this._greenHighlightFilter));

		this._fPoisonStreak_spr && this._fPoisonStreak_spr.destroy();
		this._fPoisonStreak_spr = null;

		this._fIsExpectedInstantDestroy_bl = true;
		this._playDeathFxAnimation(true);
	}


	i_playPreDeathAnimation()
	{
		if (this._fIsPreDeathAnimationPlaying_bl || this._fIsExpectedInstantDestroy_bl)
		{
			return;
		}

		this._fIsPreDeathAnimationPlaying_bl = true;

		super.i_playPreDeathAnimation();

		if(!this.spineView)
		{
			return;
		}

		let lTrack_obj = this.spineView.view.state.tracks[0];

		this.spineView.on("update", () => {
			if (lTrack_obj.trackLast <= 0.1*lTrack_obj.animationEnd)
			{
				if(this.spineView)
				{
					this.spineView.off("update");
				}
				
				this.shadow.visible = false;
				this._fDeathAnimationsCount_num++;
				this._destroyIdleAnimations();
				this._startSpittingGreenBileAnimation();
			}
		});
	}

	constructor(params)
	{
		super(params);

		this._fIsExpectedInstantDestroy_bl = null;
		this._fIsPreDeathAnimationPlaying_bl = null;
	}


	__startGrowAnimation()
	{
		if (APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM))
		{
			super.__startGrowAnimation();
			return;
		}

		//TRACK INDEXES
		let lTracks_num_arr = [0.01, 0.7, 1.3]; // the marks are percents of the animation duration. They are showing the moments in which to show the filter
		//

		if(!this.spineView)
		{
			return;
		}

		super.__startGrowAnimation();
		
		let lTrack_obj = this.spineView.view.state.tracks[0];
		//LISTENER FOR UPD TO SHOW TINT...

		this._fSpittingStamps_obj = {function: this._startSpittingGreenBileAnimation.bind(this), timeStamps: lTracks_num_arr, percentDelta: 0.018, animationName: 'grow'}
		this.spineView.addCallFunctionAtStamps(this._fSpittingStamps_obj);
		this.spineView.on('update', ()=>{
			if (lTrack_obj.trackTime >= 0.8*lTrack_obj.animationEnd)
			{
				if(this.spineView)
				{
					this.spineView.off("update");
				}

				this._startStreakAnimation();
			}
		});
		//...LISTENER FOR UPD TO SHOW TINT
	}

	__changeStateAfterGrowUp()
	{
		if (APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM))
		{
			super.__changeStateAfterGrowUp();
			return;
		}

		if(this.spineView)
		{
			this.spineView.removeCallsAtStamps(this._fSpittingStamps_obj);
		}

		this._fSpittingStamps_obj = null;
		super.__changeStateAfterGrowUp();
	}

	__startIdleAnimations()
	{
		if (!this.spineView || APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM))
		{
			return;
		}
		this._startIdleSmokeAnimation();
		this._startIdleVaporAnimation();
		this._startSparklesIdleAnimation();
	}

	_initView()
	{
		this._fIdleSmokes_spr_arr = [];
		this._fIdleVapor_spr_arr = [];
		this._fSparklesContainer = this.addChild(new Sprite());
		super._initView();
	}

	//override
	_getHitRectWidth()
	{
		return 170;
	}
	
	//override
	_getHitRectHeight()
	{
		return 150;
	}

	_startIdleSmokeAnimation()
	{
		this._fStartNewIdleSmokeAnimationTimer_t && this._fStartNewIdleSmokeAnimationTimer_t.destructor();
		let lSmoke_spr = this.addChild(this._generateIdleSmoke());
		this._fIdleSmokes_spr_arr.push(lSmoke_spr);

		let l_seq = [
			{tweens: [{prop: 'scale.x', from: 0, to: 1}, {prop: 'scale.y', from: 0, to: 1}, {prop: 'alpha', to: 1}], duration: 30*FRAME_RATE},
			{tweens: [{prop: 'scale.x', from: 1, to: 2}, {prop: 'scale.y', from: 1, to: 2}, {prop: 'alpha', to: 0}], duration: 20*FRAME_RATE, onfinish: ()=>{
				if (lSmoke_spr)
				{
					let l_num = this._fIdleSmokes_spr_arr.indexOf(lSmoke_spr);
					l_num != -1 && this._fIdleSmokes_spr_arr.splice(l_num, 1);
					lSmoke_spr.destroy();
				}
			}},
		];

		Sequence.start(lSmoke_spr, l_seq);

		let lTime_num = 20 + Math.random()*20;
		this._fStartNewIdleSmokeAnimationTimer_t = new Timer(this._startIdleSmokeAnimation.bind(this), lTime_num*FRAME_RATE);
	}

	_startIdleVaporAnimation()
	{
		let lFirst_spr = this.addChild(this._generateVapor({x: 0, y: -30}, 1));
		let lSecond_spr = this.addChild(this._generateVapor({x: 0, y: 0}, {x: 2, y: 0.5}, 2));
		let lThird_spr = this.addChild(this._generateVapor({x: 0, y: 0}, {x: -2, y: 0.5}, 2));

		this._fIdleVapor_spr_arr.push(lFirst_spr);
		this._fIdleVapor_spr_arr.push(lSecond_spr);
		this._fIdleVapor_spr_arr.push(lThird_spr);

		lFirst_spr.play();
		lSecond_spr.play();
		lThird_spr.play();
	}

	_startSparklesIdleAnimation()
	{
		this._fIdleSparkles_spr = this._fSparklesContainer.addChild(this._generateSparkles());
		this._fIdleSparkles_spr.zIndex = 1;
		this._fIdleSparkles_spr.animationSpeed = 0.35;
		this._fIdleSparkles_spr.play();
	}

	_generateSparkles()
	{
		let l_spr = new Sprite();
		l_spr.textures = generate_sparkles_textures();
		l_spr.anchor.set(0.5, 0.9);
		l_spr.scale.set(1, 2);
		l_spr.zIndex = 1;
		l_spr.blendMode = PIXI.BLEND_MODES.ADD;
		return l_spr;
	}

	get _greenShader()
	{
		return `
		attribute vec2 aVertexPosition;
		attribute vec2 aTextureCoord;
		uniform mat3 projectionMatrix;
		varying vec2 vTextureCoord;

		void main(void)
		{
			gl_Position = vec4((projectionMatrix * vec3(aVertexPosition, 1.0)).xy, 0.0, 1.0);
			vTextureCoord = aTextureCoord;
		}`;
	}

	get _greenFragmentShader()
	{
		return `
		varying vec2 vTextureCoord;
		uniform sampler2D uSampler;
		uniform float intensity;

		void main(void)
		{
			vec4 c = texture2D(uSampler, vTextureCoord);

			if (c.a < 0.7)
			{
				return;
			}

			vec4 result;
			float add = intensity;
			result.r = c.r;
			result.g = c.g+add*2.0;
			result.b = c.b-add;
			result.a = c.a;
			gl_FragColor = result;
		}`;
	}

	get _greenHighlightFilter()
	{
		if (!this._greenFilter)
		{
			this._greenFilter = new PIXI.Filter(this._greenShader, this._greenFragmentShader, {intensity: 0.5});
		}
		return this._greenFilter;
	}

	_startSpittingGreenBileAnimation()
	{
		if (!this.spineView || APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM))
		{
			if (this.spineView && this.spineView.i_isAnimationReverse || this._fDeathAnimationsCount_num > 0)
			{
				this._fDeathAnimationsCount_num--;
				this.destroy();
			}

			return;
		}
		if (this._fSpittedSmoke_spr)
		{
			return;
		}

		this._spittingGreenBileAnimationInProgress_bl = true;

		let lFilter_seq = [
			{
				tweens: [{prop: 'uniforms.intensity', from: 0.5, to: 0}],
				duration: 15*FRAME_RATE,
				onfinish: ()=>{
					if (this.spineView)
					{
						this.spineView.filters = null
					}
				}
			}
		];

		//SPARKLES...
		let lSparkles_spr = this.addChild(this._generateSparkles());
		lSparkles_spr.zIndex = 2;
		lSparkles_spr.scale.set(1, 2);
		lSparkles_spr.animationSpeed = 0.5;
		lSparkles_spr.position.set(0, 30);
		lSparkles_spr.on('animationend', ()=>{
			lSparkles_spr && lSparkles_spr.destroy();
		});
		lSparkles_spr.play();
		//...SPARKLES

		//SMOKE...
		this._fSpittedSmoke_spr = this.addChild(this._generateIdleSmoke());
		this._fSpittedSmoke_spr.position.set(0, -30);
		this._fSpittedSmoke_spr.zIndex = 2;
		this._fSpittedSmoke_spr.blendMode = PIXI.BLEND_MODES.ADD;

		let lSmokeAnimation_seq = [
			{tweens: [{prop: 'scale.x', from: 0, to: 1}, {prop: 'scale.y', from: 0, to: 1}, {prop: 'alpha', to: 1}], duration: 4*FRAME_RATE},
			{tweens: [{prop: 'scale.x', from: 1, to: 2}, {prop: 'scale.y', from: 1, to: 2}, {prop: 'alpha', to: 0}], duration: 5*FRAME_RATE, onfinish: ()=>{
				Sequence.destroy(Sequence.findByTarget(this._fSpittedSmoke_spr));
				this._fSpittedSmoke_spr && this._fSpittedSmoke_spr.destroy();
				this._fSpittedSmoke_spr = null;
				if (this.spineView && this.spineView.i_isAnimationReverse || this._fDeathAnimationsCount_num > 0)
				{
					this._fDeathAnimationsCount_num--;
					this.destroy();
				}
			}}
		];

		Sequence.start(this._fSpittedSmoke_spr, lSmokeAnimation_seq);
		//...SMOKE

		if(this.spineView)
		{
			this.spineView.filters = [this._greenHighlightFilter];
		}

		Sequence.start(this._greenHighlightFilter, lFilter_seq);
	}
	
	_startStreakAnimation()
	{
		if (APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM))
		{
			return;
		}

		this._fPoisonStreak_spr = this.addChild(new Sprite());
		this._fPoisonStreak_spr.textures = generate_streak_textures();

		this._fPoisonStreak_spr.zIndex = 1;
		this._fPoisonStreak_spr.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fPoisonStreak_spr.scale.set(1.5);
		this._fPoisonStreak_spr.anchor.set(0.5, 1);
		this._fPoisonStreak_spr.position.set(0, -20);

		this._fPoisonStreak_spr.on('animationend', ()=>{
			this._fPoisonStreak_spr && this._fPoisonStreak_spr.destroy();
			this._fPoisonStreak_spr = null;
		});

		this._fPoisonStreak_spr.play();
	}

	_generateVapor(aPosition_obj, aScale, aOptZIndex_num=0)
	{
		let l_spr = new Sprite();
		l_spr.textures = generate_vapor_textures();
		l_spr.anchor.set(0.6, 1);
		l_spr.position.set(aPosition_obj.x, aPosition_obj.y);
		l_spr.scale.set(aScale.x || aScale, aScale.y || aScale);

		l_spr.zIndex = aOptZIndex_num;

		return l_spr;
	}

	_generateIdleSmoke()
	{
		let l_spr = APP.library.getSprite('enemies/plants/poison_smoke');

		let lPositionX_num = -30 + Math.random()*60;
		let lPositionY_num = -30 + Math.random()*40;

		l_spr.alpha = 0;
		l_spr.zIndex = -1;
		l_spr.position.set(lPositionX_num, lPositionY_num);

		return l_spr;
	}

	//override
	changeShadowPosition()
	{
		let x = 5, y = -10, scale = 1.5;

		this.shadow.position.set(x, y);
		this.shadow.scale.set(scale);
	}

	_playDeathFxAnimation(aIsInstantKill_bl)
	{
		this._destroyIdleAnimations();
		super._playDeathFxAnimation(aIsInstantKill_bl);
	}
	
	_destroyIdleAnimations()
	{
		this._fStartNewIdleSmokeAnimationTimer_t && this._fStartNewIdleSmokeAnimationTimer_t.destructor();
		this._fStartNewIdleSmokeAnimationTimer_t = null;

		if (Array.isArray(this._fIdleSmokes_spr_arr))
		{
			while (this._fIdleSmokes_spr_arr.length > 0)
			{
				let l_spr = this._fIdleSmokes_spr_arr.pop();
				Sequence.destroy(Sequence.findByTarget(l_spr));
				l_spr.destroy();
			}
		}

		this._fIdleSmokes_spr_arr = null;

		if (Array.isArray(this._fIdleVapor_spr_arr))
		{
			while (this._fIdleVapor_spr_arr.length > 0)
			{
				let l_spr = this._fIdleVapor_spr_arr.pop();
				l_spr && l_spr.destroy();
			}
		}

		this._fIdleVapor_spr_arr = null;

		this._fIdleSparkles_spr && this._fIdleSparkles_spr.on('animationend', ()=>{
			this._fIdleSparkles_spr && this._fIdleSparkles_spr.destroy();
			this._fIdleSparkles_spr = null;
		});
		if (this._fSparklesContainer) this._fSparklesContainer.visible = false;
	}

	//override
	_freeze(aIsAnimated_bl = true)
	{
		this._destroyIdleAnimations();
		super._freeze(aIsAnimated_bl);
	}

	//override
	_unfreeze(aIsAnimated_bl = true)
	{
		this._fIdleSmokes_spr_arr = [];
		this._fIdleVapor_spr_arr = [];
		if (this._fSparklesContainer) this._fSparklesContainer.visible = true;
		this.__startIdleAnimations();
		super._unfreeze(aIsAnimated_bl);
	}

	destroy(purely)
	{
		this._destroyIdleAnimations();

		Sequence.destroy(Sequence.findByTarget(this._fSpittedSmoke_spr));
		this._fSpittedSmoke_spr && this._fSpittedSmoke_spr.destroy();
		this._fSpittedSmoke_spr = null;

		Sequence.destroy(Sequence.findByTarget(this._greenHighlightFilter));

		this._fPoisonStreak_spr && this._fPoisonStreak_spr.destroy();
		this._fPoisonStreak_spr = null;
		this._fSparklesContainer = null;

		this._fIsExpectedInstantDestroy_bl = null;
		this._fIsPreDeathAnimationPlaying_bl = null;
		
		super.destroy(purely);
	}
}

export default PoisonPlantEnemy;