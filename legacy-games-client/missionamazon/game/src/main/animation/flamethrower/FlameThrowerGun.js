import Gun from '../Gun';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import TorchFxAnimation from '../TorchFxAnimation';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import FlameThrowerGunShotMuzzleEffects from './FlameThrowerGunShotMuzzleEffects';

export const MUZZLE_DISTANCE = 61; //is a distance from gun's center to gun's muzzle. Might be adjusted
export const MUZZLE_LENGTH = 20;

class FlameThrowerGun extends Gun {

	constructor()
	{
		super();

		this._idleFireContainer = null;
		this._shotHighlightContainer = null;
		this._baseGlow_sprt = null;
		this._muzzleEffects = null;

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

		this._clearShotEffects();
		this.idle();
	}

	_createView() 
	{
		TorchFxAnimation.initTextures();

		this._shotHighlightContainer = this.addChild(new Sprite());
		this._idleFireContainer = this.addChild(new Sprite());

		let lFlameThrower_sprt = this.addChild(APP.library.getSpriteFromAtlas('weapons/FlameThrower/flamethrower'));
		lFlameThrower_sprt.anchor.y = 155/249;
	}

	//override
	_initIdleState()
	{	
		this._clearShotEffects();

		let lTorch_sprt = this._idleFireContainer.addChild(new Sprite());
		lTorch_sprt.textures = TorchFxAnimation.textures.torch_blue;
		lTorch_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lTorch_sprt.anchor.set(0.5, 0.73);
		lTorch_sprt.position.set(0, -MUZZLE_DISTANCE-10);
		lTorch_sprt.scale.set(0.45, 0.3);
		lTorch_sprt.skew.y = Utils.gradToRad(70);
		
		lTorch_sprt.play();
	}

	//override
	_initShotState()
	{
		this._clearShotEffects();

		if (APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater)
		{
			this._showShotBaseGlow();
		}

		this._showShotMuzzleEffects();
	}

	_showShotBaseGlow()
	{
		let glow = this._baseGlow_sprt = this._shotHighlightContainer.addChild(APP.library.getSprite('common/crate_glow'));
		glow.anchor.set(0.6, 0.5);
		glow.rotation = Math.PI/2;
		glow.blendMode = PIXI.BLEND_MODES.ADD;
		glow.scale.set(2.2, 1.5);
		glow.y = -MUZZLE_DISTANCE;
		glow.alpha = 0;

		let sequence = [
			{
				tweens: [
					{prop: 'alpha', to: 0.76}
				],
				duration: 2*2*16.7
			},
			{
				tweens: [
					{prop: 'alpha', to: 0.68}
				],
				duration: 11*2*16.7
			},
			{
				tweens: [
					{prop: 'alpha', to: 0}
				],
				duration: 9*2*16.7
			}
		]
		Sequence.start(glow, sequence);
	}

	_showShotMuzzleEffects()
	{
		let muzzleEffects = this._muzzleEffects = this.addChild(new FlameThrowerGunShotMuzzleEffects());
		muzzleEffects.once(FlameThrowerGunShotMuzzleEffects.EVENT_ON_ANIMATION_COMPLETED, this._onShotMuzzleEffectsAnimationCompleted, this);
		muzzleEffects.startAnimation();
	}

	_onShotMuzzleEffectsAnimationCompleted()
	{
		this._clearShotEffects();
		this.idle();

		this.emit(Gun.EVENT_ON_SHOT_COMPLETED);
	}

	_clearShotEffects()
	{
		if (this._baseGlow_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._baseGlow_sprt));
			this._baseGlow_sprt.destroy();
			this._baseGlow_sprt = null;
		}

		if (this._muzzleEffects)
		{
			this._muzzleEffects.destroy();
			this._muzzleEffects = null;
		}
	}

	destroy()
	{
		this._clearShotEffects();
		
		this._idleFireContainer = null;
		this._shotHighlightContainer = null;

		super.destroy();
	}

}

export default FlameThrowerGun;