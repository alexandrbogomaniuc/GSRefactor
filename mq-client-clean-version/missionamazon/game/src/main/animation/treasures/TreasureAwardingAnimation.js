import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { FRAME_RATE, TREASURES_NAMES } from '../../../../../shared/src/CommonConstants';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { AtlasSprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasConfig from './../../../config/AtlasConfig';

let gems_textures = null;
function _initGemsTextures()
{
	if (!gems_textures)
	{
		gems_textures = AtlasSprite.getFrames(APP.library.getAsset("treasures/gems"), AtlasConfig.Gems, "");
	}
}

class TreasureAwardingAnimation extends Sprite
{
	static get EVENT_ON_ANIMATION_ENDED()			{ return "onAnimationEnded"; }
	static get EVENT_ON_TREASURE_GEM_DROP()			{ return "onTreasureAwardingAnimation"; }

	get id()
	{
		return this._fTreasureId_num;
	}

	startAnimation()
	{
		this._startAnimation();
	}

	constructor(aTreasureId_num)
	{
		super();

		_initGemsTextures();

		this._fTreasureId_num = aTreasureId_num;
		this._fGameName_str = this._getGemNameById(aTreasureId_num);
		this._fFlare_spr = null;
		this._fGemContainer_spr = null;
		this._fMainGem_spr = null;
		this._fColorBlendGem_spr = null;
		this._fGlowGem_spr = null;
		this._fLightSweep_sprt = null;
		this._fSmoke_spr = null;
		this._fGemFlare_spr = null;
		this._fAnimationSequences_s_arr = [];
	}

	_addSequence(aTarget_spr, aSeq_arr)
	{
		const l_seq = Sequence.start(aTarget_spr, aSeq_arr);
		l_seq.once(Sequence.EVENT_ON_SEQUENCE_PLAYING_COMPLETED, this._checkAnimationFinished, this);
		this._fAnimationSequences_s_arr.push(l_seq);
	}

	_getGemNameById(aTreasureId_num)
	{
		return TREASURES_NAMES[aTreasureId_num];
	}

	_getGemsTextureByName(aName_str)
	{
		_initGemsTextures();

		return Utils.getTexture(gems_textures, aName_str);
	}

	_startAnimation()
	{
		this._startFlareAnimation();
		this._startGemAnimation();
		this._startSmokeAnimation();
	}

	_startFlareAnimation()
	{
		this._fFlare_spr = this.addChild(APP.library.getSpriteFromAtlas("treasures/flare"));
		this._fFlare_spr.blendMode = PIXI.BLEND_MODES.ADD;
		this._fFlare_spr.scale.set(0.2, 3.52);
		this._fFlare_spr.visible = false;
		this._fFlare_spr.rotation = Utils.gradToRad(50);

		let lFlareScaleSeq_arr = [
			{ tweens: [], duration: 6 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 0.43 }, { prop: 'scale.y', to: 0.43 }], duration: 4 * FRAME_RATE, onfinish: () => { this._fFlare_spr.visible = true; } },
			{ tweens: [{ prop: 'scale.x', to: 0.05 }, { prop: 'scale.y', to: 0.05 }], duration: 5 * FRAME_RATE, ease: Easing.back.easeIn },
			{ tweens: [{ prop: 'scale.x', to: 0.63 }, { prop: 'scale.y', to: 0.7 }], duration: 5 * FRAME_RATE, ease: Easing.back.easeIn },
			{ tweens: [{ prop: 'scale.x', to: 0 }, { prop: 'scale.y', to: 0 }], duration: 4 * FRAME_RATE },
		];
		this._addSequence(this._fFlare_spr, lFlareScaleSeq_arr);

		let lFlareRotationSeq_arr = [
			{ tweens: [], duration: 6 * FRAME_RATE },
			{ tweens: [{ prop: 'rotation', to: Utils.gradToRad(-10) }], duration: 14 * FRAME_RATE },
		];
		this._addSequence(this._fFlare_spr, lFlareRotationSeq_arr);
	}

	_startGemAnimation()
	{
		this._fGemContainer_spr = this.addChild(new Sprite());
		this._fGemContainer_spr.scale.set(0);

		this._animateMainGem();
		this._animateColorBlendGem();
		this._animateLightSweep();
		this._animateGlowGem();
		this._animateGemFlare();

		const lGemContainerScaleSeq_arr = [
			{ tweens: [], duration: 6 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 0.97 }, { prop: 'scale.y', to: 0.97 }], duration: 4 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 1.8 }, { prop: 'scale.y', to: 1.8 }], duration: 3 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 0.72 }, { prop: 'scale.y', to: 0.72 }], duration: 7 * FRAME_RATE, onfinish: () => 
		{this.emit(TreasureAwardingAnimation.EVENT_ON_TREASURE_GEM_DROP)}},
			{ tweens: [{ prop: 'scale.x', to: 1 }, { prop: 'scale.y', to: 1 }], duration: 9 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 1.1 }, { prop: 'scale.y', to: 1.1 }], duration: 20 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 0 }, { prop: 'scale.y', to: 0 }], duration: 12 * FRAME_RATE },
		];

		this._addSequence(this._fGemContainer_spr, lGemContainerScaleSeq_arr);

		const lGemContainerRotationSeq_arr = [
			{ tweens: [], duration: 46 * FRAME_RATE },
			{ tweens: [{ prop: 'rotation', to: Utils.gradToRad(5) }], duration: 6 * FRAME_RATE },
			{ tweens: [{ prop: 'rotation', to: Utils.gradToRad(0) }], duration: 8 * FRAME_RATE },
		];
		this._addSequence(this._fGemContainer_spr, lGemContainerRotationSeq_arr);
	}

	_animateMainGem()
	{
		if (!this._fGemContainer_spr) return;

		this._fMainGem_spr = this._fGemContainer_spr.addChild(new Sprite());
		this._fMainGem_spr.texture = this._getGemsTextureByName(this._fGameName_str);
	}

	_animateColorBlendGem()
	{
		if (!this._fGemContainer_spr) return;

		this._fColorBlendGem_spr = this._fGemContainer_spr.addChild(new Sprite());
		this._fColorBlendGem_spr.texture = this._getGemsTextureByName(this._fGameName_str + "_color_blend");
		this._fColorBlendGem_spr.alpha = 0.1 * 1.3;

		let lColorBlendGemAlphaSeq_arr = [
			{ tweens: [], duration: 8 * FRAME_RATE },
			{ tweens: [{ prop: 'alpha', to: 0.6 * 1.3 }], duration: 2 * FRAME_RATE },
			{ tweens: [{ prop: 'alpha', to: 0.11 * 1.3 }], duration: 5 * FRAME_RATE },
			{ tweens: [{ prop: 'alpha', to: 0.6 * 1.3 }], duration: 2 * FRAME_RATE },
			{ tweens: [{ prop: 'alpha', to: 0 }], duration: 10 * FRAME_RATE },
		];
		this._addSequence(this._fColorBlendGem_spr, lColorBlendGemAlphaSeq_arr);
	}

	_animateLightSweep()
	{
		if (!this._fGemContainer_spr) return;

		this._fLightSweep_sprt = this._fGemContainer_spr.addChild(APP.library.getSpriteFromAtlas("treasures/light_sweep"));

		const lMask_spr = this._fGemContainer_spr.addChild(new Sprite());
		lMask_spr.texture = this._getGemsTextureByName(this._fGameName_str + "_mask");
		this._fLightSweep_sprt.mask = lMask_spr;
		this._fLightSweep_sprt.position.x = -70;

		let lLightSweepPositionSeq_arr = [
			{ tweens: [], duration: 17 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: 70 },], duration: 43 * FRAME_RATE }
		];
		this._addSequence(this._fLightSweep_sprt, lLightSweepPositionSeq_arr);
	}

	_animateGlowGem()
	{
		if (!this._fGemContainer_spr) return;

		this._fGlowGem_spr = this._fGemContainer_spr.addChild(new Sprite());
		this._fGlowGem_spr.texture = this._getGemsTextureByName(this._fGameName_str + "_glow");
		this._fGlowGem_spr.alpha = 0.4;

		let lGlowGemAlphaSeq_arr = [
			{ tweens: [], duration: 9 * FRAME_RATE },
			{ tweens: [{ prop: 'alpha', to: 0 }], duration: 11 * FRAME_RATE },
		];
		this._addSequence(this._fGlowGem_spr, lGlowGemAlphaSeq_arr);
	}

	_animateGemFlare()
	{
		if (!this._fGemContainer_spr) return;

		this._fGemFlare_spr = this._fGemContainer_spr.addChild(APP.library.getSpriteFromAtlas("treasures/gem_flare"));
		this._fGemFlare_spr.position = this._getGemFlarePositionById();
		this._fGemFlare_spr.blendMode = PIXI.BLEND_MODES.ADD;
		this._fGemFlare_spr.scale.set(0);

		const lGemFlareScaleSeq_arr = [
			{ tweens: [], duration: 12 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 1.33 }, { prop: 'scale.y', to: 1.33 }], duration: 5 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 0.63 }, { prop: 'scale.y', to: 0.63 }], duration: 7 * FRAME_RATE },
		];
		this._addSequence(this._fGemFlare_spr, lGemFlareScaleSeq_arr);

		const lGemFlareRotationSeq_arr = [
			{ tweens: [{ prop: 'rotation', to: Utils.gradToRad(215) }], duration: 60 * FRAME_RATE },
		];
		this._addSequence(this._fGemFlare_spr, lGemFlareRotationSeq_arr);
	}

	_getGemFlarePositionById()
	{
		switch (this._fTreasureId_num)
		{
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
				return { x: 4, y: -12 };
		}

		return { x: 0, y: 0 };
	}

	_startSmokeAnimation()
	{
		this._fSmoke_spr = this.addChild(APP.library.getSpriteFromAtlas("treasures/smoke"));
		this._fSmoke_spr.blendMode = PIXI.BLEND_MODES.ADD;
		this._fSmoke_spr.scale.set(0.2);
		this._fSmoke_spr.visible = false;

		let lSmokeScaleSeq_arr = [
			{ tweens: [], duration: 5 * FRAME_RATE, onfinish: () => { this._fSmoke_spr.visible = true; } },
			{ tweens: [{ prop: 'scale.x', to: 1.1 }, { prop: 'scale.y', to: 1.1 }], duration: 17 * FRAME_RATE },
		];
		this._addSequence(this._fSmoke_spr, lSmokeScaleSeq_arr);

		let lSmokeAlphaSeq_arr = [
			{ tweens: [], duration: 9 * FRAME_RATE },
			{ tweens: [{ prop: 'alpha', to: 0 }], duration: 13 * FRAME_RATE },
		];
		this._addSequence(this._fSmoke_spr, lSmokeAlphaSeq_arr);
	}

	_checkAnimationFinished()
	{
		if (this._isAllSequenceFinish())
		{
			this.emit(TreasureAwardingAnimation.EVENT_ON_ANIMATION_ENDED);
		}
	}

	_isAllSequenceFinish()
	{
		if (this._fAnimationSequences_s_arr && this._fAnimationSequences_s_arr.length)
		{
			for (let i = 0; i < this._fAnimationSequences_s_arr.length; i++)
			{
				if (!this._fAnimationSequences_s_arr[i].ended)
				{
					return false;
				}
			}
		}

		return true;
	}

	destroy()
	{
		this._fFlare_spr && Sequence.destroy(Sequence.findByTarget(this._fFlare_spr));
		this._fGemContainer_spr && Sequence.destroy(Sequence.findByTarget(this._fGemContainer_spr));
		this._fMainGem_spr && Sequence.destroy(Sequence.findByTarget(this._fMainGem_spr));
		this._fColorBlendGem_spr && Sequence.destroy(Sequence.findByTarget(this._fColorBlendGem_spr));
		this._fGlowGem_spr && Sequence.destroy(Sequence.findByTarget(this._fGlowGem_spr));
		this._fLightSweep_sprt && Sequence.destroy(Sequence.findByTarget(this._fLightSweep_sprt));
		this._fSmoke_spr && Sequence.destroy(Sequence.findByTarget(this._fSmoke_spr));
		this._fGemFlare_spr && Sequence.destroy(Sequence.findByTarget(this._fGemFlare_spr));

		if (this._fAnimationSequences_s_arr && this._fAnimationSequences_s_arr.length)
		{
			for (let i = 0; i < this._fAnimationSequences_s_arr.length; i++)
			{
				this._fAnimationSequences_s_arr[i].off(Sequence.EVENT_ON_SEQUENCE_PLAYING_COMPLETED, this._checkAnimationFinished, this);
				this._fAnimationSequences_s_arr[i] = null;
			}
			this._fAnimationSequences_s_arr = null;
		}

		super.destroy();

		this._fTreasureId_num = null;
		this._fGameName_str = null;
		this._fFlare_spr = null;
		this._fGemContainer_spr = null;
		this._fMainGem_spr = null;
		this._fFinishPosition_obj = null;
		this._fColorBlendGem_spr = null;
		this._fGlowGem_spr = null;
		this._fLightSweep_sprt = null;
		this._fSmoke_spr = null;
		this._fGemFlare_spr = null;
	}
}

export default TreasureAwardingAnimation;