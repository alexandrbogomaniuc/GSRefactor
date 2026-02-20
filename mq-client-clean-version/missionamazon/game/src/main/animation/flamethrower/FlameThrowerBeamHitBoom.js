import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import AtlasConfig from '../../../config/AtlasConfig';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';

class FlameThrowerBeamHitBoom extends Sprite 
{
	constructor()
	{
		super();
		
		this._hitBoom_sprt = null;

		this._initView();
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this));

		super.destroy();
	}

	startAnimation()
	{
		this._startAnimation();
	}

	_initView()
	{
		let lFlameHitBoom_sprt = this._hitBoom_sprt = this.addChild(new Sprite());
		lFlameHitBoom_sprt.textures = FlameThrowerBeamHitBoom.getHitBoomTextures();
		lFlameHitBoom_sprt.animationSpeed = 24/60;
		lFlameHitBoom_sprt.anchor.set(0.07, 0.5);
		lFlameHitBoom_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lFlameHitBoom_sprt.scale.set(2.5);
		
		lFlameHitBoom_sprt.on('animationend', () => {
			lFlameHitBoom_sprt.stop();
			this.destroy();
		});

		lFlameHitBoom_sprt.visible = false;
	}

	_startAnimation()
	{
		let seq = [
			{
				tweens: [],
				duration: 2*2*16.7,
				onfinish: ()=> {
					this._hitBoom_sprt.visible = true;
					this._hitBoom_sprt.play();
				}
			},
			{
				tweens: [],
				duration: 22*2*16.7
			},
			{
				tweens: [ { prop: 'alpha', to: 0} ],
				duration: 10*2*16.7,
				onfinish: ()=> {
					this.destroy();
				}
			}
		];
		Sequence.start(this, seq);
	}
}

FlameThrowerBeamHitBoom.getHitBoomTextures = function ()
{
	if (!FlameThrowerBeamHitBoom.hitBoomTextures)
	{
		FlameThrowerBeamHitBoom.setHitBoomTextures();
	}
	return FlameThrowerBeamHitBoom.hitBoomTextures;
}

FlameThrowerBeamHitBoom.setHitBoomTextures = function ()
{
	FlameThrowerBeamHitBoom.hitBoomTextures = AtlasSprite.getFrames(APP.library.getAsset("weapons/FlameThrower/hit_boom"), AtlasConfig.FlameThrowerFlameHitBoom, "");
}

export default FlameThrowerBeamHitBoom;