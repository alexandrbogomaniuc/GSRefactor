import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { AtlasSprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasConfig from '../../../config/AtlasConfig';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import ProfilingInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/profiling/ProfilingInfo';

class DeathFxAnimation extends Sprite
{
	
	static get EVENT_OUTRO_STARTED() 		{ return "EVENT_OUTRO_STARTED" };
	static get EVENT_ANIMATION_COMPLETED() 	{ return "EVENT_ANIMATION_COMPLETED" };
	static get EVENT_ON_DEATH_COIN_AWARD()	{ return "eventDeathFxCoinAward" }

	playIntro(aIsFastVariant_bl = false, aSkipOutro_bl = false)
	{
		this._playIntro(aIsFastVariant_bl, aSkipOutro_bl);
	}

	constructor()
	{
		super();
		this._fTripplePuff_sprt = null;
		this._fDeathPile_sprt = null;
		this._fDeathPileGold_sprt = null;
	}

	_playIntro(aIsFastVariant_bl = false, aSkipOutro_bl = false)
	{
		DeathFxAnimation.initTextures();

		if (!aSkipOutro_bl && !aIsFastVariant_bl)
		{
			this._initDeathPale();
		}

		let fallRocks_0 = this._playEffect('fallingDebris', PIXI.BLEND_MODES.SCREEN, 2);
		fallRocks_0.position.set(23.5, -120);

		fallRocks_0.on("changeframe", (e) => {
			if (fallRocks_0.currentFrame >= 2)
			{
				fallRocks_0.off('changeframe');
				let fallRocks_1 = this._playEffect('fallingDebris', PIXI.BLEND_MODES.SCREEN, 2);
				fallRocks_1.position.set(6.5, -122);
			}
		});

		if (!APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.LOWER))
		{
			let fireUpFx = this._playEffect('fireUpFx', PIXI.BLEND_MODES.SCREEN, 2);
			fireUpFx.position.set(0, -55)

			let smokeFx = this._playEffect('smokeFx', PIXI.BLEND_MODES.SCREEN, 2);
			smokeFx.position.set(0, -55);

			let smokePuff_5 = this._playEffect('smokePuff_5', PIXI.BLEND_MODES.SCREEN, 2);
			smokePuff_5.position.set(-0.5, -33.5);

			let puff = this._playEffect('smokePuff', PIXI.BLEND_MODES.ADD, 2*0.4);
			puff.position.set(0, -45);
			
			let groundHitFx = this._playEffect('groundHitFx', PIXI.BLEND_MODES.SCREEN, 2);
			groundHitFx.position.set(-12, -80 );
		}

		let outroSequence = [];
		let preOutroDuration = aIsFastVariant_bl ? 10 : 68;
		
		if (aSkipOutro_bl)
		{
			outroSequence.push({ tweens: [], duration: 10*FRAME_RATE, onfinish: () => { this.emit(DeathFxAnimation.EVENT_ON_DEATH_COIN_AWARD); } });
			preOutroDuration -= 10;
		}
		
		outroSequence.push({ tweens: [], duration: preOutroDuration*FRAME_RATE, onfinish: () => { this._playOutro(aSkipOutro_bl); } });
		Sequence.start(this, outroSequence);

	}

	_initDeathPale()
	{
		this._fDeathPile_sprt = this.addChild(APP.library.getSprite("death/death_pile"));
		this._fDeathPile_sprt.scale.set(1.6)
		this._fDeathPile_sprt.alpha = 0;

		if(!APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.LOWER))
		{
			this._fDeathPileGold_sprt = this.addChild(APP.library.getSprite("death/death_pile_gold"));
			this._fDeathPileGold_sprt.alpha = 0;
	
			let lSeqGoldFade_arr = [{tweens: [{prop: "alpha", to: 0}], duration: 10*FRAME_RATE, onfinish: () => {
				this.emit(DeathFxAnimation.EVENT_ON_DEATH_COIN_AWARD);
				this._fDeathPileGold_sprt && this._fDeathPileGold_sprt.destroy();
				this._fDeathPileGold_sprt = null;
				}
			}];
	
			let lSeqGlow_arr = [{tweens: [{prop: "alpha", to: 1}], duration: 3*FRAME_RATE, onfinish: () => {
					this._fDeathPile_sprt.alpha = 1;
					Sequence.start(this._fDeathPileGold_sprt, lSeqGoldFade_arr);
				}
			}];
	
			Sequence.start(this._fDeathPileGold_sprt, lSeqGlow_arr);
		}
		else
		{
			this.emit(DeathFxAnimation.EVENT_ON_DEATH_COIN_AWARD);
			this._fDeathPile_sprt.alpha = 1;
		}
	}

	_playOutro(aSkipOutro_bl = false)
	{
		this.emit(DeathFxAnimation.EVENT_OUTRO_STARTED);

		if (aSkipOutro_bl)
		{
			this._destroyAll();
		}
		else
		{
			if (!APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.LOWER))
			{
				let vortex = this._playEffect('vortex', PIXI.BLEND_MODES.SCREEN, 1, () => { this._playTripplePuff() });
				vortex.position.set(-7, 22);
				vortex.on('changeframe', (e) => {
					if (vortex.currentFrame >= 12)
					{
						vortex.off('changeframe');
						this._playTripplePuff(); // sometimes when FPS slows down, this might never be called, that's why it was added to _playEffect callback
					}
				});
			}
			else
			{
				this._destroyAll();
			}
		}
	}

	_playTripplePuff()
	{
		if (this._fTripplePuff_sprt) return; //already playing

		if (this._fDeathPile_sprt)
		{
			let lSeq_arr = [{tweens: [{prop: "alpha", to: 0}], duration: 5*FRAME_RATE, onfinish: () => {
					this._fDeathPile_sprt && this._fDeathPile_sprt.destroy();
					this._fDeathPile_sprt = null;
				}
			}];

			Sequence.start(this._fDeathPile_sprt, lSeq_arr);
		}

		let tripplePuff = this._playEffect('tripplePuff', PIXI.BLEND_MODES.ADD, 0.4*2, () => {this._destroyAll();});
		tripplePuff.position.set(-5, -27);
		this._fTripplePuff_sprt = tripplePuff;
	}

	_playEffect(texturesName, blendMode = PIXI.BLEND_MODES.NORMAL, scale = 1, callback = null)
	{
		let effect = this.addChild(new Sprite());
		effect.textures = DeathFxAnimation.textures[texturesName];
		effect.blendMode = blendMode;
		effect.scale.set(scale);
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

	_destroyAll()
	{
		this.emit(DeathFxAnimation.EVENT_ANIMATION_COMPLETED);
		this.destroy();
	}

	destroy()
	{
		if (this._fDeathPile_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fDeathPile_sprt));
			this._fDeathPile_sprt.destroy();
			this._fDeathPile_sprt = null;
		}

		if (this._fDeathPileGold_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fDeathPileGold_sprt));
			this._fDeathPileGold_sprt.destroy();
			this._fDeathPileGold_sprt = null;
		}

		Sequence.destroy(Sequence.findByTarget(this));
		this._fTripplePuff_sprt = null;
		super.destroy();
	}
}

