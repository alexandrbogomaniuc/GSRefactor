import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import { Sequence } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import MainOrbAnimation from './MainOrbAnimation';
import FinalOrbFXAnimation from './FinalOrbFXAnimation';

class ElectrifiedOrb extends Sprite
{
	static get EVENT_ON_ANIMATIONS_DESTROYED() { return "onAnimationsDestroyed"; }
	static get EVENT_ON_DISAPPEAR_ANIMATION_ENDED() { return FinalOrbFXAnimation.EVENT_ON_DISAPPEAR_ANIMATION_ENDED; }

	onFreeze()
	{
		this._handleFreeze();
	}

	onUnfreeze()
	{
		this._handleUnfreeze();
	}

	onDeathRequired()
	{
		this._onDeathRequired();
	}

	constructor()
	{
		super();

		this._fEffectContainer_sprt = null;
		this._fOrbContainer_sprt = null;
		this._fFinalFx_fofxa = null;

		this._fSmokes_arr = null;
		this._fFinalTimer_t = null;

		this._init();
	}

	_init()
	{
		this._fSmokes_arr = [];

		this._fEffectContainer_sprt = this.addChild(new Sprite());
		this._fOrbContainer_sprt = this._fEffectContainer_sprt.addChild(new MainOrbAnimation());
		this._fFinalFx_fofxa = this._fEffectContainer_sprt.addChild(new FinalOrbFXAnimation());

		this._startAnimations();
	}

