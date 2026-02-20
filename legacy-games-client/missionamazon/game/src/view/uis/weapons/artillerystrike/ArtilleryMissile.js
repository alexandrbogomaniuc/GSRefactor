import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AtlasSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../../config/AtlasConfig';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';

class ArtilleryMissile extends Sprite {

	static get EVENT_ON_ANIMATION_END() { return 'EVENT_ON_ANIMATION_END'; }

	i_hideMissile()
	{
		this._hideMissile();
	}

	constructor()
	{
		super();

		APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater && ArtilleryMissile.initTextures();

		ArtilleryMissile.initFireTexture();

		this._fMissile_sprt = null;
		this._fSmokeTrail_sprt = null;
		this._fSequences = [];
		
		this._fFireloop_sprt = null;

		this._createView();	
	}

	_createView()
	{
		let missile = APP.library.getSpriteFromAtlas('weapons/ArtilleryStrike/missile');
		missile.anchor.set(0.5, 0);
		this.addChild(missile);
		this._initFireLoop();

		let lMissileBounds_obj = missile.getBounds();
		this._fFireloop_sprt.y = lMissileBounds_obj.height + 40;

		this._fMissile_sprt = missile;

		APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater && this._addSmokeTrail(this._fFireloop_sprt.x + 3, this._fFireloop_sprt.y - 20);
	}

	_initFireLoop()
	{
		let fireloop = this.addChild(new Sprite);
		fireloop.textures = ArtilleryMissile.textures.fireloop;
		fireloop.blendMode = PIXI.BLEND_MODES.SCREEN;

		fireloop.rotation = Math.PI;
		fireloop.scale.set(0.24, 0.82);
		fireloop.alpha = 1;
		fireloop.loop = true;
		fireloop.play();

		this._fFireloop_sprt = fireloop;
	}

	_addSmokeTrail(x,  y)
	{
		let smokeTrail = this.addChild(new Sprite);
		smokeTrail.position.set(x, y);
		smokeTrail.alpha = 1;
		smokeTrail.scale.set(2);
		smokeTrail.textures = ArtilleryMissile.textures.smokeTrail;
		smokeTrail.anchor.set(291/540, 740/960);
		smokeTrail.rotation = -Math.PI;
		let mask = new PIXI.Graphics();
		this.addChild(smokeTrail);
		smokeTrail.stop();
		smokeTrail.once('animationend', _=> {
			seq && seq.destructor();
			seq = null;
			smokeTrail.destroy();
			this._fSmokeTrail_sprt = null;
			this._onAnimationEndSuspicion();
		});
		let sequence = [
			{
				tweens: [],
				duration: 6*FRAME_RATE,
				onfinish: ()=>{
					smokeTrail.play();
				}
			},
			{
				tweens: [],
				duration: 4*FRAME_RATE
			},
			{
				tweens: [
					{prop: 'position.y', to: y - 100}
				],
				duration: 20 * FRAME_RATE
			},
		]
		let seq = Sequence.start(smokeTrail, sequence);
		this._fSequences.push(seq);
		this._fSmokeTrail_sprt = smokeTrail;
		
	}

	_onAnimationEndSuspicion()
	{
		if (!this._fSmokeTrail_sprt && !this._fMissile_sprt)
		{
			this._onAnimationEnd();
		}
	}

	_onAnimationEnd()
	{
		this.emit(ArtilleryMissile.EVENT_ON_ANIMATION_END);
	}

	_hideMissile()
	{
		this._fMissile_sprt.destroy();
		this._fFireloop_sprt.destroy();
		
		this._fFireloop_sprt = null;
		this._fMissile_sprt = null;

		this._onAnimationEndSuspicion();
	}

	destroy()
	{
		this.removeAllListeners();
		this._fSequences.forEach(function(seq){seq && seq.destructor();});
		this._fSequences = [];

		this._fMissile_sprt && this._fMissile_sprt.destroy();
		this._fSmokeTrail_sprt && this._fSmokeTrail_sprt.destroy();
		this._fFireloop_sprt && this._fFireloop_sprt.destroy();

		super.destroy();
	}
}

ArtilleryMissile.textures = {
	smokeTrail: null,
	fireloop: null
};

ArtilleryMissile.setTexture = function (name, imageNames, configs, path) {
	if(!ArtilleryMissile.textures[name]){
		ArtilleryMissile.textures[name] = [];

		if(!Array.isArray(imageNames)) imageNames = [imageNames];
		if(!Array.isArray(configs)) configs = [configs];

		let assets = [];
		imageNames.forEach(function(item){assets.push(APP.library.getAsset(item))});

		ArtilleryMissile.textures[name] = AtlasSprite.getFrames(assets, configs, path);
		ArtilleryMissile.textures[name].sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}
};

ArtilleryMissile.initTextures = function(){
	ArtilleryMissile.setTexture('smokeTrail', 'weapons/ArtilleryStrike/smoke_trail', AtlasConfig.SmokeTrail, '');
};

ArtilleryMissile.initFireTexture = function()
{
	ArtilleryMissile.setTexture('fireloop', 'weapons/ArtilleryStrike/fire_loop', AtlasConfig.FireLoop, '');
}

export default ArtilleryMissile;