DeathFxAnimation.textures = {
	fallingDebris: null,
	fireUpFx: null,
	groundHitFx: null,
	smokeFx: null, // die smoke
	smokePuff_5: null,
	smokePuff: null,
	vortex: null,
	tripplePuff: null
};

let imageNames = [],
	configs = [];

for (let i = 0; i <= 4; i ++)
{
	let name = 'death/death_fx-' + i;
	imageNames.push(name);
	configs.push(AtlasConfig.DeathFx[i]);
}

DeathFxAnimation.getGroundHitFxTextures = function()
{
	return DeathFxAnimation.textures["groundHitFx"];
}

DeathFxAnimation.setTexture = function (name, imageNames, configs, path)
{
	if (!DeathFxAnimation.textures[name])
	{
		DeathFxAnimation.textures[name] = [];

		if(!Array.isArray(imageNames)) imageNames = [imageNames];
		if(!Array.isArray(configs)) configs = [configs];

		let assets = [];
		imageNames.forEach(function(item){assets.push(APP.library.getAsset(item))});

		DeathFxAnimation.textures[name] = AtlasSprite.getFrames(assets, configs, path);
		DeathFxAnimation.textures[name].sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}
};

DeathFxAnimation.initSmokePuffTextures = function ()
{
	DeathFxAnimation.setTexture('smokePuff', imageNames, configs, 'smokepuff');
}

DeathFxAnimation.getSmokePuffTextures = function()
{
	return DeathFxAnimation.textures["smokePuff"];
}

DeathFxAnimation.initTextures = function()
{
	DeathFxAnimation.setTexture('fallingDebris', imageNames, configs, 'falling_debris');
	DeathFxAnimation.setTexture('fireUpFx', imageNames, configs, 'FireUpFx');
	DeathFxAnimation.setTexture('groundHitFx', imageNames, configs, 'GroundHitFx');
	DeathFxAnimation.initSmokePuffTextures();
	DeathFxAnimation.setTexture('smokeFx', imageNames, configs, 'Smoke FX');
	DeathFxAnimation.setTexture('smokePuff_5', imageNames, configs, 'smoke puff 5');
	DeathFxAnimation.setTexture('vortex', imageNames, configs, 'Vortex');
	DeathFxAnimation.setTexture('tripplePuff', imageNames, configs, 'tripple_puff');

}


export default DeathFxAnimation;