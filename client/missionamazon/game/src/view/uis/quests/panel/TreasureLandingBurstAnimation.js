import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';
import DeathFxAnimation from '../../../../main/animation/death/DeathFxAnimation';

class TreasureLandingBurstAnimation extends Sprite
{
	constructor()
	{
		super();

		DeathFxAnimation.initSmokePuffTextures();

		this._showAnimation();
	}

	_showAnimation()
	{
		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._playSmokeEffect(-6,  -5);
			this._playSmokeEffect(-6,  -22);
			this._playSmokeEffect(-6,  -47);
			this._playSmokeEffect(-11, -18);
			this._playSmokeEffect(-11, -34);
			this._playSmokeEffect(-11, -59);
			this._playSmokeEffect(-16, 9);
			this._playSmokeEffect(-16, -8);
			this._playSmokeEffect(-16, -34, () => { this._onSmokesAnimationCompleted(); });
		}
		
		this._showFlare();
	}

	_showFlare(callback = null)
	{
		let flare = this.addChild(APP.library.getSpriteFromAtlas("common/orange_flare_glowed"));
		flare.blendMode = PIXI.BLEND_MODES.ADD;
		flare.scale.set(2*0.58);
		flare.x = -35;

		flare.scaleTo(2*0.68, 8*FRAME_RATE, undefined, () => { 
																flare.scaleTo(0, 17*FRAME_RATE, undefined,  () => { 
																													this._onFlareAnimationCompleted();
																												 });
															 }
					);
	}

	_playSmokeEffect(aX_num, aY_num, callback = null)
	{
		let effect = this.addChild(new Sprite());
		effect.textures = DeathFxAnimation.textures['smokePuff'];
		effect.blendMode = PIXI.BLEND_MODES.SCREEN;
		effect.scale.set(2*0.4);
		effect.position.set(aX_num, aY_num);
		effect.animationSpeed = 48/60;
		effect.play();
		effect.once('animationend', (e) => {
			e.target.destroy();
			if (callback)
			{
				callback.call();
			}
		});
		return effect;
	}

	_onSmokesAnimationCompleted()
	{
		this._tryToCompleteAnimation();
	}

	_onFlareAnimationCompleted()
	{
		this._tryToCompleteAnimation();
	}

	_tryToCompleteAnimation()
	{
		if (!this.children || !this.children.length)
		{
			this.destroy();
		}
	}

	destroy()
	{
		super.destroy();
	}
}

export default TreasureLandingBurstAnimation;