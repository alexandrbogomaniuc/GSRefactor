import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import MissEffect from '../../missEffects/MissEffect';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { AtlasSprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasConfig from './../../../config/AtlasConfig';

let _groundsmoke_textures = null;
function _getGroundSmokeTextures()
{
	if (_groundsmoke_textures) return _groundsmoke_textures;

	_groundsmoke_textures = AtlasSprite.getFrames([APP.library.getAsset("weapons/InstantKill/groundsmoke")], [AtlasConfig.InstantKillGroundSmokeAtlas], "");
	return _groundsmoke_textures;
}

class InstantKillExplosion extends Sprite
{
	static get EVENT_ON_READY_FOR_DESTROY() { return 'EVENT_ON_READY_FOR_DESTROY'; }

	constructor(lensFlareParent)
	{
		super();

		this.lensFlareParent = lensFlareParent;
		this.lensFlare = null;
		this.energyMark = null;
		this.smoke = null;
		this.groundSmoke = null;
		this.glow = null;

		this.start();
	}

	start()
	{
		this.lensFlare = this.lensFlareParent.addChild(APP.library.getSpriteFromAtlas("common/lensflare"));
		this.lensFlare.scale.set(0);
		this.lensFlare.blendMode = PIXI.BLEND_MODES.ADD;

		var sequence = [{
							tweens: [],
							duration: 1.6 * 2 * 16.6,
							onfinish: () => {this.addHitEffect();}
						},
						{
							tweens: [
										{ prop: "scale.x", to: 1.94 },
										{ prop: "scale.y", to: 1.94 }
									],
							duration: 2.4 * 2 * 16.6
						},
						{
							tweens: [],
							duration: 2.4 * 2 * 16.6
						},
						{
							tweens: [
										{ prop: "scale.x", to: 0 },
										{ prop: "scale.y", to: 0 }
									],
							duration: 6.4 * 2 * 16.6,
							ease: Easing.sine.sineInOut,
							onfinish: () => { this.lensFlareParent.destroy(); }
						}
					]
		Sequence.start(this.lensFlare, sequence);
	}

	addHitEffect()
	{
		this.energyMark = this.addChild(APP.library.getSpriteFromAtlas("weapons/InstantKill/plasmaenergymark"));
		this.energyMark.blendMode = PIXI.BLEND_MODES.ADD;
		let energyMarkSequence = 	[	
							{
								tweens: [],
								duration: 7 * 2 * 16.6,
								onfinish: () => { this.energyMark.destroy(); }
							}
						];
		Sequence.start(this.energyMark, energyMarkSequence);

		if(APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this.smoke = this.addChild(new Sprite);
			this.smoke.textures = MissEffect.getSmokeTextures();
			this.smoke.blendMode = PIXI.BLEND_MODES.SCREEN;
			this.smoke.position.set(0, -14);
			this.smoke.scale.set(2.72);
			this.smoke.animationSpeed = 1;		

			this.smoke.play();
			this.smoke.once('animationend', (e) => {
				e.target.destroy();
				this.destroySuspicion();
			});

			this.groundSmoke = this.addChild(new Sprite);
			this.groundSmoke.textures = _getGroundSmokeTextures();
			this.groundSmoke.blendMode = PIXI.BLEND_MODES.SCREEN;
			this.groundSmoke.play();
			this.groundSmoke.once('animationend', (e) => {
				this.groundSmoke.destroy();
				this.destroySuspicion();
			});
		}

		this.glow = this.addChild(APP.library.getSpriteFromAtlas("weapons/InstantKill/glow"));
		this.glow.blendMode = PIXI.BLEND_MODES.ADD;
		this.glow.alpha = 0;
		let glowSequence = [
								{
									tweens: [{prop: "alpha", to: 1}],
									duration: 2.4*2*16.6
								},
								{
									tweens: [{prop: "alpha", to: 0}],
									duration: 7*2*16.6,
									onfinish: () => {
										this.glow.destroy();
										this.destroySuspicion();
									}
								}
							];
		Sequence.start(this.glow, glowSequence);
	}

	destroySuspicion()
	{
		if (this.children.length == 0)
		{
			this.destroy();
		}
	}

	destroy()
	{
		this.emit(InstantKillExplosion.EVENT_ON_READY_FOR_DESTROY);
		if (this.lensFlare)
		{
			Sequence.destroy(Sequence.findByTarget(this.lensFlare));
		}
		
		if (this.energyMark)
		{
			Sequence.destroy(Sequence.findByTarget(this.energyMark));
		}
		
		if (this.glow)
		{
			Sequence.destroy(Sequence.findByTarget(this.glow));
		}

		this.lensFlareParent && this.lensFlareParent.destroy();
		this.lensFlareParent = null;
		
		this.lensFlare = null;
		this.energyMark = null;
		this.smoke = null;
		this.groundSmoke = null;
		this.glow = null;

		super.destroy();
	}
}

export default InstantKillExplosion;