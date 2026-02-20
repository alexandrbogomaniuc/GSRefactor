import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';

class SmokeRisingAnimation extends Sprite
{
	static get EVENT_ON_ANIMATION_ENDED()			{return "onSmokeRisingAnimationEnded";}

	startAnimation()
	{
		this._startAnimation();
	}

	constructor()
	{
		super();

		this._animsCounter = 0;
		this._totalIterations = 0;
		this._animations = [];
	}

	_startAnimation()
	{
		this._totalIterations = 5;
		this._startNextSmoke();
	}

	_startNextSmoke(dir = -1)
	{
		let smoke = this.addChild(APP.library.getSpriteFromAtlas("boss_mode/smoke_fx"));
		smoke.blendMode = PIXI.BLEND_MODES.SCREEN;
		smoke.alpha = 0;

		++this._animsCounter;
		let seq = [
			{tweens:[{prop: "alpha", to: 0.6}, {prop: "rotation", to: Utils.gradToRad(dir*4.1)}, {prop: "position.y", to: -28}],	duration: 31*FRAME_RATE, onfinish: () => {
				--this._totalIterations;
				if (this._totalIterations > 0)
				{
					this._startNextSmoke(dir*-1);
				}
			}},
			{tweens:[{prop: "alpha", to: 0}, {prop: "rotation", to: Utils.gradToRad(dir*8.2)}, {prop: "position.y", to: -56}],		duration: 35*FRAME_RATE, onfinish: () => {
				smoke && smoke.destroy();
				--this._animsCounter;
				this._tryToFinishAnimation();
			}}
		];

		this._animations.push(smoke);
		Sequence.start(smoke, seq);
	}

	_tryToFinishAnimation()
	{
		if (this._animsCounter <= 0)
		{
			this._onAnimationFinished();
		}
	}

	_onAnimationFinished()
	{
		this.emit(SmokeRisingAnimation.EVENT_ON_ANIMATION_ENDED);
	}

	destroy()
	{
		if (this._animations)
		{
			for (let anim of this._animations)
			{
				anim && Sequence.destroy(Sequence.findByTarget(anim));
				anim && anim.destroy();
			}
		}

		super.destroy();

		this._animsCounter = null;
		this._totalIterations = null;
		this._animations = null;
	}
}
export default SmokeRisingAnimation;