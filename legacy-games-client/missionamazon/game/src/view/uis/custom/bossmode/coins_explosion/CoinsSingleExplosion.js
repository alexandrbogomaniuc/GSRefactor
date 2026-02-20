import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sequence } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation';
import { FRAME_RATE } from './../../../../../../../shared/src/CommonConstants';
import CoinsEmitter from './CoinsEmitter';
import CoinsEmitterSilver from './CoinsEmitterSilver';

class CoinsSingleExplosion extends Sprite
{
	static get EVENT_ON_ANIMATION_COMPLETED()		{return "onSingleCoinAnimationCompleted";}

	startAnimation()
	{
		this._startAnimation();
	}

	constructor(aIsCoPlayerWin_bln)
	{
		super();

		this._fIsCoPlayerWin_bln = aIsCoPlayerWin_bln;
		this._fFlare_sprt = null;
		this._fEmitters_arr = [];
	}

	_startAnimation()
	{
		this._animateFlare();
		this._animateCoins();
	}

	_animateFlare()
	{
		this._fFlare_sprt = this.addChild(APP.library.getSprite("boss_mode/coins_explode/flare"));
		this._fFlare_sprt.scale.set(0);
		this._fFlare_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		let lSeq_arr = [
			{tweens: [{prop: "scale.x", to: 1}, {prop: "scale.y", to: 1}], duration: 5*FRAME_RATE},
			{tweens: [{prop: "scale.x", to: 0.675}, {prop: "scale.y", to: 0.675}], duration: 4*FRAME_RATE},
			{tweens: [{prop: "scale.x", to: 0}, {prop: "scale.y", to: 0}, {prop: "alpha", to: 0}], duration: 5*FRAME_RATE, onfinish: () => {
				this._fFlare_sprt && this._fFlare_sprt.destroy();
				this._fFlare_sprt = null;
			}}
		];

		Sequence.start(this._fFlare_sprt, lSeq_arr, 7*FRAME_RATE);
	}

	_animateCoins()
	{
		let lEmitter_ce;
		if (this._fIsCoPlayerWin_bln)
		{
			lEmitter_ce = this.addChild(new CoinsEmitterSilver());
		}
		else
		{
			lEmitter_ce = this.addChild(new CoinsEmitter());
		}
		lEmitter_ce.alpha = 0.5;
		this._fEmitters_arr.push(lEmitter_ce);
		lEmitter_ce.once(CoinsEmitter.EVENT_ON_EMITTING_COMPLETED, this._onNextAnimationCompleted, this);
		lEmitter_ce.start();
	}

	_onNextAnimationCompleted(aEvent_obj)
	{
		let lEmitter_ce = aEvent_obj.target;

		let lId_num = this._fEmitters_arr.indexOf(lEmitter_ce);
		if (~lId_num) this._fEmitters_arr.splice(lEmitter_ce);

		lEmitter_ce && lEmitter_ce.destroy();

		this.emit(CoinsSingleExplosion.EVENT_ON_ANIMATION_COMPLETED);
	}

	destroy()
	{
		for (let lEmitter_ce of this._fEmitters_arr)
		{
			lEmitter_ce.off(CoinsEmitter.EVENT_ON_EMITTING_COMPLETED, this._onNextAnimationCompleted, this);
			lEmitter_ce && lEmitter_ce.destroy();
		}

		this._fFlare_sprt && Sequence.destroy(Sequence.findByTarget(this._fFlare_sprt));
		this._fFlare_sprt && this._fFlare_sprt.destroy();

		super.destroy();

		this._fFlare_sprt = null;
		this._fEmitters_arr = null;
	}
}

export default CoinsSingleExplosion;