import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import BigWinAnimation from '../../../awarding/big_win/BigWinAnimation';
import AtlasSprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../../../config/AtlasConfig';
import { FRAME_RATE } from '../../../../../../../shared/src/CommonConstants';
import * as Easing from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Sequence from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { Utils } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

class YouWinAnimation extends BigWinAnimation
{
	static get EVENT_ON_YOU_WIN_MULTIPLIER_LANDED()				{return "EVENT_ON_YOU_WIN_MULTIPLIER_LANDED";}

	get isMasterWin()
	{
		return this._fIsMasterWin_bl;
	}

	get seatId()
	{
		return this._fSeatId_int;
	}

	constructor(aPayoutValue_num, aIsMasterWin_bl, aSeatId_int)
	{
		super(aPayoutValue_num);

		this._fIsMasterWin_bl = aIsMasterWin_bl;
		this._fSeatId_int = aSeatId_int;
		this._fMult_sprt = null;
	}

	//override
	get _captionInitialParams()
	{
		if (APP.isBattlegroundGame)
		{
			return {x: 0, y: -50, scale: 500};
		}
		return {x: 0, y: -72, scale: 0.83};
	}

	//override
	_generateCoinsFlyAnimationInstance()
	{
		let l_cfa = super._generateCoinsFlyAnimationInstance();
		if (!this._fIsMasterWin_bl)
		{
			l_cfa.coinTextures = AtlasSprite.getFrames([APP.library.getAsset("common/silver_coin_spin")], AtlasConfig.SilverWinCoin, "");
		}

		l_cfa.zIndex = APP.isBattlegroundGame ? 2 : 0;
		
		return l_cfa;
	}

	//override
	get _captionGlowAsset()
	{
		return "TAYouWinGlowCaption";
	}

	//override
	get _captionAsset()
	{
		return "TAYouWinCaption";
	}

	//override
	_startPayoutAnimation()
	{
		super._startPayoutAnimation();

		this._fPayoutView_bwpv.zIndex = 1;

		if (APP.isBattlegroundGame)
		{
			this._fPayoutView_bwpv.visible = false;
		}
	}

