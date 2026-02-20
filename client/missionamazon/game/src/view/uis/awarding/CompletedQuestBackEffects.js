import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../config/AtlasConfig';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

let sparkles_textures = null;
function _initSparklesTextures()
{
	if (!sparkles_textures)
	{
		sparkles_textures = AtlasSprite.getFrames([APP.library.getAsset("treasures/sparkles_0"), APP.library.getAsset("treasures/sparkles_1")], [AtlasConfig.Sparkles0, AtlasConfig.Sparkles1], "");
	}
}

const SPARKLES_CONFIG = [
	{
		rotation: 30,
	},
	{
		rotation: 200,
	},
	{
		rotation: 108,
	},
	{
		rotation: 30,
	},
	{
		rotation: 200,
	}
];

class CompletedQuestBackEffects extends Sprite
{
	constructor()
	{
		super();

		_initSparklesTextures();

		this._fSparkles_spr_arr = [];
		this._fCorcles_spr_arr = [];
		this._fFlare_spr = null;
		this._fSecondCircleTimet_t = null;

		this._addEffects();
	}

	_addEffects()
	{
		this._addSparkles();
		this._addFlare();
		this._addCircles();
	}

	_addSparkles()
	{
		for (let i = 0; i < SPARKLES_CONFIG.length; i++)
		{
			const lSparkles_spr = this.addChild(new Sprite());
			lSparkles_spr.textures = sparkles_textures;
			lSparkles_spr.blendMode = PIXI.BLEND_MODES.ADD;
			lSparkles_spr.pivot.set(348, 0);
			lSparkles_spr.rotation = Utils.gradToRad(SPARKLES_CONFIG[i].rotation);
			lSparkles_spr.animationSpeed = 0.5;
			lSparkles_spr.once('animationend', () => { lSparkles_spr && lSparkles_spr.destroy() });
			lSparkles_spr.play();

			this._fSparkles_spr_arr.push(lSparkles_spr);
		}
	}

	_addFlare()
	{
		this._fFlare_spr = this.addChild(APP.library.getSpriteFromAtlas("treasures/flare"));
		this._fFlare_spr.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fFlare_spr.scale.set(0.47);
		this._fFlare_spr.rotation = Utils.gradToRad(10);

		let lFlareScaleSeq_arr = [
			{ tweens: [{ prop: 'scale.x', to: 0 }, { prop: 'scale.y', to: 0 }], duration: 15 * FRAME_RATE },
		];
		Sequence.start(this._fFlare_spr, lFlareScaleSeq_arr);

		let lFlareRotationSeq_arr = [
			{ tweens: [{ prop: 'rotation', to: Utils.gradToRad(32) }], duration: 14 * FRAME_RATE },
		];
		Sequence.start(this._fFlare_spr, lFlareRotationSeq_arr);
	}

	_addCircles()
	{
		this._animateCircle();
		this._fSecondCircleTimet_t = new Timer(this._animateCircle.bind(this), 2 * FRAME_RATE);
	}

	_animateCircle()
	{
		const lCircle_spr = this.addChild(APP.library.getSpriteFromAtlas("treasures/circle"));
		lCircle_spr.scale.set(0.13);
		lCircle_spr.blendMode = PIXI.BLEND_MODES.SCREEN;

		let lCircleScaleSeq_arr = [
			{ tweens: [{ prop: 'scale.x', to: 0.76 }, { prop: 'scale.y', to: 0.76 }], duration: 7 * FRAME_RATE },
		];
		Sequence.start(lCircle_spr, lCircleScaleSeq_arr);

		let lCircleAlphaSeq_arr = [
			{ tweens: [{ prop: 'alpha', to: 0 }], duration: 7 * FRAME_RATE },
		];
		Sequence.start(lCircle_spr, lCircleAlphaSeq_arr);

		this._fCorcles_spr_arr.push(lCircle_spr);
	}

	destroy()
	{
		this._fFlare_spr && Sequence.destroy(Sequence.findByTarget(this._fFlare_spr));
		this._fSecondCircleTimet_t && this._fSecondCircleTimet_t.destructor();
		this._fSecondCircleTimet_t = null;

		this._fCorcles_spr_arr && this._fCorcles_spr_arr.forEach(l_spr =>
		{
			Sequence.destroy(Sequence.findByTarget(l_spr));
			l_spr.destroy();
		});
		this._fCorcles_spr_arr = null;

		super.destroy();

		this._fSparkles_spr_arr.forEach(l_spr => l_spr.destroy());
		this._fSparkles_spr_arr = null;
		this._fFlare_spr = null;
	}
}

export default CompletedQuestBackEffects;