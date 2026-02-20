import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { FRAME_RATE } from './../../../../../../../shared/src/CommonConstants';
import CoinsSingleExplosion from './CoinsSingleExplosion';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

const EXPLOSIONS_SEGMENTS_SETTINGS = [
	{
		position: {x: 10, y: -80},
		delay: 0*FRAME_RATE
	},
	{
		position: {x: 0, y: 0},
		delay: 0*FRAME_RATE
	},
	{
		position: {x: -4, y: 80},
		delay: 0*FRAME_RATE
	},
	{
		position: {x: 18, y: 88},
		delay: 5*FRAME_RATE
	},
	{
		position: {x: 10, y: -80},
		delay: 19*FRAME_RATE
	},
	{
		position: {x: 0, y: 0},
		delay: 19*FRAME_RATE
	},
	{
		position: {x: -4, y: 80},
		delay: 19*FRAME_RATE
	},
	{
		position: {x: 18, y: 88},
		delay: 28*FRAME_RATE
	},
	{
		position: {x: 10, y: -80},
		delay: 39*FRAME_RATE
	},
	{
		position: {x: 0, y: 0},
		delay: 39*FRAME_RATE
	},
	{
		position: {x: -4, y: 80},
		delay: 39*FRAME_RATE
	},
	{
		position: {x: 18, y: 88},
		delay: 50*FRAME_RATE
	},
	{
		position: {x: 10, y: -80},
		delay: 61*FRAME_RATE
	},
	{
		position: {x: 0, y: 0},
		delay: 61*FRAME_RATE
	},
	{
		position: {x: -4, y: 80},
		delay: 61*FRAME_RATE
	},
	{
		position: {x: 18, y: 88},
		delay: 76*FRAME_RATE
	}
];

class CoinsExplosionCoinsAnimation extends Sprite
{
	static get EVENT_ON_COINS_EXPLOSION_ANIMATION_COMPLETED()		{return "onCoinsExplosionAnimationCompleted";}

	startAnimation()
	{
		this._startAnimation();
	}

	constructor(aIsCoPlayerWin_bln)
	{
		super();

		this._fIsCoPlayerWin_bln = aIsCoPlayerWin_bln;
		this._fAnimationsCounter_num = 0;
		this._fTimers_arr = [];
	}

	_startAnimation()
	{
		this._fAnimationsCounter_num = 0;

		for (let lSet_obj of EXPLOSIONS_SEGMENTS_SETTINGS)
		{
			this._startExplosionSequence(lSet_obj);
		}
	}

	_startExplosionSequence(aSet_obj)
	{
		let lExplosion_cse = this.addChild(new CoinsSingleExplosion(this._fIsCoPlayerWin_bln));
		lExplosion_cse.position.set(aSet_obj.position.x, aSet_obj.position.y);

		lExplosion_cse.once(CoinsSingleExplosion.EVENT_ON_ANIMATION_COMPLETED, this._onSingleAnimationCompleted, this);

		let lDelayTimer_t = new Timer(()=>lExplosion_cse.startAnimation(), aSet_obj.delay);
		this._fTimers_arr.push(lDelayTimer_t);
	}

	_onSingleAnimationCompleted()
	{
		++this._fAnimationsCounter_num;
		let lTotalAnimations_num = EXPLOSIONS_SEGMENTS_SETTINGS.length;

		if (this._fAnimationsCounter_num >= lTotalAnimations_num)
		{
			this.emit(CoinsExplosionCoinsAnimation.EVENT_ON_COINS_EXPLOSION_ANIMATION_COMPLETED);
		}
	}

	destroy()
	{
		for (let lTimer_t of this._fTimers_arr)
		{
			lTimer_t && lTimer_t.destructor();
		}

		super.destroy();

		this._fAnimationsCounter_num = null;
		this._fTimers_arr = null;
	}
}

export default CoinsExplosionCoinsAnimation;