	get _payoutSequence()
	{
		if (APP.isBattlegroundGame)
		{
			return [
						{tweens: [	{prop: "scale.x", to: 0.9},		{prop: "scale.y", to: 0.9}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
						{tweens: [	{prop: "scale.x", to: 1.35},	{prop: "scale.y", to: 1.35}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
						{tweens: [	{prop: "scale.x", to: 1.3},		{prop: "scale.y", to: 1.3}],	duration: 2*FRAME_RATE, ease: Easing.quadratic.easeIn},
						{tweens: [	{prop: "scale.x", to: 0.96},	{prop: "scale.y", to: 0.96}],	duration: 3*FRAME_RATE, ease: Easing.quadratic.easeIn},
						{tweens: [	{prop: "scale.x", to: 1},		{prop: "scale.y", to: 1}],		duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn, onfinish: ()=>{ this._onPayoutAppeared(); } }
			];
		}

		if (!this._fIsMasterWin_bl)
		{
			return [
						{tweens: [	{prop: "scale.x", to: 0.3},		{prop: "scale.y", to: 0.3}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
						{tweens: [	{prop: "scale.x", to: 0.50},    {prop: "scale.y", to: 0.50}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
						{tweens: [	{prop: "scale.x", to: 0.45},	{prop: "scale.y", to: 0.45}],	duration: 2*FRAME_RATE, ease: Easing.quadratic.easeIn},
						{tweens: [	{prop: "scale.x", to: 0.35},	{prop: "scale.y", to: 0.35}],	duration: 3*FRAME_RATE, ease: Easing.quadratic.easeIn},
						{tweens: [	{prop: "scale.x", to: 0.40},	{prop: "scale.y", to: 0.40}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn, onfinish: ()=>{ this._onPayoutAppeared(); } },
						{tweens: [	{prop: "scale.x", to: 0.3},		{prop: "scale.y", to: 0.3}],	duration: 7*FRAME_RATE, ease: Easing.quadratic.easeIn},
						{tweens: [	{prop: "scale.x", to: 0.45},	{prop: "scale.y", to: 0.45}],	duration: 3*FRAME_RATE, ease: Easing.quadratic.easeIn},
						{tweens: [	{prop: "scale.x", to: 0.35},	{prop: "scale.y", to: 0.35}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
						{tweens: [	{prop: "scale.x", to: 0.45},	{prop: "scale.y", to: 0.45}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
						{tweens: [	{prop: "scale.x", to: 0.35},	{prop: "scale.y", to: 0.35}],	duration: 4*FRAME_RATE, ease: Easing.quadratic.easeIn}
			];
		}

		return super._payoutSequence;
	}

	_startPayoutDisappear()
	{
		Sequence.start(this._fPayoutView_bwpv, this._payoutDisappearSequence);
	}

	get _payoutDisappearSequence()
	{
		if (APP.isBattlegroundGame || this._fIsMasterWin_bl)
		{
			return super._payoutDisappearSequence;
		}

		let lScalePayout_seq = [
						{tweens: [	{prop: "scale.x", to: 0.5},		{prop: "scale.y", to: 0.5}],	duration: 3*FRAME_RATE, ease: Easing.cubic.easeOut},
						{tweens: [	{prop: "scale.x", to: 0},		{prop: "scale.y", to: 0}],		duration: 2*FRAME_RATE, ease: Easing.cubic.easeIn, onfinish: ()=>{
							this._endPayoutSequence();
						}}
					];

		return lScalePayout_seq;
	}

	get _startPayoutValue()
	{
				return super._startPayoutValue;
	}

	_onValueCountingCompleted(event)
	{
		super._onValueCountingCompleted(event);
	}

	//override
	_generateCaptionView()
	{
		if (APP.isBattlegroundGame)
		{
			if (this._fIsMasterWin_bl)
			{
				return APP.library.getSpriteFromAtlas("boss_mode/dragon_mult");
			}

			return APP.library.getSpriteFromAtlas("boss_mode/dragon_mult_silver");
		}

		let lCaptionView_sprt = super._generateCaptionView();
		if (!this._fIsMasterWin_bl)
		{
			lCaptionView_sprt.visible = false;
		}

		return lCaptionView_sprt;
	}

	//override
	_generateCaptionGlowView()
	{
		if (APP.isBattlegroundGame)
		{
			let lGlowView = APP.library.getSpriteFromAtlas("boss_mode/dragon_mult_glow");
			lGlowView.scale.set(2);
			lGlowView.blendMode = PIXI.BLEND_MODES.ADD;

			return lGlowView;
		}

		let lCaptionGlowView_sprt = super._generateCaptionGlowView();
		if (!this._fIsMasterWin_bl)
		{
			lCaptionGlowView_sprt.visible = false;
		}

		return lCaptionGlowView_sprt;
	}

	//override
	_startCaptionAnimation()
	{
		super._startCaptionAnimation();

		this._fCaptionView_ta.zIndex = APP.isBattlegroundGame ? 0 : 2;

		if (APP.isBattlegroundGame)
		{
			this._startGlowAnimation();

			if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
			{
				this._initMultLandingFlareAnimation();
			}
		}
	}

	get _captionScaleSequence()
	{
		if (
			!APP.isBattlegroundGame
			|| !APP.isBattlegroundGamePlayMode
		)
		{
			return super._captionScaleSequence;
		}

		let lItemView_si = APP.gameScreen.gameField.scoreboardController.view.getItemView(this._fSeatId_int);
		let lGlobalPos_p = lItemView_si ? lItemView_si.targetBossMultiplierGlobalPosition : new PIXI.Point(160, 270);
		let lTargetMultPos_p = this.globalToLocal(lGlobalPos_p.x, lGlobalPos_p.y);

		let lScale_seq = [
			{tweens: [	{prop: "scale.x", to: 0.9*1.7},	{prop: "scale.y", to: 0.9*1.7}],		duration: 5*FRAME_RATE, ease: Easing.quadratic.easeOut},
			{tweens: [],																		duration: 2*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 1.3*1.7},		{prop: "scale.y", to: 1.3*1.7}],	duration: 3*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 0.96*1.7},	{prop: "scale.y", to: 0.96*1.7}],	duration: 5*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 1.00*1.7},	{prop: "scale.y", to: 1.00*1.7}],	duration: 8*FRAME_RATE, ease: Easing.quadratic.easeIn},
			{tweens: [	{prop: "scale.x", to: 0.90*1.7},	{prop: "scale.y", to: 0.90*1.7}],	duration: 3*FRAME_RATE, ease: Easing.quadratic.easeIn, onfinish: ()=>{
				this._onCoinsAnimationRequired();
			}},
			{tweens: [	{prop: "scale.x", to: 1.30*1.7},	{prop: "scale.y", to: 1.30*1.7}],	duration: 3*FRAME_RATE, ease: Easing.quadratic.easeIn, onfinish: ()=>{
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
				if (this._fPayoutView_bwpv)
				{
					Sequence.destroy(Sequence.findByTarget(this._fPayoutView_bwpv));
					this._endPayoutSequence();
				}
			}},
			{tweens: [	{prop: "scale.x", to: 0.12*1.7},	{prop: "scale.y", to: 0.12*1.7},
						{prop: "position.x", to: lTargetMultPos_p.x}, {prop: "position.y", to: lTargetMultPos_p.y}],	
				duration: 5*FRAME_RATE, ease: Easing.quadratic.easeOut, onfinish: ()=>{
																						if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
																						{
																							this._fParticles_arr = this._fParticles_arr || [];
																							this._startParticle({x: lTargetMultPos_p.x, y: lTargetMultPos_p.y}, 0, 1.2);
																							this._startParticle({x: lTargetMultPos_p.x, y: lTargetMultPos_p.y}, Math.PI/2, 1.2);
																							this._startParticle({x: lTargetMultPos_p.x, y: lTargetMultPos_p.y}, Math.PI, 1.2);
																							this._startParticle({x: lTargetMultPos_p.x, y: lTargetMultPos_p.y}, Math.PI*3/2, 1.2);
																							this._startMultiplyerLandingAnimation({x: lTargetMultPos_p.x, y: lTargetMultPos_p.y});
																						}
																						this._endCaptionSequence();

																						lItemView_si && lItemView_si.i_doubleUpBossScores();
																						this.emit(YouWinAnimation.EVENT_ON_YOU_WIN_MULTIPLIER_LANDED, {money: this.payoutValue});
																					}}
		];

		return lScale_seq;
	}

	//override
	get _captionPosSequence()
	{
		if (!APP.isBattlegroundGame)
		{
			return super._captionPosSequence;
		}

		return null;
	}

	//override
	get _countingDuration()
	{
		if (APP.isBattlegroundGame)
		{
			return 1*FRAME_RATE;
		}

		if (!this._fIsMasterWin_bl)
		{
			return 0;
		}

		return 30*FRAME_RATE;
	}

	//override
	_startGlowAnimation()
	{
		this._fGlow_sprt.alpha = 1;

		let lAlpha_seq = [
			{tweens: [],							duration: 3*FRAME_RATE},
			{tweens: [	{prop: "alpha", to: 0}],	duration: 4*FRAME_RATE},
			{tweens: [],							duration: 17*FRAME_RATE},
			{tweens: [	{prop: "alpha", to: 1}],	duration: 3*FRAME_RATE},
			{tweens: [	{prop: "alpha", to: 0}],	duration: 4*FRAME_RATE, onfinish: ()=>{
				this._endGlowSequence();
			}}
		];

		Sequence.start(this._fGlow_sprt, lAlpha_seq);
	}

	_initMultLandingFlareAnimation()
	{
		this._fLandingFlare_sprt = this.addChild(APP.library.getSpriteFromAtlas("boss_mode/dragon_mult_flare"));
		this._fLandingFlare_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fLandingFlare_sprt.visible = false;
	}

	_startMultiplyerLandingAnimation(aPos_p)
	{
		let lLandingFlare_sprt = this._fLandingFlare_sprt;
		lLandingFlare_sprt.scale.set(0.57*2);
		lLandingFlare_sprt.position.set(aPos_p.x, aPos_p.y);

		let l_seq = [
			{tweens: [],								duration: 2*FRAME_RATE, onfinish: ()=>{ lLandingFlare_sprt.visible = true; }},
			{tweens: [	{prop: "scale.x", to: 0}, 
						{prop: "scale.y", to: 0}, 
						{prop: "rotation", to: Utils.gradToRad(45)} ],	duration: 15*FRAME_RATE, onfinish: ()=>{ this._onLandingFlareSeqCompleted(); }}
		];

		Sequence.start(lLandingFlare_sprt, l_seq);
	}

	_onLandingFlareSeqCompleted()
	{
		let lLandingFlare_sprt = this._fLandingFlare_sprt;
		Sequence.destroy(Sequence.findByTarget(lLandingFlare_sprt));
		lLandingFlare_sprt.destroy();
		this._fLandingFlare_sprt = null;

		this._validateEnding();
	}

	// override
	get _isSomethingAnimating()
	{
		return	super._isSomethingAnimating ||
				this._fLandingFlare_sprt;
	}

	destroy()
	{
		this._fIsMasterWin_bl = undefined;
		this._fSeatId_int = undefined;
		
		this._fLandingFlare_sprt && Sequence.destroy(Sequence.findByTarget(this._fLandingFlare_sprt));
		this._fLandingFlare_sprt = null;

		super.destroy();
	}
}

export default YouWinAnimation;