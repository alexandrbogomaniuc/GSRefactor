import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import AtlasConfig from './../../../../config/AtlasConfig';
import { AtlasSprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import BigWinAnimation from './BigWinAnimation';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';

class HugeWinAnimation extends BigWinAnimation
{
	constructor(aPayoutValue_num)
	{
		super(aPayoutValue_num);

		this._fBurst_sprt = null;
		this._fCircleOut2_sprt = null;
		this._fCircleIn2_sprt = null;
	}

	_startAnimation()
	{
		super._startAnimation();

		this._startSecondCircleAnimation();
		this._startBurstAnimation();
	}

	_startBurstAnimation()
	{
		this._fBurst_sprt = this.addChild(APP.library.getSprite(this._burstAsset));
		this._fBurst_sprt.scale.set(0);
		this._fBurst_sprt.position.set(0, -30);
		this._fBurst_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		let l_seq = [
			{tweens: [],																				duration: 10*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 4},	{prop: "scale.y", to: 4}, {prop: "alpha", to: 0}],	duration: 21*FRAME_RATE, ease: Easing.cubic.easeOut, onfinish: ()=>{
				this._fBurst_sprt.scale.set(0);
				this._fBurst_sprt.alpha = 1;
			}},
			{tweens: [	{prop: "scale.x", to: 4},	{prop: "scale.y", to: 4}, {prop: "alpha", to: 0}],	duration: 25*FRAME_RATE, ease: Easing.cubic.easeOut, onfinish: ()=>{
				this._fBurst_sprt.scale.set(0);
				this._fBurst_sprt.alpha = 1;
			}},
			{tweens: [	{prop: "scale.x", to: 4},	{prop: "scale.y", to: 4}, {prop: "alpha", to: 0}],	duration: 19*FRAME_RATE, ease: Easing.cubic.easeOut, onfinish: ()=>{
				this._endBurstSequence();
			}}
		];

		Sequence.start(this._fBurst_sprt, l_seq);
	}

	_endBurstSequence()
	{
		this._fBurst_sprt && Sequence.destroy(Sequence.findByTarget(this._fBurst_sprt));
		this._fBurst_sprt && this._fBurst_sprt.destroy();
		this._fBurst_sprt = null;

		this._validateEnding();
	}

	_startSecondCircleAnimation()
	{
		this._fCircleIn2_sprt = this.addChild(APP.library.getSprite(this._circleAssetIn));
		this._fCircleIn2_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fCircleIn2_sprt.position.set(0, -20);
		this._fCircleIn2_sprt.scale.set(0);
		this._fCircleIn2_sprt.alpha = 0.8;

		this._fCircleOut2_sprt = this.addChild(APP.library.getSprite(this._circleAssetOut));
		this._fCircleOut2_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fCircleOut2_sprt.position.set(0, -20);
		this._fCircleOut2_sprt.scale.set(0);
		this._fCircleOut2_sprt.alpha = 0.8;

		let lScaleIn_seq = [
			{tweens: [],															duration: 8*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 4.9},	{prop: "scale.y", to: 4.9}],	duration: 15*FRAME_RATE}
		];

		let lAlphaIn_seq = [
			{tweens: [],							duration: 13*FRAME_RATE},
			{tweens: [	{prop: "alpha", to: 0}],	duration: 8*FRAME_RATE, onfinish: ()=>{
				this._endCircleIn2Sequence();
			}}
		];

		let lScaleOut_seq = [
			{tweens: [],															duration: 8*FRAME_RATE},
			{tweens: [	{prop: "scale.x", to: 4.9},	{prop: "scale.y", to: 4.9}],	duration: 15*FRAME_RATE}
		];

		let lAlphaOut_seq = [
			{tweens: [],							duration: 13*FRAME_RATE},
			{tweens: [	{prop: "alpha", to: 0}],	duration: 8*FRAME_RATE, onfinish: ()=>{
				this._endCircleOut2Sequence();
			}}
		];

		Sequence.start(this._fCircleOut2_sprt, lScaleOut_seq, 1*FRAME_RATE);
		Sequence.start(this._fCircleOut2_sprt, lAlphaOut_seq, 1*FRAME_RATE);

		Sequence.start(this._fCircleIn2_sprt, lScaleIn_seq, 4*FRAME_RATE);
		Sequence.start(this._fCircleIn2_sprt, lAlphaIn_seq, 4*FRAME_RATE);
	}

	_endCircleIn2Sequence()
	{
		this._fCircleIn2_sprt && Sequence.destroy(Sequence.findByTarget(this._fCircleIn2_sprt));
		this._fCircleIn2_sprt && this._fCircleIn2_sprt.destroy();
		this._fCircleIn2_sprt = null;

		this._validateEnding();
	}

	_endCircleOut2Sequence()
	{
		this._fCircleOut2_sprt && Sequence.destroy(Sequence.findByTarget(this._fCircleOut2_sprt));
		this._fCircleOut2_sprt && this._fCircleOut2_sprt.destroy();
		this._fCircleOut2_sprt = null;

		this._validateEnding();
	}

	_startCoinsExplodeAnimation()
	{
		this._startNextExplosion(6*FRAME_RATE, {x: 0, y: 30});
		this._startNextExplosion(17*FRAME_RATE, {x: -39, y: 30});
		this._startNextExplosion(57*FRAME_RATE, {x: 0, y: 30}, true);
	}

	get _captionGlowAsset()
	{
		return "TAHugeWinGlowCaption";
	}

	get _payoutGlowColor()
	{
		return 0xc96000;
	}

	get _coinsAmount()
	{
		return 20;
	}

	get _burstAsset()
	{
		return "big_win/huge_win_burst";
	}

	get _captionAsset()
	{
		return "TAHugeWinCaption";
	}

	get _glowAsset()
	{
		return "big_win/glow_huge_win";
	}

	get _circleAssetIn()
	{
		return "big_win/circle_huge_win";
	}

	get _circleAssetOut()
	{
		return "big_win/circle_huge_win";
	}

	get _isSomethingAnimating()
	{
		return	super._isSomethingAnimating ||
				this._fCircleOut2_sprt ||
				this._fCircleIn2_sprt ||
				this._fBurst_sprt;
	}

	destroy()
	{
		this._fCircleOut2_sprt && Sequence.destroy(Sequence.findByTarget(this._fCircleOut2_sprt));
		this._fCircleIn2_sprt && Sequence.destroy(Sequence.findByTarget(this._fCircleIn2_sprt));
		this._fBurst_sprt && Sequence.destroy(Sequence.findByTarget(this._fBurst_sprt));

		super.destroy();

		this._fBurst_sprt = null;
		this._fCircleOut2_sprt = null;
		this._fCircleIn2_sprt = null;
	}
}

export default HugeWinAnimation;