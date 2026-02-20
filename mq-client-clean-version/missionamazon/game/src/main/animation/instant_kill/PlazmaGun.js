import Gun from '../Gun';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import InstantKillAtomizer from '../../animation/instant_kill/InstantKillAtomizer';
import GameSoundsController from '../../../controller/sounds/GameSoundsController';
import InstantKillGunFlare from '../../animation/instant_kill/InstantKillGunFlare';
import InstantKillSmokeLoop from '../../animation/instant_kill/InstantKillSmokeLoop';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import SimpleSoundController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/sounds/SimpleSoundController';

class PlazmaGun extends Gun
{
	constructor(aIsMaster_bl)
	{
		super();

		this._fIsMaster_bl = aIsMaster_bl;
		this._defaultView = null;
		this._fAlternateView_sprt = null;
		this._fAtomizer_ica = null;
		this._fReloadTimer_tmr = null;

		this._createView();
		this.idle();
	}

	//override
	reset()
	{
		super.reset();
		
		if (this.isIdleState)
		{
			return;
		}

		this._clearReloadEffect();
		this.idle();
	}

	shotCompleted()
	{
		this.idle();
		this.emit(Gun.EVENT_ON_SHOT_COMPLETED);
	}

	//override
	get isCharged()
	{
		return this._isAlreadyCharged;
	}

	_createView()
	{
		let lPlazmagun_sprt = this._defaultView = this.addChild(APP.library.getSpriteFromAtlas('weapons/InstantKill/gun_off'));
	}

	_createAlternateSprite() 
	{
		if (this._fAlternateView_sprt)
		{
			return this._fAlternateView_sprt;
		}

		let alternate = APP.library.getSpriteFromAtlas('weapons/InstantKill/gun_on');
		//gun flares...
		for (let i = 0; i < 3; i++)
		{
			alternate.addChild(new InstantKillGunFlare(i));
		}
		alternate.addChild(new InstantKillSmokeLoop(1.74, 1.74));
		//...gun flares
		
		this._fAlternateView_sprt = this.addChild(alternate);
		this._fAlternateView_sprt.alpha = 0;
		return this._fAlternateView_sprt;
	}

	//override
	_initIdleState()
	{
		//nothing to do
	}

	//override
	_initReloadState()
	{
		if (!!this._fReloadTimer_tmr)
		{
			return;
		}

		if (this._fIsMaster_bl && !this._isAlreadyCharged)
		{
			APP.soundsController.play('plasma_target');
		}

		let lReloadTimerDuration_num = this._isAlreadyCharged ? 120 : 160;
		this._fReloadTimer_tmr = new Timer(this._onReloadTimerCompleted.bind(this), lReloadTimerDuration_num);
	}

	get _isAlreadyCharged()
	{
		return !!this._fAtomizer_ica || !!this._fAlternateView_sprt;
	}

	_onReloadTimerCompleted()
	{
		this._destroyReloadTimer();

		if (this._isAlreadyCharged)
		{
			this._onReloadCompleted();
		}
		else
		{
			if (!this._fAlternateView_sprt)
			{
				this.showAlternate();
			}

			if (!this._fAtomizer_ica)
			{
				let atomizer = this._fAtomizer_ica = this.addChild(new InstantKillAtomizer());
				atomizer.on(InstantKillAtomizer.EVENT_ON_ANIMATION_COMPLETED, this._onAtomizerAnimationCompleted, this);
				atomizer.once(Sprite.EVENT_ON_DESTROYING, this._onAtomizerDestroying, this);
			}
		}
	}

	_onAtomizerAnimationCompleted(event)
	{
		this._fAtomizer_ica && this._fAtomizer_ica.off(Sprite.EVENT_ON_DESTROYING, this._onAtomizerDestroying, this, true);

		this._onReloadCompleted();

		this._fAtomizer_ica && this._fAtomizer_ica.destroy();
		this._fAtomizer_ica = null;
	}

	_onAtomizerDestroying(event)
	{
		if (this.isReloadState)
		{
			this._onReloadCompleted();
		}
	}

	showAlternate()
	{
		this._createAlternateSprite();

		this._fAlternateView_sprt.fadeTo(1, 20 * 2 * 16.6);
		this._defaultView.fadeTo(0, 20 * 2 * 16.6);

		this._playChargeSound();
	}

	hideAlternate()
	{
		if (this._fAlternateView_sprt)
		{
			this._fAlternateView_sprt.removeTweens();
			this._defaultView.removeTweens();

			if (this._fAlternateView_sprt.alpha > 0)
			{
				this._fAlternateView_sprt.fadeTo(0, 50, null, () => {
																this._clearReloadEffect();
															});
				this._defaultView.fadeTo(1, 50);
			}
			else
			{
				this._clearReloadEffect();
			}
		}
	}

	_clearReloadEffect()
	{
		this._destroyChargeSound();
		this._destroyReloadTimer();

		this._fAlternateView_sprt && this._fAlternateView_sprt.destroy();
		this._fAlternateView_sprt = null;

		this._fAtomizer_ica && this._fAtomizer_ica.destroy();
		this._fAtomizer_ica = null;

		this._defaultView.alpha = 1;
	}

	_onReloadCompleted()
	{
		this.emit(Gun.EVENT_ON_RELOADED);
	}

	_playChargeSound()
	{
		let lPlasmaVolume_num = this._fIsMaster_bl ? 1 : GameSoundsController.OPPONENT_WEAPON_VOLUME;
		let lChargeSound_snd = this._fChargeSound_snd = APP.soundsController.play('plasma_powerup', false, lPlasmaVolume_num, !this._fIsMaster_bl);
		lChargeSound_snd && lChargeSound_snd.once(SimpleSoundController.i_EVENT_SOUND_DESTROYING, this._onChargeSoundDestroying, this);
	}

	_onChargeSoundDestroying(event)
	{
		this._fChargeSound_snd = null;
	}

	_destroyChargeSound()
	{
		if (this._fChargeSound_snd)
		{
			this._fChargeSound_snd.off(SimpleSoundController.i_EVENT_SOUND_DESTROYING, this._onChargeSoundDestroying, this, true);
			this._fChargeSound_snd.i_destroy();
			this._fChargeSound_snd = null;
		}
	}

	//override
	_initShotState()
	{
		
	}

	_destroyReloadTimer()
	{
		this._fReloadTimer_tmr && this._fReloadTimer_tmr.destructor();
		this._fReloadTimer_tmr = null;
	}

	destroy()
	{
		this._clearReloadEffect();
		
		super.destroy();
	}
}

export default PlazmaGun;