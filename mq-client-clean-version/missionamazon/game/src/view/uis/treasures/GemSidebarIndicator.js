import { Sprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import { FRAME_RATE, TREASURES_NAMES, TOTAL_QUEST_COMPLETE_GEMS } from '../../../../../shared/src/CommonConstants';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { AtlasSprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasConfig from './../../../config/AtlasConfig';
import TextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';

let gems_textures = null;
function _initGemsTextures()
{
	if (!gems_textures)
	{
		gems_textures = AtlasSprite.getFrames(APP.library.getAsset("treasures/gems"), AtlasConfig.Gems, "");
	}
}

class GemSidebarIndicator extends Sprite
{
	static get AWARD_ANIMATION_FINISH()							{ return "AWARD_ANIMATION_FINISH"; }
	static get EVENT_ON_PRE_QUEST_COMPETE_ANIMATION_END()		{ return "EVENT_ON_PRE_QUEST_COMPETE_ANIMATION_END"; }

	clear()
	{
		this._clear();
		if (APP.isBattlegroundGame)
		{
			this.i_startDeactivateAnimation(true);
		}
	}

	startPreQuestCompleteAnimation()
	{
		this._startPreQuestCompleteAnimation();
	}

	i_startDeactivateAnimation(aOptSkipAnimation_bl=false)
	{
		if(aOptSkipAnimation_bl)
		{
			this._fGemDisableView_spr.visible = true;
			this._fGemDisableView_spr.alpha = 1;
		}
		else
		{
			let l_seq = [
				{ tweens: [{ prop: 'alpha', from: 0, to: 0 }], duration: 3 * FRAME_RATE },
				{ tweens: [{ prop: 'alpha', from: 0, to: 1 }], duration: 5 * FRAME_RATE, onfinish: Sequence.destroy.bind(this, Sequence.findByTarget(this._fGemDisableView_spr)) }
			];
			Sequence.start(this._fGemDisableView_spr, l_seq);
		}
	}

	setGemAmount(aValue_int)
	{
		this._setGemAmount(aValue_int);
	}

	i_setPrice(aValue_num)
	{
		this._setPrice(aValue_num);
	}

	startAwardAnimation()
	{
		this._startAwardAnimation();
	}

	getGemGlobalPosition()
	{
		return this._getGemGlobalPosition();
	}

	get id()
	{
		return this._fId_num;
	}

	constructor(aGemId_num, isLineNeed_bl=false)
	{
		super();

		_initGemsTextures();

		this._fId_num = aGemId_num;
		this._fGameName_str = this._getGemNameById(aGemId_num);
		this._fGem_spr = null;
		this._fGemGlow_spr = null;
		this._fAmountIndicator_tf = null;
		this._fGemDisableView_spr = null;

		this._init(isLineNeed_bl);
	}

	_getGemGlobalPosition()
	{
		if (this._fGem_spr)
		{
			return this._fGem_spr.parent.localToGlobal(this._fGem_spr.x, this._fGem_spr.y);
		}
	}

	_init(isLineNeed_bl)
	{
		this._addGem();
		isLineNeed_bl && this._addLine();
		if (APP.isBattlegroundGame)
		{
			this._addPriceIndicator();
		}
		else
		{
			this._addAmountIndicator();
		}
	}

	_addGem()
	{
		this._fGem_spr = this.addChild(new Sprite());
		this._fGem_spr.texture = this._getGemsTextureByName(this._fGameName_str + "_mask");
		this._fGem_spr.scale.set(0.33);

		this._fGemDisableView_spr = this._fGem_spr.addChild(new Sprite());
		this._fGemDisableView_spr.texture = this._getGemsTextureByName(this._fGameName_str + "_gray");
		this._fGemDisableView_spr.visible = APP.isBattlegroundGame;

		this._fGemGlow_spr = this._fGem_spr.addChild(new Sprite());
		this._fGemGlow_spr.texture = this._getGemsTextureByName(this._fGameName_str + "_glow");
		this._fGemGlow_spr.alpha = 0;
	}

	_addLine()
	{
		const lLine_spr = this.addChild(APP.library.getSpriteFromAtlas("treasures/line"));
		lLine_spr.position.set(0, 30);
	}

	_addAmountIndicator()
	{
		this._fAmountIndicator_tf = this.addChild(new TextField(this._indicatorStyle));
		this._fAmountIndicator_tf.text = "0/" + TOTAL_QUEST_COMPLETE_GEMS;
		this._fAmountIndicator_tf.position.set(1, 18);
		this._fAmountIndicator_tf.anchor.set(0.5, 0.5);
	}

	_addPriceIndicator()
	{
		this._fPriceIndicator_tf = this.addChild(new TextField(this._indicatorStyle));
		this._fPriceIndicator_tf.position.set(1, 18);
		this._fPriceIndicator_tf.anchor.set(0.5, 0.5);
		this._fPriceIndicator_tf.maxWidth = 25
	}

	_startAwardAnimation()
	{
		if (!this._fGemGlow_spr || Sequence.findByTarget(this._fGemGlow_spr).length > 0) return;

		if (APP.isBattlegroundGame)
		{
			this._fGemDisableView_spr.alpha = 0;
		}

		this._fGemGlow_spr.alpha = 1;
		this._fGemGlow_spr.scale.set(0.43);

		const lScaleSeq_arr = [
			{ tweens: [{ prop: 'scale.x', to: 1.13 }, { prop: 'scale.y', to: 1.13 }], duration: 4 * FRAME_RATE, ease: Easing.exponential.easeOut },
			{ tweens: [{ prop: 'scale.x', to: 0.78 }, { prop: 'scale.y', to: 0.78 }], duration: 7 * FRAME_RATE, ease: Easing.quartic.easeIn },
		];
		Sequence.start(this._fGemGlow_spr, lScaleSeq_arr);

		let lAlphaSeq_arr = [
			{ tweens: [], duration: 9 * FRAME_RATE },
			{ tweens: [{ prop: 'alpha', to: 0 }], duration: 3 * FRAME_RATE },
		];

		Sequence.start(this._fGemGlow_spr, lAlphaSeq_arr).once(Sequence.EVENT_ON_SEQUENCE_PLAYING_COMPLETED, this._onAwardAnimationFinish, this);
	}

	_onAwardAnimationFinish()
	{
		this.emit(GemSidebarIndicator.AWARD_ANIMATION_FINISH, { gemId: this._fId_num });
	}

	_startPreQuestCompleteAnimation()
	{
		let l_seq = [
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: Utils.getRandomWiggledValue(this._fGem_spr.position.x, 0.5) }, { prop: 'position.y', to: Utils.getRandomWiggledValue(this._fGem_spr.position.y, 0.5) }], duration: 1 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: this._fGem_spr.position.x }, { prop: 'position.y', to: this._fGem_spr.position.y }], duration: 1 * FRAME_RATE },
		];

		Sequence.start(this._fGem_spr, l_seq).once(Sequence.EVENT_ON_SEQUENCE_PLAYING_COMPLETED, this._onPreQuestCompleteAnimationFinish, this);
	}

	_onPreQuestCompleteAnimationFinish()
	{
		this.emit(GemSidebarIndicator.EVENT_ON_PRE_QUEST_COMPETE_ANIMATION_END, { gemId: this._fId_num });
	}

	get _indicatorStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 10,
			align: "center",
			fill: 0xffffff,
			letterSpacing: 1.8,
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI / 2,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5
		}

		return lStyle_obj;
	}

	_getGemsTextureByName(aName_str)
	{
		_initGemsTextures();

		return Utils.getTexture(gems_textures, aName_str);
	}

	_getGemNameById(aGemId_num)
	{
		return TREASURES_NAMES[aGemId_num];
	}

	_setGemAmount(aValue_int)
	{
		this._fAmountIndicator_tf.text = aValue_int + "/" + TOTAL_QUEST_COMPLETE_GEMS;
	}

	_setPrice(aValue_num)
	{
		this._fPriceIndicator_tf.text = APP.currencyInfo.i_formatNumber(aValue_num);
	}

	_clear()
	{
		this._fGem_spr && Sequence.destroy(Sequence.findByTarget(this._fGem_spr));
		this._fGem_spr.position.set(0, 0);
		this._fGemGlow_spr && Sequence.destroy(Sequence.findByTarget(this._fGemGlow_spr));
		this._fGemGlow_spr.alpha = 0;
		this._fGemDisableView_spr && Sequence.destroy(Sequence.findByTarget(this._fGemDisableView_spr));
	}

	destroy()
	{
		this._fGem_spr && Sequence.destroy(Sequence.findByTarget(this._fGem_spr));
		this._fGemGlow_spr && Sequence.destroy(Sequence.findByTarget(this._fGemGlow_spr));

		super.destroy();

		this._fId_num = null;
		this._fGameName_str = null;
		this._fGem_spr = null;
		this._fGemGlow_spr = null;
		this._fAmountIndicator_tf = null;
		this._fGemDisableView_spr = null;
		this._fPriceIndicator_tf = null;
	}
}

export default GemSidebarIndicator;