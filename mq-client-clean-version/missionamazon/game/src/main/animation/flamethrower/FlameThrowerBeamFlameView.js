import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import AtlasConfig from '../../../config/AtlasConfig';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';

class FlameThrowerBeamFlameView extends Sprite 
{
	static get EVENT_ON_FLAME_TARGET_ACHIEVED()	{ return 'EVENT_ON_FLAME_TARGET_ACHIEVED'; }
	static get EVENT_ON_FLAME_TARGET_ANIMATION_COMPLETED()	{ return 'EVENT_ON_FLAME_TARGET_ANIMATION_COMPLETED'; }

	static get BASE_BEAM_LENGTH () 
	{
		return 235;
	}

	constructor()
	{
		super();

		this._flameSprites_arr = [];
		
		this._initView();
	}

	destroy()
	{
		while (this._flameSprites_arr && this._flameSprites_arr.length)
		{
			let lFlame_sprt = this._flameSprites_arr.pop();
			lFlame_sprt.destroy();
		}
		this._flameSprites_arr = null;

		Sequence.destroy(Sequence.findByTarget(this));

		super.destroy();
	}

	startAnimation()
	{
		this._startAnimation();
	}

	_initView()
	{
		let flameSmoke_sprt = this._addFlameSprite(FlameThrowerBeamFlameView.getFlameTextures(), PIXI.BLEND_MODES.NORMAL);
		let blurFilter = new PIXI.filters.BlurFilter();
   		blurFilter.blurY = 2;
   		flameSmoke_sprt.tint = 0x000000;
		flameSmoke_sprt.filters = [blurFilter];
		flameSmoke_sprt.rotation = Utils.gradToRad(-6);
		flameSmoke_sprt.alpha = 0.16;
		flameSmoke_sprt.animationSpeed = 24/60;

		this._addFlameSprite(FlameThrowerBeamFlameView.getFlameTextures(), PIXI.BLEND_MODES.SCREEN, 24);
		this._addFlameSprite(FlameThrowerBeamFlameView.getFlameBaseFireTextures(), PIXI.BLEND_MODES.SCREEN);
		this._addFlameSprite(FlameThrowerBeamFlameView.getFlameCoverFireTextures(), PIXI.BLEND_MODES.NORMAL);
	}

	_addFlameSprite(textures, blendMode, framerate=30)
	{
		let lFlame_sprt = this.addChild(new Sprite());
		lFlame_sprt.textures = textures;
		lFlame_sprt.animationSpeed = framerate/60;
		lFlame_sprt.anchor.set(0, 0.5);
		lFlame_sprt.blendMode = blendMode;
		lFlame_sprt.scale.set(2);

		lFlame_sprt.on('animationend', () => {
			lFlame_sprt.stop();
		});

		this._flameSprites_arr.push(lFlame_sprt);

		return lFlame_sprt;
	}

	_startAnimation()
	{
		this.pivot.set(0, 0);
		this.scale.x = 0;

		let seq = [
			{
				tweens: [],
				duration: 1*2*16.7,
				onfinish: () => {
					this._startFlamePlaying();
				}
			},
			{
				tweens: [
					{ prop: 'scale.x', to: 1 }
				],
				duration: 5*2*16.7,
				onfinish: () => {
					this._onTargetAchieved();
				}
			},
			{
				tweens: [
				],
				duration: 22*2*16.7,
				onfinish: () => {
					this.pivot.set(FlameThrowerBeamFlameView.BASE_BEAM_LENGTH, 0);
					this.position.x += FlameThrowerBeamFlameView.BASE_BEAM_LENGTH;
					this._onTargetAnimationCompleted();
				}
			},
			{
				tweens: [
					{ prop: 'scale.x', to: 0}
				],
				duration: 3*2*16.7,
				ease: Easing.circular.sineIn,
				onfinish: ()=> {
					this.destroy();
				}
			}
		];
		Sequence.start(this, seq);
	}

	_startFlamePlaying()
	{
		for (let i=0; i<this._flameSprites_arr.length; i++)
		{
			let lFlame_sprt = this._flameSprites_arr[i];
			lFlame_sprt.play();
		}
	}

	_onTargetAchieved()
	{
		this.emit(FlameThrowerBeamFlameView.EVENT_ON_FLAME_TARGET_ACHIEVED);
	}

	_onTargetAnimationCompleted()
	{
		this.emit(FlameThrowerBeamFlameView.EVENT_ON_FLAME_TARGET_ANIMATION_COMPLETED);
	}
}

FlameThrowerBeamFlameView.getFlameTextures = function ()
{
	if (!FlameThrowerBeamFlameView.flameTextures)
	{
		FlameThrowerBeamFlameView.setFlameTextures();
	}
	return FlameThrowerBeamFlameView.flameTextures;
}

FlameThrowerBeamFlameView.setFlameTextures = function ()
{
	FlameThrowerBeamFlameView.flameTextures = AtlasSprite.getFrames(APP.library.getAsset("weapons/FlameThrower/flame_base"), AtlasConfig.FlameThrowerFlameBase, "");
}

FlameThrowerBeamFlameView.getFlameBaseFireTextures = function ()
{
	if (!FlameThrowerBeamFlameView.flameBaseFireTextures)
	{
		FlameThrowerBeamFlameView.setFlameBaseFireTextures();
	}
	return FlameThrowerBeamFlameView.flameBaseFireTextures;
}

FlameThrowerBeamFlameView.setFlameBaseFireTextures = function ()
{
	FlameThrowerBeamFlameView.flameBaseFireTextures = AtlasSprite.getFrames(APP.library.getAsset("weapons/FlameThrower/flame_base_fire"), AtlasConfig.FlameThrowerFlameBaseFire, "");
}

FlameThrowerBeamFlameView.getFlameCoverFireTextures = function ()
{
	if (!FlameThrowerBeamFlameView.flameCoverFireTextures)
	{
		FlameThrowerBeamFlameView.setFlameCoverFireTextures();
	}
	return FlameThrowerBeamFlameView.flameCoverFireTextures;
}

FlameThrowerBeamFlameView.setFlameCoverFireTextures = function ()
{
	FlameThrowerBeamFlameView.flameCoverFireTextures = AtlasSprite.getFrames(APP.library.getAsset("weapons/FlameThrower/flame_cover_fire"), AtlasConfig.FlameThrowerFlameCoverFire, "");
}

export default FlameThrowerBeamFlameView;