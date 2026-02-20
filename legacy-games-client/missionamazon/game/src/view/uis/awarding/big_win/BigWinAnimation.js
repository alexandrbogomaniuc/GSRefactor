import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import BigWinPayoutView from './BigWinPayoutView';
import AtlasConfig from './../../../../config/AtlasConfig';
import { AtlasSprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import CoinsFlyAnimation from './CoinsFlyAnimation';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';
import { GlowFilter } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';

let _particlesTextures = null;
function _initParticlesTextures()
{
	if (_particlesTextures) return;

	_particlesTextures = AtlasSprite.getFrames(APP.library.getAsset("critical_hit/critical_particles"), AtlasConfig.CriticalParticles, "");
}

class BigWinAnimation extends Sprite
{
	static get EVENT_ON_BIG_WIN_ANIMATION_COMPLETED()			{return "onBigWinAnimationCompleted";}
	static get EVENT_ON_BIG_WIN_COINS_REQUIRED()				{return "onBigWinCoinsRequired";}
	static get EVENT_ON_BIG_WIN_PAYOUT_APPEARED()				{return "onBigWinPayoutAppeared";}

	startAnimation()
	{
		this._startAnimation();
	}

	get payoutValue()
	{
		return this._fPayoutValue_num;
	}

	constructor(aPayoutValue_num)
	{
		super();

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			_initParticlesTextures();
		}

		this._fPayoutValue_num = aPayoutValue_num;

		this._fPayoutView_bwpv = null;
		this._fPayoutGlowView_bwpv = null;
		this._fCaptionView_ta = null;
		this._fCaptionGlowView_ta = null;
		this._fFlare_sprt = null;
		this._fGlow_sprt = null;
		this._fCoinsFlyAnimation_cea = null;
		this._fCircleOut_sprt = null;
		this._fCircleIn_sprt = null;
		this._fEndFlare_sprt = null;
		this._fBigFlare_sprt = null;
		this._fCoinsFlyAnimations_arr = null;
		this._fParticles_arr = null;
	}

	_startAnimation()
	{
		this._fCoinsFlyAnimations_arr = [];
		this._fParticles_arr = [];

		this._startCircleAnimation();
		this._initGlowAnimation();
		this._startPayoutAnimation();
		this._initPayoutGlowAnimation();
		this._startCaptionAnimation();
		this._startCaptionGlowAnimation();
		this._startCoinsExplodeAnimation();
		this._initBigFlare();
		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._startFlareAnimation();
			this._initEndFlareAnimation();
		}

		APP.gameScreen.gameField.shakeTheGround();
	}

	_generateCaptionView()
	{
		return I18.generateNewCTranslatableAsset(this._captionAsset);
	}

	_startCaptionAnimation()
	{
		this._fCaptionView_ta = this.addChild(this._generateCaptionView());
		let lInitialParams = this._captionInitialParams;
		this._fCaptionView_ta.scale.set(lInitialParams.scale);
		this._fCaptionView_ta.position.set(lInitialParams.x, lInitialParams.y);

		let lScale_seq = this._captionScaleSequence;
		let lPos_seq = this._captionPosSequence;

		Sequence.start(this._fCaptionView_ta, lScale_seq);
		lPos_seq && Sequence.start(this._fCaptionView_ta, lPos_seq);
	}

	get _captionScaleSequence()
	{
		let lScale_seq = [
			{tweens: [	{prop: "scale.x", to: 0.83},	{prop: "scale.y", to: 0.83}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [],																duration: 2*FRAME_RATE, onfinish: ()=>{
				this._startGlowAnimation();
			}},
			{tweens: [	{prop: "scale.x", to: 1.2},		{prop: "scale.y", to: 1.2}],	duration: 3*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 0.88},	{prop: "scale.y", to: 0.88}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 0.92},	{prop: "scale.y", to: 0.92}],	duration: 8*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 0.82},	{prop: "scale.y", to: 0.82}],	duration: 3*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 1.2},		{prop: "scale.y", to: 1.2}],	duration: 3*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 0.88},	{prop: "scale.y", to: 0.88}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 0.92},	{prop: "scale.y", to: 0.92}],	duration: 8*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 0.87},	{prop: "scale.y", to: 0.87}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn, onfinish: ()=>{
				let lPosCaption_seq = [
					{tweens: [	{prop: "position.y", to: -7}],									duration: 5*FRAME_RATE, ease: Easing.cubic.easeIn}
				];
				Sequence.start(this._fCaptionView_ta, lPosCaption_seq);

				if (this._fPayoutView_bwpv)
				{
					Sequence.destroy(Sequence.findByTarget(this._fPayoutView_bwpv));

					this._startPayoutDisappear();
				}
			}},
			{tweens: [	{prop: "scale.x", to: 1.38},	{prop: "scale.y", to: 1.38}],	duration: 3*FRAME_RATE, ease: Easing.cubic.easeOut, onfinish: ()=>{
				this._onCoinsAnimationRequired();
			}},
			// {tweens: [],																duration: 2*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 0},		{prop: "scale.y", to: 0}],		duration: 3*FRAME_RATE, ease: Easing.cubic.easeOut, onfinish: ()=>{
				if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
				{
					this._startParticle({x: -16, y: -8}, Math.PI);
					this._startParticle({x: 16, y: 0}, -Math.PI);
					this._startEndFlareAnimation();
				}
				else
				{
					this._fParticles_arr = null;
				}
				this._startBigFlare();
				this._endPayoutSequence();
				this._endCaptionSequence();
			}}
		];

		return lScale_seq;
	}

	_startPayoutDisappear()
	{
		let lPosPyout_seq = [
			{tweens: [	{prop: "position.y", to: -20}],		duration: 5*FRAME_RATE, ease: Easing.cubic.easeIn}
		];
		Sequence.start(this._fPayoutView_bwpv, lPosPyout_seq);

		Sequence.start(this._fPayoutView_bwpv, this._payoutDisappearSequence);
	}

	get _payoutDisappearSequence()
	{
		let lScalePayout_seq = [
						{tweens: [	{prop: "scale.x", to: 1.5},		{prop: "scale.y", to: 1.5}],	duration: 3*FRAME_RATE, ease: Easing.cubic.easeOut},
						// {tweens: [],																duration: 2*FRAME_RATE},
						{tweens: [	{prop: "scale.x", to: 0},		{prop: "scale.y", to: 0}],		duration: 2*FRAME_RATE, ease: Easing.cubic.easeIn, onfinish: ()=>{
							this._endPayoutSequence();
						}}
					];

		return lScalePayout_seq;
	}

	get _captionPosSequence()
	{
		let lPos_seq = [
			{tweens: [	{prop: "position.x", to: 0},	{prop: "position.y", to: -72}],	duration: 5*FRAME_RATE, ease: Easing.cubic.easeIn}
		]

		return lPos_seq;
	}

	get _captionInitialParams()
	{
		return {x: 2541, y: -32, scale: 97.9};
	}

	_endCaptionSequence()
	{
		this._endCaptionGlowSequence();

		this._fCaptionView_ta && Sequence.destroy(Sequence.findByTarget(this._fCaptionView_ta));
		this._fCaptionView_ta && this._fCaptionView_ta.destroy();
		this._fCaptionView_ta = null;
		APP.gameScreen.gameField.shakeTheGround("bigWinEnd");

		this._validateEnding();
	}

	get _captionGlowAsset()
	{
		return "TABigWinGlowCaption";
	}

	_generateCaptionGlowView()
	{
		return I18.generateNewCTranslatableAsset(this._captionGlowAsset);
	}

	_startCaptionGlowAnimation()
	{
		this._fCaptionGlowView_ta = this._fCaptionView_ta.addChild(this._generateCaptionGlowView());
		this._fCaptionGlowView_ta.alpha = 1;

		let lAlpha_seq = [
			{tweens: [],							duration: 3*FRAME_RATE, ease: Easing.cubic.easeIn},
			{tweens: [	{prop: "alpha", to: 0}],	duration: 4*FRAME_RATE, ease: Easing.cubic.easeIn},
			{tweens: [],							duration: 17*FRAME_RATE},
			{tweens: [	{prop: "alpha", to: 1}],	duration: 3*FRAME_RATE, ease: Easing.cubic.easeIn},
			{tweens: [	{prop: "alpha", to: 0}],	duration: 4*FRAME_RATE, ease: Easing.cubic.easeIn, onfinish: ()=>{
				this._endCaptionGlowSequence();
			}}
		];

		Sequence.start(this._fCaptionGlowView_ta, lAlpha_seq);
	}

	_endCaptionGlowSequence()
	{
		this._fCaptionGlowView_ta && Sequence.destroy(Sequence.findByTarget(this._fCaptionGlowView_ta));
		this._fCaptionGlowView_ta && this._fCaptionGlowView_ta.destroy();
		this._fCaptionGlowView_ta = null;

		this._validateEnding();
	}

	_onCoinsAnimationRequired()
	{
		this.emit(BigWinAnimation.EVENT_ON_BIG_WIN_COINS_REQUIRED, {amount: this._coinsAmount});
	}

	get _captionAsset()
	{
		return "TABigWinCaption";
	}

	_startFlareAnimation()
	{
		this._fFlare_sprt = this.addChild(APP.library.getSprite("critical_hit/flare"));
		this._fFlare_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		this._fFlare_sprt.tint = 0xffdb4d;
		this._fFlare_sprt.scale.set(0);
		this._fFlare_sprt.position.set(0, -26);

		let lScale_seq = [
			{tweens: [],																duration: 3*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 3.92},	{prop: "scale.y", to: 1.92}],	duration: 4*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 0},		{prop: "scale.y", to: 0}],		duration: 7*FRAME_RATE, onfinish: ()=>{
				this._endFlareSequence();
			}}
		];

		Sequence.start(this._fFlare_sprt, lScale_seq);
	}

	_endFlareSequence()
	{
		this._fFlare_sprt && Sequence.destroy(Sequence.findByTarget(this._fFlare_sprt));
		this._fFlare_sprt && this._fFlare_sprt.destroy();
		this._fFlare_sprt = null;

		this._validateEnding();
	}

	_initEndFlareAnimation()
	{
		this._fEndFlare_sprt = this.addChild(APP.library.getSprite("critical_hit/flare"));
		this._fEndFlare_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		this._fEndFlare_sprt.tint = 0xffdb4d;
		this._fEndFlare_sprt.scale.set(0);
		this._fEndFlare_sprt.position.set(0, -26);
	}

	_startEndFlareAnimation()
	{
		let lScale_seq = [
			{tweens: [	{prop: "scale.x", to: 2.2},		{prop: "scale.y", to: 0.8}],	duration: 2*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 1.4},		{prop: "scale.y", to: 1}],		duration: 2*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 0},		{prop: "scale.y", to: 0}],		duration: 4*FRAME_RATE, onfinish: ()=>{
				this._endEndFlareSequence();
			}}
		];

		Sequence.start(this._fEndFlare_sprt, lScale_seq);
	}

	get _coinsAmount()
	{
		return 20;
	}

	_endEndFlareSequence()
	{
		this._fEndFlare_sprt && Sequence.destroy(Sequence.findByTarget(this._fEndFlare_sprt));
		this._fEndFlare_sprt && this._fEndFlare_sprt.destroy();
		this._fEndFlare_sprt = null;

		this._validateEnding();
	}

	_initBigFlare()
	{
		this._fBigFlare_sprt = this.addChild(APP.library.getSprite("big_win/flare"));
		this._fBigFlare_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fBigFlare_sprt.scale.set(0);
		this._fBigFlare_sprt.position.set(0, -26);
	}

	_startBigFlare()
	{
		let lScale_seq = [
			{tweens: [	{prop: "scale.x", to: 0.10},	{prop: "scale.y", to: 0.10}],	duration: 1*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 0.23},	{prop: "scale.y", to: 0.23}],	duration: 1*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 0},		{prop: "scale.y", to: 0}],		duration: 14*FRAME_RATE, onfinish: ()=>{
				this._endBigFlareSequence();
			}}
		];

		let lRotation_seq = [
			{tweens: [	{prop: "rotation", to: 0.28}],		duration: 16*FRAME_RATE}
		];

		Sequence.start(this._fBigFlare_sprt, lScale_seq);
		Sequence.start(this._fBigFlare_sprt, lRotation_seq);
	}

	_endBigFlareSequence()
	{
		this._fBigFlare_sprt && Sequence.destroy(Sequence.findByTarget(this._fBigFlare_sprt));
		this._fBigFlare_sprt && this._fBigFlare_sprt.destroy();
		this._fBigFlare_sprt = null;

		this._validateEnding();
	}

	_initGlowAnimation()
	{
		this._fGlow_sprt = this.addChild(APP.library.getSprite(this._glowAsset));
		this._fGlow_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fGlow_sprt.scale.set(1.125, 0.75);
		this._fGlow_sprt.position.set(0, -20);
		this._fGlow_sprt.alpha = 0;
	}

	_startGlowAnimation()
	{
		let lAlpha_seq = [
			{tweens: [	{prop: "alpha", to: 1}],	duration: 8*FRAME_RATE},
			{tweens: [],							duration: 45*FRAME_RATE},
			{tweens: [	{prop: "alpha", to: 0}],	duration: 5*FRAME_RATE, onfinish: ()=>{
				this._endGlowSequence();
			}}
		];

		Sequence.start(this._fGlow_sprt, lAlpha_seq);
	}

	_endGlowSequence()
	{
		this._fGlow_sprt && Sequence.destroy(Sequence.findByTarget(this._fGlow_sprt));
		this._fGlow_sprt && this._fGlow_sprt.destroy();
		this._fGlow_sprt = null;

		this._validateEnding();
	}

	get _glowAsset()
	{
		return "big_win/glow_big_win";
	}

	_startPayoutAnimation()
	{
		this._fPayoutView_bwpv = this.addChild(new BigWinPayoutView());
		this._fPayoutView_bwpv.once(BigWinPayoutView.EVENT_ON_VALUE_COUNTING_COMPLETED, this._onValueCountingCompleted, this);
		this._fPayoutView_bwpv.showPayout(this._startPayoutValue, this._countingDuration);
		this._fPayoutView_bwpv.scale.set(0);
		this._fPayoutView_bwpv.position.set(0, 25);

		let lScale_seq = this._payoutSequence;
		Sequence.start(this._fPayoutView_bwpv, lScale_seq);
	}

	get _payoutSequence()
	{
		let lScale_seq = [
			{tweens: [	{prop: "scale.x", to: 0.9},		{prop: "scale.y", to: 0.9}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 1.35},	{prop: "scale.y", to: 1.35}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 1.3},		{prop: "scale.y", to: 1.3}],	duration: 2*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 0.96},	{prop: "scale.y", to: 0.96}],	duration: 3*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 1},		{prop: "scale.y", to: 1}],		duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn, onfinish: ()=>{ this._onPayoutAppeared(); } },
			{tweens: [	{prop: "scale.x", to: 0.9},		{prop: "scale.y", to: 0.9}],	duration: 7*FRAME_RATE, ease: Easing.quadratic.easeIn, onfinish: ()=>{
				this._startPayoutGlowAnimation();
			}},
			{tweens: [	{prop: "scale.x", to: 1.3},		{prop: "scale.y", to: 1.3}],	duration: 3*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 0.96},	{prop: "scale.y", to: 0.96}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 1},		{prop: "scale.y", to: 1}],		duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 0.95},	{prop: "scale.y", to: 0.95}],	duration: 4*FRAME_RATE, ease: Easing.quadratic.easeIn}
		];

		return lScale_seq;
	}

	get _startPayoutValue()
	{
		return this._fPayoutValue_num;
	}

	_onValueCountingCompleted(event)
	{
	}

	_onPayoutAppeared()
	{
		this.emit(BigWinAnimation.EVENT_ON_BIG_WIN_PAYOUT_APPEARED);
	}

	get _countingDuration()
	{
		return 0;
	}

	get _payoutGlowColor()
	{
		return 0xfff6cb;
	}

	_initPayoutGlowAnimation()
	{
		if (!APP.isDeprecetedInternetExplorer) this._fPayoutView_bwpv.filters = [new GlowFilter({distance: 12, outerStrength: 0, innerStrength: 3, color: this._payoutGlowColor, quality: 2})];

		let lBounds_obj = this._fPayoutView_bwpv.Container_sprt.getBounds();
		let l_txtr = PIXI.RenderTexture.create({ width: lBounds_obj.width, height:  lBounds_obj.height, scaleMode: PIXI.SCALE_MODES.LINEAR, resolution: 2 });
		APP.stage.renderer.render(this._fPayoutView_bwpv, { renderTexture: l_txtr });

		let l_sprt = new Sprite();
		l_sprt.texture = l_txtr;
		l_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		l_sprt.pivot.x = 8;
		this._fPayoutView_bwpv.filters = [];

		this._fPayoutGlowView_bwpv = this._fPayoutView_bwpv.addChild(l_sprt);
		this._fPayoutGlowView_bwpv.alpha = 0;
	}

	_startPayoutGlowAnimation()
	{
		let lAlpha_seq = [
			{tweens: [],							duration: 1*FRAME_RATE},
			{tweens: [	{prop: "alpha", to: 1}],	duration: 3*FRAME_RATE, ease: Easing.cubic.easeIn},
			{tweens: [	{prop: "alpha", to: 0}],	duration: 4*FRAME_RATE, ease: Easing.cubic.easeIn, onfinish: ()=>{
				this._endPayoutGlowSequence();
			}}
		];

		Sequence.start(this._fPayoutGlowView_bwpv, lAlpha_seq);
	}

	_endPayoutSequence()
	{
		this._endPayoutGlowSequence();

		this._fPayoutView_bwpv && Sequence.destroy(Sequence.findByTarget(this._fPayoutView_bwpv));
		this._fPayoutView_bwpv && this._fPayoutView_bwpv.destroy();
		this._fPayoutView_bwpv = null;

		this._validateEnding();
	}

	_endPayoutGlowSequence()
	{
		this._fPayoutGlowView_bwpv && Sequence.destroy(Sequence.findByTarget(this._fPayoutGlowView_bwpv));
		this._fPayoutGlowView_bwpv && this._fPayoutGlowView_bwpv.destroy();
		this._fPayoutGlowView_bwpv = null;

		this._validateEnding();
	}

	_startCircleAnimation()
	{
		this._fCircleIn_sprt = this.addChild(APP.library.getSprite(this._circleAssetIn));
		this._fCircleIn_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fCircleIn_sprt.position.set(0, -20);
		this._fCircleIn_sprt.scale.set(0);
		this._fCircleIn_sprt.alpha = 0.8;

		this._fCircleOut_sprt = this.addChild(APP.library.getSprite(this._circleAssetOut));
		this._fCircleOut_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fCircleOut_sprt.position.set(0, -20);
		this._fCircleOut_sprt.scale.set(0);
		this._fCircleOut_sprt.alpha = 0.8;

		let lScaleIn_seq = [
			{tweens: [],																duration: 5*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 4.9},		{prop: "scale.y", to: 4.9}],	duration: 15*FRAME_RATE}
		];

		let lAlphaIn_seq = [
			{tweens: [],							duration: 10*FRAME_RATE},
			{tweens: [	{prop: "alpha", to: 0}],	duration: 8*FRAME_RATE, onfinish: ()=>{
				this._endCircleInSequence();
			}}
		];

		let lScaleOut_seq = [
			{tweens: [],																duration: 5*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 4.9},		{prop: "scale.y", to: 4.9}],	duration: 15*FRAME_RATE}
		];

		let lAlphaOut_seq = [
			{tweens: [],							duration: 10*FRAME_RATE},
			{tweens: [	{prop: "alpha", to: 0}],	duration: 8*FRAME_RATE, onfinish: ()=>{
				this._endCircleOutSequence();
			}}
		];

		Sequence.start(this._fCircleOut_sprt, lScaleOut_seq);
		Sequence.start(this._fCircleOut_sprt, lAlphaOut_seq);

		Sequence.start(this._fCircleIn_sprt, lScaleIn_seq, 3*FRAME_RATE);
		Sequence.start(this._fCircleIn_sprt, lAlphaIn_seq, 3*FRAME_RATE);
	}

	_endCircleInSequence()
	{
		this._fCircleIn_sprt && Sequence.destroy(Sequence.findByTarget(this._fCircleIn_sprt));
		this._fCircleIn_sprt && this._fCircleIn_sprt.destroy();
		this._fCircleIn_sprt = null;

		this._validateEnding();
	}

	_endCircleOutSequence()
	{
		this._fCircleOut_sprt && Sequence.destroy(Sequence.findByTarget(this._fCircleOut_sprt));
		this._fCircleOut_sprt && this._fCircleOut_sprt.destroy();
		this._fCircleOut_sprt = null;

		this._validateEnding();
	}

	get _circleAssetIn()
	{
		return "big_win/circle_big_win";
	}

	get _circleAssetOut()
	{
		return "big_win/circle_mega_win";
	}

	_startParticle(aPos_obj, aRot_num, aOptScale_num=2.5)
	{
		let lParticle_sprt = this.addChild(new Sprite());
		this._fParticles_arr.push(lParticle_sprt);
		lParticle_sprt.textures = _particlesTextures;
		lParticle_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lParticle_sprt.scale.set(aOptScale_num);
		lParticle_sprt.rotation = aRot_num;
		lParticle_sprt.position.set(aPos_obj.x, aPos_obj.y);
		lParticle_sprt.animationSpeed = 30/60;
		lParticle_sprt.on('animationend', () => {
			let id = this._fParticles_arr.indexOf(lParticle_sprt);
			if (~id)
			{
				this._fParticles_arr.splice(id, 1);
			}
			lParticle_sprt && lParticle_sprt.destroy();
			lParticle_sprt = null;

			if (this._fParticles_arr.length == 0)
			{
				this._fParticles_arr = null;
			}

			this._validateEnding();
		});
		lParticle_sprt.play();
		lParticle_sprt.fadeTo(0, 12*FRAME_RATE);
	}

	_startCoinsExplodeAnimation()
	{
		this._startNextExplosion(6*FRAME_RATE, {x: 0, y: 30}, true);
	}

	_startNextExplosion(aDelay_num, aPos_obj, aFinal_bln)
	{
		let lCoinsExplosion_cfa = this.addChild(this._generateCoinsFlyAnimationInstance());
		lCoinsExplosion_cfa.position.set(aPos_obj.x, aPos_obj.y);
		this._fCoinsFlyAnimations_arr.push(lCoinsExplosion_cfa);
		if (aFinal_bln)
		{
			lCoinsExplosion_cfa.once(CoinsFlyAnimation.EVENT_ON_ANIMATION_ENDED, this._onCoinsAnimationEnded, this);
		}
		lCoinsExplosion_cfa.startAnimation(aDelay_num);
	}

	_generateCoinsFlyAnimationInstance()
	{
		let l_cfa = new CoinsFlyAnimation();
		return l_cfa;
	}

	_onCoinsAnimationEnded()
	{
		while (this._fCoinsFlyAnimations_arr && this._fCoinsFlyAnimations_arr.length)
		{
			this._fCoinsFlyAnimations_arr.pop().destroy();
		}
		this._fCoinsFlyAnimations_arr = null;

		this._validateEnding();
	}

	get _isSomethingAnimating()
	{
		return	this._fCaptionView_ta ||
				this._fCaptionGlowView_ta ||
				this._fPayoutView_bwpv ||
				this._fPayoutGlowView_bwpv ||
				this._fGlow_sprt ||
				this._fCircleOut_sprt ||
				this._fCircleIn_sprt ||
				this._fFlare_sprt ||
				this._fEndFlare_sprt ||
				this._fBigFlare_sprt ||
				this._fCoinsFlyAnimations_arr ||
				this._fParticles_arr;
	}

	_validateEnding()
	{
		if (this._isSomethingAnimating) return;

		this._onAnimationCompleted();
	}

	_onAnimationCompleted()
	{
		this.emit(BigWinAnimation.EVENT_ON_BIG_WIN_ANIMATION_COMPLETED, {value: this._fPayoutValue_num});
	}

	destroy()
	{
		this._fCaptionView_ta && Sequence.destroy(Sequence.findByTarget(this._fCaptionView_ta));
		this._fCaptionGlowView_ta && Sequence.destroy(Sequence.findByTarget(this._fCaptionGlowView_ta));
		this._fPayoutView_bwpv && Sequence.destroy(Sequence.findByTarget(this._fPayoutView_bwpv));
		this._fPayoutGlowView_bwpv && Sequence.destroy(Sequence.findByTarget(this._fPayoutGlowView_bwpv));
		this._fGlow_sprt && Sequence.destroy(Sequence.findByTarget(this._fGlow_sprt));
		this._fCircleOut_sprt && Sequence.destroy(Sequence.findByTarget(this._fCircleOut_sprt));
		this._fCircleIn_sprt && Sequence.destroy(Sequence.findByTarget(this._fCircleIn_sprt));
		this._fFlare_sprt && Sequence.destroy(Sequence.findByTarget(this._fFlare_sprt));
		this._fEndFlare_sprt && Sequence.destroy(Sequence.findByTarget(this._fEndFlare_sprt));
		this._fBigFlare_sprt && Sequence.destroy(Sequence.findByTarget(this._fBigFlare_sprt));

		if (this._fCoinsFlyAnimations_arr)
		{
			for (let anim of this._fCoinsFlyAnimations_arr)
			{
				anim && anim.off(CoinsFlyAnimation.EVENT_ON_ANIMATION_ENDED, this._onCoinsAnimationEnded, this);
				anim && anim.destroy();
			}
		}

		while (this._fParticles_arr && this._fParticles_arr.length)
		{
			this._fParticles_arr.pop().destroy();
		}

		super.destroy();

		this._fPayoutValue_num = null;

		this._fPayoutView_bwpv = null;
		this._fPayoutGlowView_bwpv = null;
		this._fCaptionView_ta = null;
		this._fCaptionGlowView_ta = null;
		this._fFlare_sprt = null;
		this._fGlow_sprt = null;
		this._fCoinsFlyAnimation_cea = null;
		this._fCircleOut_sprt = null;
		this._fCircleIn_sprt = null;
		this._fEndFlare_sprt = null;
		this._fBigFlare_sprt = null;
		this._fCoinsFlyAnimations_arr = null;
		this._fParticles_arr = null;
	}
}

export default BigWinAnimation;