import { Sprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { FRAME_RATE, TREASURES_NAMES, TOTAL_QUEST_COMPLETE_GEMS } from '../../../../../shared/src/CommonConstants';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { AtlasSprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasConfig from './../../../config/AtlasConfig';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import PathTween from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/PathTween';

let gems_textures = null;
function _initGemsTextures()
{
	if (!gems_textures)
	{
		gems_textures = AtlasSprite.getFrames(APP.library.getAsset("treasures/gems"), AtlasConfig.Gems, "");
	}
}

let sparkles_textures = null;
function _initSparklesTextures()
{
	if (!sparkles_textures)
	{
		sparkles_textures = AtlasSprite.getFrames([APP.library.getAsset("treasures/sparkles_0"), APP.library.getAsset("treasures/sparkles_1")], [AtlasConfig.Sparkles0, AtlasConfig.Sparkles1], "");
	}
}

class QuestCompletedAmimation extends Sprite
{
	static get EVENT_ON_GEM_ANIMATION_START()		{ return "EVENT_ON_GEM_ANIMATION_START"; }
	static get EVENT_ON_ANIMATION_FINISH()			{ return "EVENT_ON_ANIMATION_FINISH"; }

	startAnimation()
	{
		this.__startAnimation();
	}

	get gemId()
	{
		return this._fTreasureId_num;
	}

	constructor(aTreasureId_num)
	{
		super();

		_initGemsTextures();
		_initSparklesTextures();

		this._fTreasureId_num = aTreasureId_num;
		this._fGameName_str = this._getGemNameById(aTreasureId_num);
		this._fFlare_spr = null;
		this._fSparkles_spr = null;
		this._fGems_spr_arr = [];
		this._fGemsGlow_spr_arr = [];
		this._fGemsTimers_t_arr = [];
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

	get __pathTweenDuration()
	{
		return 19 * FRAME_RATE;
	}

	__startAnimation()
	{
		this._startGemsAnimation();
		this._fSparklesTimer_t = new Timer(() => { this._startSparklesAnimation(); }, 2 * FRAME_RATE);
		this._startFlareAnimation();
	}

	_startGemsAnimation()
	{
		for (let i = 0; i < TOTAL_QUEST_COMPLETE_GEMS; i++)
		{
			const lGemTimer_t = new Timer(this._animateGem.bind(this), (i * 2 + 2) * FRAME_RATE);
			this._fGemsTimers_t_arr.push(lGemTimer_t);
		}
	}

	_animateGem()
	{
		const lGem_spr = this.addChild(new Sprite());
		lGem_spr.texture = this._getGemsTextureByName(this._fGameName_str);
		lGem_spr.scale.set(0.33);

		const lPathTween_pt = new PathTween(lGem_spr, [{ x: 0, y: 0 }, { x: -21, y: 0 }, { x: -135, y: 80 }], true);
		lPathTween_pt.start(19 * FRAME_RATE, Easing.quadratic.easeIn, () => { lGem_spr.visible = false; this.__checkAnimationFinish() });

		const lGemGlow_spr = lGem_spr.addChild(new Sprite());
		lGemGlow_spr.texture = this._getGemsTextureByName(this._fGameName_str + "_glow");
		lGemGlow_spr.alpha = 1;

		this.emit(QuestCompletedAmimation.EVENT_ON_GEM_ANIMATION_START, { gemId: this._fTreasureId_num });

		let lGlowGemAlphaSeq_arr = [
			{ tweens: [], duration: 4 * FRAME_RATE },
			{ tweens: [{ prop: 'alpha', to: 0 }], duration: 20 * FRAME_RATE },
		];
		Sequence.start(lGemGlow_spr, lGlowGemAlphaSeq_arr);

		this._fGems_spr_arr.push(lGem_spr);
		this._fGemsGlow_spr_arr.push(lGemGlow_spr);
	}

	__checkAnimationFinish()
	{
		if (this._fGems_spr_arr.find(l_spr => l_spr.visible == true) === undefined)
		{
			this.emit(QuestCompletedAmimation.EVENT_ON_ANIMATION_FINISH);
		}
	}

	_startSparklesAnimation()
	{
		this._fSparkles_spr = this.addChild(new Sprite());
		this._fSparkles_spr.textures = sparkles_textures;
		this._fSparkles_spr.blendMode = PIXI.BLEND_MODES.ADD;
		this._fSparkles_spr.pivot.set(348, 0);
		this._fSparkles_spr.scale.set(0.4);
		this._fSparkles_spr.animationSpeed = 0.5;
		this._fSparkles_spr.once('animationend', () => { this._fSparkles_spr && this._fSparkles_spr.destroy() });
		this._fSparkles_spr.play();
	}

	_startFlareAnimation()
	{
		this._fFlare_spr = this.addChild(APP.library.getSpriteFromAtlas("treasures/flare_orange"));
		this._fFlare_spr.blendMode = PIXI.BLEND_MODES.ADD;
		this._fFlare_spr.scale.set(0);

		let lFlareScaleSeq_arr = [
			{ tweens: [{ prop: 'scale.x', to: 0.28 }, { prop: 'scale.y', to: 0.25 }], duration: 2 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 0.2 }, { prop: 'scale.y', to: 0.18 }], duration: 2 * FRAME_RATE },
			{ tweens: [{ prop: 'scale.x', to: 0 }, { prop: 'scale.y', to: 0 }], duration: 4 * FRAME_RATE }
		];
		Sequence.start(this._fFlare_spr, lFlareScaleSeq_arr);
	}

	destroy()
	{
		this._fGemsTimer_t && this._fGemsTimer_t.destructor();
		this._fGemsTimer_t = null;
		this._fSparklesTimer_t && this._fSparklesTimer_t.destructor();
		this._fSparklesTimer_t = null;
		this._fFlare_spr && Sequence.destroy(Sequence.findByTarget(this._fFlare_spr));

		if (this._fGemsTimers_t_arr && this._fGemsTimers_t_arr.length)
		{
			for (let i = 0; i < this._fGemsTimers_t_arr.length; i++)
			{
				this._fGemsTimers_t_arr[i] && this._fGemsTimers_t_arr[i].destructor();
				this._fGemsTimers_t_arr[i] = null;
			}
		}

		if (this._fGems_spr_arr && this._fGems_spr_arr.length)
		{
			for (let i = 0; i < this._fGems_spr_arr.length; i++)
			{
				PathTween.destroy(PathTween.findByTarget(this._fGems_spr_arr[i]));
				this._fGems_spr_arr[i].destroy();
				this._fGems_spr_arr[i] = null;
			}
		}

		if (this._fGemsGlow_spr_arr && this._fGemsGlow_spr_arr.length)
		{
			for (let i = 0; i < this._fGemsGlow_spr_arr.length; i++)
			{
				Sequence.destroy(Sequence.findByTarget(this._fGemsGlow_spr_arr[i]));
				this._fGemsGlow_spr_arr[i].destroy();
				this._fGemsGlow_spr_arr[i] = null;
			}
		}

		super.destroy();

		this._fTreasureId_num = null;
		this._fGameName_str = null;
		this._fFlare_spr = null;
		this._fSparkles_spr = null;
		this._fGems_spr_arr = null;
		this._fGemsGlow_spr_arr = null;
		this._fGemsTimers_t_arr = null;
	}
}


class BTGQuestCompletedAmimation extends QuestCompletedAmimation
{
	__startAnimation()
	{
		this.emit(QuestCompletedAmimation.EVENT_ON_GEM_ANIMATION_START, { gemId: this._fTreasureId_num });
		this._fSparklesTimer_t = new Timer(() => { this._startSparklesAnimation(); }, 2 * FRAME_RATE);
		this._startFlareAnimation();
		this._fFinishBTGTimer_t = new Timer(this.__checkAnimationFinish.bind(this), 8 * FRAME_RATE);
	}

	destroy()
	{
		if (this._fFinishBTGTimer_t)
		{
			this._fFinishBTGTimer_t.destructor();
			this._fFinishBTGTimer_t = null;
			this.__checkAnimationFinish();
		}

		super.destroy();
	}
}

export {QuestCompletedAmimation, BTGQuestCompletedAmimation};
