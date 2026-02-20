import Gun from '../Gun';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import EternalCryogunSmoke from '../cryogun/EternalCryogunSmoke';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import GameSoundsController from '../../../controller/sounds/GameSoundsController';
import SimpleSoundController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/sounds/SimpleSoundController';
import CryogunsController from '../../../controller/uis/weapons/cryogun/CryogunsController';

const STATES = 	{
					STATE_IDLE: 0,
					STATE_RELOAD: 1,
					STATE_SHOT: 2
				}
class Cryogun extends Gun {

	constructor(aIsMaster_bl)
	{
		super();

		this._fIsMaster_bl = aIsMaster_bl;
		this._fReloadCover_sprt = null;
		this._fEternalCryogunSmoke_eps = null;
		this._fChargeSound_snd = null;
		this._createView();
		this.reload();
	}

	//override
	reset()
	{
		super.reset();
		
		if (this.isReloadState)
		{
			return;
		}

		this._destroyChargeSound();
		this.reload();
	}

	_createView()
	{
		let lCryogun_sprt = this.addChild(APP.library.getSprite('weapons/Cryogun/cryogun'));
	}

	//override
	_initIdleState()
	{
		//nothing to do
	}

	//override
	_initReloadState()
	{
		if (this._fReloadCover_sprt)
		{
			return;
		}
		this._fReloadCover_sprt = this.addChild(new Sprite);
		
		let lFrozenCryogun_sprt = this._fReloadCover_sprt.addChild(APP.library.getSprite('weapons/Cryogun/cryogun_frozen'));
		lFrozenCryogun_sprt.alpha = 0;
		lFrozenCryogun_sprt.fadeTo(1, 8*2*16.7);

		let lFreezeOver_sprt = this._fReloadCover_sprt.addChild(APP.library.getSprite('weapons/Cryogun/cryogun_freeze_over'));
		let lOverMask_sprt = this._fReloadCover_sprt.addChild(APP.library.getSpriteFromAtlas('common/rounded_mask'));
		lOverMask_sprt.scale.set(0.6, 0.75);
		lFreezeOver_sprt.mask = lOverMask_sprt;
		lOverMask_sprt.position.set(0, 30);
		lOverMask_sprt.moveTo(0, -30, 15*2*16.7, null, () => {
			lFreezeOver_sprt.mask = null;
			lOverMask_sprt.destroy();
		});
		if(APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._fEternalCryogunSmoke_eps = this._fReloadCover_sprt.addChild(new EternalCryogunSmoke());
			this._fEternalCryogunSmoke_eps.position.set(0, -25);
			this._fEternalCryogunSmoke_eps.scale.set(4*0.15, 4*0.10);
			this._fEternalCryogunSmoke_eps.scaleTo(4*0.26, 4*0.16, 20*2*16.7);
			this._fEternalCryogunSmoke_eps.alpha = 0;
			let lAlphaSequence_seq = [
				{
					tweens: [],
					duration: 3*2*16.7
				},
				{
					tweens: [
						{prop: 'alpha', to: 1}
					],
					duration: 5*2*16.7
				}
			]
			Sequence.start(this._fEternalCryogunSmoke_eps, lAlphaSequence_seq);
		}
		this._playChargeSound();
		this.idle();
	}

	_playChargeSound()
	{
		let lVolume_num = this._fIsMaster_bl ? 1 : GameSoundsController.OPPONENT_WEAPON_VOLUME;
		let lChargeSound_snd = this._fChargeSound_snd = APP.soundsController.play('cryo_gun_charge', false, lVolume_num, !this._fIsMaster_bl);
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
		APP.currentWindow.cryogunsController.once(CryogunsController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._onFinishShot, this);
		this._destroyChargeSound();
		this.reload();
	}

	_onFinishShot()
	{
		this.emit(Gun.EVENT_ON_SHOT_COMPLETED);
		this.idle();
	}

	destroy()
	{
		this._destroyChargeSound();
		Sequence.destroy(Sequence.findByTarget(this._fEternalCryogunSmoke_eps));
		this._fEternalCryogunSmoke_eps && this._fEternalCryogunSmoke_eps.destroy();
		super.destroy();
	}
}

export default Cryogun;