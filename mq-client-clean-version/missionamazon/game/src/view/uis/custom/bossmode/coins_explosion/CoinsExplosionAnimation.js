import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { FRAME_RATE } from './../../../../../../../shared/src/CommonConstants';
import CoinsExplosionShockWaveAnimation from './CoinsExplosionShockWaveAnimation';
import CoinsExplosionSmokeAnimation from './CoinsExplosionSmokeAnimation';
import CoinsExplosionFlareAnimation from './CoinsExplosionFlareAnimation';
import CoinsExplosionCoinsAnimation from './CoinsExplosionCoinsAnimation';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const ANIMATIONS_TIMEOUTS = {
	"Coins":		3*FRAME_RATE,
	"ShockWaveA":	9*FRAME_RATE,
	"ShockWaveB":	32*FRAME_RATE,
	"Smoke":		109*FRAME_RATE,
	"Flare":		98*FRAME_RATE
}

class CoinsExplosionAnimation extends Sprite
{
	static get EVENT_ON_ANIMATION_COMPLETED()		{return "onCoinsExplosionAnimationCompleted";}
	static get EVENT_ON_ANIMATION_INTERRUPTED()		{return "onCoinsExplosionAnimationInterrupted";}

	startAnimation()
	{
		this._startAnimation();
	}

	interruptAnimation()
	{
		this._destroyAnimation();
	}

	constructor(aBossType_str, aIsCoPlayerWin_bln)
	{
		super();

		this._fShockWaveAnim_ceswa = null;
		this._fSmokeAnim_cesa = null;
		this._fCoinsAnimation_ceca = null;
		this._fFlare_cefa = null;
		this._fTimers_arr = [];
		this._fBossType_str = aBossType_str;
		this._fIsCoPlayerWin_bln = aIsCoPlayerWin_bln;

		this._fContainer_sprt = this.addChild(new Sprite());
		this._fContainer_sprt.visible = false;
	}

	_startAnimation()
	{
		this._fContainer_sprt.visible = true;

		let lShowSmoke_bln = APP.profilingController.info.isVfxProfileValueMediumOrGreater;
		if (lShowSmoke_bln)
		{
			this._startSmokeAnimation();
			this._startShockWaveAnimation();
		}

		this._startCoinsAnimation();
		this._startFlareAnimation();
	}

	_startCoinsAnimation()
	{
		this._fCoinsAnimation_ceca = this._fContainer_sprt.addChild(new CoinsExplosionCoinsAnimation(this._fIsCoPlayerWin_bln));
		this._fCoinsAnimation_ceca.on(CoinsExplosionCoinsAnimation.EVENT_ON_COINS_EXPLOSION_ANIMATION_COMPLETED, this._onAnimationCompleted, this);

		let lTimer_t = new Timer(()=>this._fCoinsAnimation_ceca.startAnimation(), ANIMATIONS_TIMEOUTS.Coins);
		this._fTimers_arr.push(lTimer_t);
	}

	_startShockWaveAnimation()
	{
		this._fShockWaveAnim_ceswa = this._fContainer_sprt.addChild(new CoinsExplosionShockWaveAnimation());

		let lTimer_t = new Timer(()=>this._fShockWaveAnim_ceswa.startPartA(), ANIMATIONS_TIMEOUTS.ShockWaveA);
		this._fTimers_arr.push(lTimer_t);

		lTimer_t = new Timer(()=>this._fShockWaveAnim_ceswa.startPartB(), ANIMATIONS_TIMEOUTS.ShockWaveB);
		this._fTimers_arr.push(lTimer_t);
	}

	_startSmokeAnimation()
	{
		this._fSmokeAnim_cesa = this._fContainer_sprt.addChild(new CoinsExplosionSmokeAnimation());
		this._fSmokeAnim_cesa.position.set(0, 82);

		let lTimer_t = new Timer(()=>this._fSmokeAnim_cesa.startAnimation(), ANIMATIONS_TIMEOUTS.Smoke);
		this._fTimers_arr.push(lTimer_t);
	}

	_startFlareAnimation()
	{
		this._fFlare_cefa = this._fContainer_sprt.addChild(new CoinsExplosionFlareAnimation(this._fBossType_str));

		let lTimer_t = new Timer(()=>this._fFlare_cefa.startAnimation(), ANIMATIONS_TIMEOUTS.Flare);
		this._fTimers_arr.push(lTimer_t);
	}

	_onAnimationCompleted()
	{
		this.emit(CoinsExplosionAnimation.EVENT_ON_ANIMATION_COMPLETED);
	}

	_destroyAnimation()
	{
		this._fContainer_sprt.visible = false;

		for (let lTimer_t of this._fTimers_arr)
		{
			lTimer_t && lTimer_t.destructor();
		}

		this._fShockWaveAnim_ceswa && this._fShockWaveAnim_ceswa.destroy();
		this._fShockWaveAnim_ceswa = null;

		this._fSmokeAnim_cesa && this._fSmokeAnim_cesa.destroy();
		this._fSmokeAnim_cesa = null;

		this._fFlare_cefa && this._fFlare_cefa.destroy();
		this._fFlare_cefa = null;

		this._fTimers_arr = [];

		if (this._fCoinsAnimation_ceca)
		{
			this._fCoinsAnimation_ceca.off(CoinsExplosionCoinsAnimation.EVENT_ON_COINS_EXPLOSION_ANIMATION_COMPLETED, this._onAnimationCompleted, this);
			this._fCoinsAnimation_ceca.destroy();
			this._fCoinsAnimation_ceca = null;
		}

		this.emit(CoinsExplosionAnimation.EVENT_ON_ANIMATION_INTERRUPTED);
	}

	destroy()
	{
		this._destroyAnimation();

		super.destroy();

		this._fShockWaveAnim_ceswa = null;
		this._fSmokeAnim_cesa = null;
		this._fCoinsAnimation_ceca = null;
		this._fFlare_cefa = null;
		this._fContainer_sprt = null;
		this._fTimers_arr = null;
		this._fBossType_str = null;
	}
}

export default CoinsExplosionAnimation;