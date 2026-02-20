import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite, AtlasSprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import { Sequence } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import AtlasConfig from './../../../config/AtlasConfig';

const FINAL_LIGHTS_CONFIG = [
	{
		"id": 1,
		"pos": { x: -16, y: 16 },
		"scale": { x: 1.05, y: 1.05 },
		"angle": 0
	},
	{
		"id": 2,
		"pos": { x: 8, y: 25 },
		"scale": { x: 1.05, y: 1.05 },
		"angle": 0
	},
	{
		"id": 1,
		"pos": { x: 32, y: -27 },
		"scale": { x: 1.05, y: 1.05 },
		"angle": 180
	},
	{
		"id": 2,
		"pos": { x: -44, y: -5 },
		"scale": { x: 1.05, y: 1.05 },
		"angle": 136
	},
	{
		"id": 3,
		"pos": { x: -35, y: 24 },
		"scale": { x: 1.05, y: 1.05 },
		"angle": 0
	},
	{
		"id": 3,
		"pos": { x: 30, y: 16 },
		"scale": { x: 1.05, y: 1.05 },
		"angle": -36
	},
	{
		"id": 3,
		"pos": { x: 1, y: -46 },
		"scale": { x: 1.05, y: 1.05 },
		"angle": -228
	}
];

let LIGHT_TEXTURE_1 = null;
let LIGHT_TEXTURE_2 = null;
let LIGHT_TEXTURE_3 = null;

function initLightTextures()
{
	if (!LIGHT_TEXTURE_1)
	{
		let lTextures_arr = AtlasSprite.getFrames(APP.library.getAsset("enemies/blue_orbs/fx_electrified_orb/end_light_1"), AtlasConfig.EndLight1, "");
		lTextures_arr.splice(5, 0, lTextures_arr[5]);

		LIGHT_TEXTURE_1 = lTextures_arr;
	}

	if (!LIGHT_TEXTURE_2)
	{
		let lTextures_arr = AtlasSprite.getFrames(APP.library.getAsset("enemies/blue_orbs/fx_electrified_orb/end_light_2"), AtlasConfig.EndLight2, "");
		lTextures_arr.splice(4, 0, lTextures_arr[4], lTextures_arr[4], lTextures_arr[4]);

		LIGHT_TEXTURE_2 = lTextures_arr;
	}

	if (!LIGHT_TEXTURE_3)
	{
		let lTextures_arr = AtlasSprite.getFrames(APP.library.getAsset("enemies/blue_orbs/fx_electrified_orb/end_light_3"), AtlasConfig.EndLight3, "");
		lTextures_arr.splice(1, 0, lTextures_arr[1], lTextures_arr[1], lTextures_arr[1], lTextures_arr[1], lTextures_arr[1]);

		LIGHT_TEXTURE_3 = lTextures_arr;
	}
}

class FinalOrbFXAnimation extends Sprite
{
	static get EVENT_ON_DISAPPEAR_ANIMATION_ENDED() { return "onDisappearAnimationEnded"; }

	startAnimation()
	{
		this._startAnimation();
	}

	onDeathRequired()
	{
		this._onDeathRequired();
	}

	constructor()
	{
		super();

		initLightTextures();

		this._fFinalLights_arr = [];
		this._fSequenceTargets_arr = [];
	}

	_startAnimation()
	{
		this._finalSmokeAnimation();
		this._finalLightningAnimation();
		this._finalFlareAnimation();
	}

