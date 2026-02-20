import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TorchFxAnimation from '../TorchFxAnimation';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { MUZZLE_DISTANCE, MUZZLE_LENGTH } from './FlameThrowerGun';

class FlameThrowerGunShotMuzzleEffects extends Sprite 
{
	static get EVENT_ON_ANIMATION_COMPLETED()	{ return 'EVENT_ON_ANIMATION_COMPLETED'; }

	startAnimation()
	{
		this._startAnimation();
	}

	constructor()
	{
		super();

		this._shotMuzzle = null;
		this._torch = null;

		this._initView();
	}

	_initView() 
	{
		TorchFxAnimation.initTextures();
	}

	_startAnimation()
	{
		this._startMuzzleHighlightEffects();
		this._addFirstTorch();
	}

	_addFirstTorch()
	{
		let lTorch_sprt = this._torch = this._createTorch(20);
		lTorch_sprt.scale.set(0.4, 0.62);

		let sequence = [
			{
				tweens: [
					{prop: 'scale.x', to: 0.46},
					{prop: 'scale.y', to: 1.15}
				],
				duration: 15*2*16.7,
				onfinish: (e) => {
					Sequence.destroy(Sequence.findByTarget(lTorch_sprt));
					lTorch_sprt.destroy();

					this._addSecondTorch();
				}
			}
		]

		lTorch_sprt.play();
		Sequence.start(lTorch_sprt, sequence);
	}

	_addSecondTorch()
	{
		let lTorch_sprt = this._torch = this._createTorch();
		lTorch_sprt.scale.set(0.46, 1.15);

		let sequence = [
			{
				tweens: [
					{prop: 'scale.y', to: 0.93}
				],
				duration: 11*2*16.7
			},
			{
				tweens: [
					{prop: 'scale.y', to: 0}
				],
				duration: 5*2*16.7,
				onfinish: (e) => {
					Sequence.destroy(Sequence.findByTarget(lTorch_sprt));
					lTorch_sprt.destroy();
				}
			}
		]

		lTorch_sprt.play();
		Sequence.start(lTorch_sprt, sequence);
	}

	_createTorch(totalFrames = undefined)
	{
		let lTorch_sprt = this.addChild(Sprite.createMultiframesSprite(TorchFxAnimation.textures.torch, 0, totalFrames));
		lTorch_sprt.anchor.set(0.5, 0.7);
		lTorch_sprt.animationSpeed = 30/60;
		lTorch_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		lTorch_sprt.position.set(0, -(MUZZLE_DISTANCE+MUZZLE_LENGTH));

		return lTorch_sprt;
	}

	_startMuzzleHighlightEffects()
	{
		let lFlameThrowerShotMuzzle_sprt = this._shotMuzzle = this.addChild(APP.library.getSpriteFromAtlas('weapons/FlameThrower/flamethrower_muzzle_shot'));
		lFlameThrowerShotMuzzle_sprt.anchor.y = 155/249;
		lFlameThrowerShotMuzzle_sprt.alpha = 1;

		let sequence = [
			{
				tweens: [],
				duration: 28*2*16.7
			},
			{
				tweens: [
					{prop: 'alpha', to: 0}
				],
				duration: 6*2*16.7,
				onfinish: (e) => {
					this._onMuzzleHighlightEffectCompleted();
				}
			}
		]
		Sequence.start(lFlameThrowerShotMuzzle_sprt, sequence);
	}

	_onMuzzleHighlightEffectCompleted()
	{
		this._onAnimationCompleted();
	}

	_onAnimationCompleted()
	{
		this.emit(FlameThrowerGunShotMuzzleEffects.EVENT_ON_ANIMATION_COMPLETED);
	}

	destroy()
	{
		if (this._shotMuzzle)
		{
			Sequence.destroy(Sequence.findByTarget(this._shotMuzzle));
			this._shotMuzzle = null;
		}
		
		if (this._torch)
		{
			Sequence.destroy(Sequence.findByTarget(this._torch));
			this._torch = null;
		}
		
		super.destroy();
	}

}

export default FlameThrowerGunShotMuzzleEffects;