	_handleFreeze()
	{
		if (!this._fEffectContainer_sprt) return;

		let lSeq_arr = [
			{ tweens: [{ prop: 'scale.x', to: 1.07 }, { prop: 'scale.y', to: 1.07 }], duration: 1.5 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 0 }, { prop: 'scale.y', to: 0 }, { prop: 'alpha', to: 0 }], duration: 3.5 * FRAME_RATE, onfinish: () => this._destroyAnimations() }
		];
		Sequence.start(this._fEffectContainer_sprt, lSeq_arr);
	}

	_handleUnfreeze()
	{
		this._destroyAnimations();
		this._init();

		this._fEffectContainer_sprt.scale.set(0);
		this._fEffectContainer_sprt.alpha = 0;

		let lSeq_arr = [
			{ tweens: [{ prop: 'scale.x', to: 1.07 }, { prop: 'scale.y', to: 1.07 }, { prop: 'alpha', to: 1 }], duration: 3.5 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 1 }, { prop: 'scale.y', to: 1 }], duration: 1.5 * FRAME_RATE }
		];
		Sequence.start(this._fEffectContainer_sprt, lSeq_arr);
	}

	_startAnimations()
	{
		this._fOrbContainer_sprt.startAnimation();
		this._startNextSmoke();
	}

	_startNextSmoke()
	{
		let lSmoke_sprt = this._fEffectContainer_sprt.addChild(APP.library.getSprite("enemies/blue_orbs/fx_electrified_orb/smoke"));
		lSmoke_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		lSmoke_sprt.scale.set(0.24);
		lSmoke_sprt.alpha = 0;

		let lSeq_arr = [
			{ tweens: [{ prop: 'scale.x', to: 0.39 }, { prop: 'scale.y', to: 0.39 }, { prop: 'alpha', to: 0.7 }], duration: 9 * FRAME_RATE, ease: Easing.quadratic.easeIn },
			{
				tweens: [{ prop: 'scale.x', to: 0.5 }, { prop: 'scale.y', to: 0.5 }, { prop: 'alpha', to: 0.51 }], duration: 4 * FRAME_RATE, onfinish: () =>
				{
					this._startNextSmoke();
				}
			},
			{
				tweens: [{ prop: 'scale.x', to: 0.7 }, { prop: 'scale.y', to: 0.7 }, { prop: 'alpha', to: 0 }], duration: 11 * FRAME_RATE, ease: Easing.quartic.easeOut, onfinish: () =>
				{
					if (lSmoke_sprt)
					{
						this._fEffectContainer_sprt && this._fEffectContainer_sprt.removeChild(lSmoke_sprt);
						lSmoke_sprt.destroy();

						if (this._fSmokes_arr)
						{
							let lId_num = this._fSmokes_arr.indexOf(lSmoke_sprt);
							if (~lId_num)
							{
								this._fSmokes_arr.splice(lId_num, 1);
							}
						}
					}
				}
			}
		];

		this._fSmokes_arr.push(lSmoke_sprt);
		Sequence.start(lSmoke_sprt, lSeq_arr);
	}

	_onDeathRequired()
	{
		this._startGlowAnimation();
	}

	_startGlowAnimation()
	{
		if (this._fOrbContainer_sprt)
		{
			this._fOrbContainer_sprt.startGlowAnimation();
			this._fOrbContainer_sprt.once(MainOrbAnimation.EVENT_ON_GLOW_COMPLETED, this._onGlowAnimationEnded, this);
		}
		else
		{
			this._onGlowAnimationEnded();
		}
	}

	_onGlowAnimationEnded()
	{
		if (!this._fOrbContainer_sprt)
		{
			this._finalDeathPart();
			return;
		}

		Sequence.destroy(Sequence.findByTarget(this._fOrbContainer_sprt));
		this._fOrbContainer_sprt.startDisappearAnimation();

		this._fFinalTimer_t && this._fFinalTimer_t.destructor();
		this._fFinalTimer_t = new Timer(() => this._finalDeathPart(), 2 * FRAME_RATE);
	}

	_finalDeathPart()
	{
		for (let lTarget_sprt of this._fSmokes_arr)
		{
			this._fEffectContainer_sprt && this._fEffectContainer_sprt.removeChild(lTarget_sprt);
			Sequence.destroy(Sequence.findByTarget(lTarget_sprt));
			lTarget_sprt.destroy();
		}

		if (this._fFinalFx_fofxa)
		{
			this._fFinalFx_fofxa.once(FinalOrbFXAnimation.EVENT_ON_DISAPPEAR_ANIMATION_ENDED, this._onDeathFxAnimationEnded, this);
			this._fFinalFx_fofxa.startAnimation();
		}
	}

	_onDeathFxAnimationEnded()
	{
		this._fFinalFx_fofxa && this._fFinalFx_fofxa.off(FinalOrbFXAnimation.EVENT_ON_DISAPPEAR_ANIMATION_ENDED, this._onDeathFxAnimationEnded, this);
		this.emit(ElectrifiedOrb.EVENT_ON_DISAPPEAR_ANIMATION_ENDED);
	}

	_destroyAnimations()
	{
		if (this._fSmokes_arr)
		{
			for (let lTarget_sprt of this._fSmokes_arr)
			{
				this._fEffectContainer_sprt && this._fEffectContainer_sprt.removeChild(lTarget_sprt);
				Sequence.destroy(Sequence.findByTarget(lTarget_sprt));
				lTarget_sprt.destroy();
			}
		}

		this._fFinalTimer_t && this._fFinalTimer_t.destructor();
		this._fFinalTimer_t = null;

		if (this._fFinalFx_fofxa)
		{
			this._fFinalFx_fofxa.off(FinalOrbFXAnimation.EVENT_ON_DISAPPEAR_ANIMATION_ENDED, this._onDeathFxAnimationEnded, this);
			this._fFinalFx_fofxa.destroy();
			this._fFinalFx_fofxa = null;
		}

		if (this._fOrbContainer_sprt)
		{
			this._fOrbContainer_sprt.off(MainOrbAnimation.EVENT_ON_GLOW_COMPLETED, this._onGlowAnimationEnded, this);
			Sequence.destroy(Sequence.findByTarget(this._fOrbContainer_sprt));
			this._fOrbContainer_sprt.destroy();
			this._fOrbContainer_sprt = null;
		}

		if (this._fEffectContainer_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fEffectContainer_sprt));
			this._fEffectContainer_sprt.destroy();
			this._fEffectContainer_sprt = null;
		}
		this._fSmokes_arr = [];

		this.emit(ElectrifiedOrb.EVENT_ON_ANIMATIONS_DESTROYED);
	}

	destroy()
	{
		this._destroyAnimations();

		super.destroy();

		this._fEffectContainer_sprt = null;
		this._fOrbContainer_sprt = null;
		this._fFinalFx_fofxa = null;

		this._fSmokes_arr = null;
		this._fFinalTimer_t = null;
	}
}

export default ElectrifiedOrb;