	_finalSmokeAnimation()
	{
		let lSmoke_sprt = this.addChild(APP.library.getSprite("enemies/blue_orbs/fx_electrified_orb/smoke"));
		lSmoke_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		lSmoke_sprt.scale.set(0.4);

		let lSeq_arr = [
			{ tweens: [{ prop: 'scale.x', to: 1.3 }, { prop: 'scale.y', to: 1.3 }], duration: 2 * FRAME_RATE, ease: Easing.quadratic.easeIn },
			{ tweens: [], duration: 4 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 0 }, { prop: 'scale.y', to: 0 }], duration: 5 * FRAME_RATE, ease: Easing.quartic.easeOut },
			{
				tweens: [], duration: 3 * FRAME_RATE, ease: Easing.quartic.easeOut, onfinish: () =>
				{
					if (lSmoke_sprt)
					{
						this.removeChild(lSmoke_sprt);
						lSmoke_sprt.destroy();

						if (this._fSequenceTargets_arr)
						{
							let lId_num = this._fSequenceTargets_arr.indexOf(lSmoke_sprt);
							if (~lId_num)
							{
								this._fSequenceTargets_arr.splice(lId_num, 1);
							}
						}
					}

					this._onAnimationEnded();
				}
			}
		];

		this._fSequenceTargets_arr.push(lSmoke_sprt);
		Sequence.start(lSmoke_sprt, lSeq_arr);
	}

	_finalFlareAnimation()
	{
		const lFlare_sprt = this.addChild(APP.library.getSprite("enemies/blue_orbs/fx_electrified_orb/flare"));
		lFlare_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		const lScaleSeq_arr = [
			{ tweens: [{ prop: 'scale.x', to: 1.1 }, { prop: 'scale.y', to: 1.1 }], duration: 6 * FRAME_RATE },
			{
				tweens: [{ prop: 'scale.x', to: 0 }, { prop: 'scale.y', to: 0 }], duration: 6 * FRAME_RATE, ease: Easing.quartic.easeOut, onfinish: () =>
				{
					if (lFlare_sprt)
					{
						this.removeChild(lFlare_sprt);
						lFlare_sprt.destroy();

						if (this._fSequenceTargets_arr)
						{
							let lId_num = this._fSequenceTargets_arr.indexOf(lFlare_sprt);
							if (~lId_num)
							{
								this._fSequenceTargets_arr.splice(lId_num, 1);
							}
						}
					}

				}
			}
		];

		const lAlphaSeq_arr = [
			{ tweens: [], duration: 6 * FRAME_RATE },
			{ tweens: [{ prop: 'alpha', to: 0 }], duration: 3 * FRAME_RATE }
		];

		const lRatationSeq_arr = [
			{ tweens: [{ prop: 'rotation', to: Utils.gradToRad(34) }], duration: 9 * FRAME_RATE },
		];

		this._fSequenceTargets_arr.push(lFlare_sprt);

		Sequence.start(lFlare_sprt, lRatationSeq_arr);
		Sequence.start(lFlare_sprt, lScaleSeq_arr);
		Sequence.start(lFlare_sprt, lAlphaSeq_arr);
	}

	_finalLightningAnimation()
	{
		for (let i = 0; i < FINAL_LIGHTS_CONFIG.length; ++i)
		{
			this._generateFinalLight(FINAL_LIGHTS_CONFIG[i]);
		}
	}

	_generateFinalLight(aConfig_obj)
	{
		let lLight_sprt = this.addChild(new Sprite());
		lLight_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lLight_sprt.textures = this._getLightTexture(aConfig_obj.id);

		lLight_sprt.position.set(aConfig_obj.pos.x, aConfig_obj.pos.y);
		lLight_sprt.scale.set(aConfig_obj.scale.x, aConfig_obj.scale.y);
		lLight_sprt.rotation = Utils.gradToRad(aConfig_obj.angle);
		lLight_sprt.animationSpeed = 1.2;

		lLight_sprt.play();
		lLight_sprt.once('animationend', () =>
		{
			if (this._fFinalLights_arr)
			{
				let lId_num = this._fFinalLights_arr.indexOf(lLight_sprt);
				if (~lId_num)
				{
					this._fFinalLights_arr.splice(lId_num, 1);
				}
			}

			if (lLight_sprt)
			{
				this.removeChild(lLight_sprt);
				lLight_sprt.destroy();
			}
		}
		);

		this._fFinalLights_arr.push(lLight_sprt);
	}

	_getLightTexture(aId_num)
	{
		if (aId_num == 1) return LIGHT_TEXTURE_1;
		if (aId_num == 2) return LIGHT_TEXTURE_2;
		return LIGHT_TEXTURE_3;
	}

	_onAnimationEnded()
	{
		this.emit(FinalOrbFXAnimation.EVENT_ON_DISAPPEAR_ANIMATION_ENDED);
	}

	destroy()
	{
		while (this._fSequenceTargets_arr.length)
		{
			let lTarget_sprt = this._fSequenceTargets_arr.pop();
			this.removeChild(lTarget_sprt);
			Sequence.destroy(Sequence.findByTarget(lTarget_sprt));
			lTarget_sprt.destroy();
		}

		while (this._fFinalLights_arr.length)
		{
			this._fFinalLights_arr.pop().destroy();
		}

		super.destroy();

		this._fFinalLights_arr = null;
		this._fSequenceTargets_arr = null;
	}
}

export default FinalOrbFXAnimation;