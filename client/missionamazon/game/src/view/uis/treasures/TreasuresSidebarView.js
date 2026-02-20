import SimpleUIView from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import GemSidebarIndicator from './GemSidebarIndicator';
import { QuestCompletedAmimation, BTGQuestCompletedAmimation } from './QuestCompletedAmimation';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import { Sequence } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation';

const GEMS_SIDEBAR_INDICATORS_CONFIG = [
	{
		id: 0,
		position: { x: 1, y: -105 }
	},
	{
		id: 1,
		position: { x: 1, y: -105 + 1 * 48 }
	},
	{
		id: 2,
		position: { x: 1, y: -105 + 2 * 48 }
	},
	{
		id: 3,
		position: { x: 1, y: -105 + 3 * 48 }
	},
	{
		id: 4,
		position: { x: 1, y: -105 + 4 * 48 }
	},
];

class TreasuresSidebarView extends SimpleUIView
{
	static get QUEST_COMPLETE_ANIMATION_FINISH()		{ return "QUEST_COMPLETE_ANIMATION_FINISH"; }
	static get AWARD_ANIMATION_FINISH()					{ return GemSidebarIndicator.AWARD_ANIMATION_FINISH; }
	static get EVENT_ON_GEM_ANIMATION_START()			{ return QuestCompletedAmimation.EVENT_ON_GEM_ANIMATION_START; }

	clear()
	{
		this.__clear();
	}

	getTreasureLandingGlobalPosition(aGemId_num)
	{
		return this._getHandTreasureGlobalPosition(aGemId_num);
	}

	startGemAwardAnimationById(aGemId_num)
	{
		this._startGemAwardAnimationById(aGemId_num);
	}

	update(aLevel_int)
	{
		this.__update(aLevel_int);
	}

	startQuestCompleteAnimation(aGemId_int)
	{
		this.__startQuestCompleteAnimation(aGemId_int);
	}

	constructor()
	{
		super();

		this._fGemsIndicators_gi_arr = [];
		this._fQuestCompletedAmimation_spr_arr = [];

		this._fCurrentGemId_num = null;

		this._init();
	}

	__addBackground()
	{
		this._fBackGround_spr = this._fContainer_spr.addChild(APP.library.getSpriteFromAtlas("treasures/sidebar_back"));
	}

	__generateQuestCompletedAnimation(aGemId_int)
	{
		return new QuestCompletedAmimation(aGemId_int);
	}

	__addGemsIndicators()
	{
		for (let i = 0; i < GEMS_SIDEBAR_INDICATORS_CONFIG.length; i++)
		{
			const lGemSidebarIndicatorConfig_obj = GEMS_SIDEBAR_INDICATORS_CONFIG[i];
			const lIsLast_bl = i == GEMS_SIDEBAR_INDICATORS_CONFIG.length - 1;
			const lGemSidebarIndicator_gi = this._fContainer_spr.addChild(new GemSidebarIndicator(i, !lIsLast_bl));
			lGemSidebarIndicator_gi.on(GemSidebarIndicator.AWARD_ANIMATION_FINISH, this.emit, this);
			lGemSidebarIndicator_gi.position = lGemSidebarIndicatorConfig_obj.position;
			this._fGemsIndicators_gi_arr.push(lGemSidebarIndicator_gi);
		}
	}

	__update(aLevel_int)
	{
		const lCurrentGemsAmount_arr = this.uiInfo.currentGemsAmount;
		this._fGemsIndicators_gi_arr.forEach((l_gi, l_id) => {
			l_gi.setGemAmount(lCurrentGemsAmount_arr[l_id]);
		});

		this._fHighlightFrame_spr.position = this.__getGemPositionById(aLevel_int);
	}

	__startQuestCompleteAnimation(aGemId_int)
	{
		const lGemSidebarIndicator_gi = this._getGemSidebarIndicatorById(aGemId_int);
		lGemSidebarIndicator_gi.startPreQuestCompleteAnimation();
		lGemSidebarIndicator_gi.once(GemSidebarIndicator.EVENT_ON_PRE_QUEST_COMPETE_ANIMATION_END, this.__onPreQuestCompleteAnimationFinish, this);
	}

	__onPreQuestCompleteAnimationFinish(aEvent_obj)
	{
		const lGemId_int = aEvent_obj.gemId;
		const lQuestCompletedAmimation_qca = this._fContainer_spr.addChild(this.__generateQuestCompletedAnimation(lGemId_int));
		lQuestCompletedAmimation_qca.on(QuestCompletedAmimation.EVENT_ON_GEM_ANIMATION_START, this.emit, this);
		lQuestCompletedAmimation_qca.on(QuestCompletedAmimation.EVENT_ON_ANIMATION_FINISH, this._onQuestCompletedAnimationFinish, this);
		lQuestCompletedAmimation_qca.position = this.__getGemPositionById(lGemId_int);
		lQuestCompletedAmimation_qca.startAnimation();

		this._fQuestCompletedAmimation_spr_arr.push(lQuestCompletedAmimation_qca);
	}

	__getGemPositionById(aGemId_int)
	{
		for (let i = 0; i < GEMS_SIDEBAR_INDICATORS_CONFIG.length; i++)
		{
			if (GEMS_SIDEBAR_INDICATORS_CONFIG[i].id == aGemId_int)
			{
				return GEMS_SIDEBAR_INDICATORS_CONFIG[i].position;
			}
		}

		return { x: 0, y: 0 };
	}

	_init()
	{
		this._fContainer_spr = this.addChild(new Sprite());

		this.__addBackground();
		this.__addGemsIndicators();
		this._addHighlightedFrame();
	}

	_getHandTreasureGlobalPosition(aGemId_num)
	{
		const lGemSidebarIndicator_gi = this._getGemSidebarIndicatorById(aGemId_num);
		const lGemSidebarIndicatorGemGlobalPosition_obj = lGemSidebarIndicator_gi.getGemGlobalPosition();

		return lGemSidebarIndicatorGemGlobalPosition_obj;
	}

	_getGemSidebarIndicatorById(aGemId_num)
	{
		if (this._fGemsIndicators_gi_arr && this._fGemsIndicators_gi_arr.length)
		{
			return this._fGemsIndicators_gi_arr.find(l_gi => l_gi.id == aGemId_num);
		}
	}

	_addHighlightedFrame()
	{
		this._fHighlightFrame_spr = this.addChild(APP.library.getSpriteFromAtlas("treasures/highlight_frame"));
		this._fHighlightFrame_spr.blendMode = PIXI.BLEND_MODES.ADD;
		this._fHighlightFrame_spr.scale.set(1.15);
	}

	_startGemAwardAnimationById(aGemId_num)
	{
		const lGemSidebarIndicator_gi = this._getGemSidebarIndicatorById(aGemId_num);
		lGemSidebarIndicator_gi.startAwardAnimation();
	}

	_startChangeGemAnimation()
	{
		let l_seq = [
			{ tweens: [{prop: 'alpha', to: 1}], duration: 5*FRAME_RATE },
			{ tweens: [{prop: 'alpha', to: 0}], duration: 7*FRAME_RATE },
		];

		Sequence.start(this._fGlowBackGround_gs, l_seq);
	}

	_onQuestCompletedAnimationFinish(aEvent_obj)
	{
		const lQuestCompletedAmimation_qca = aEvent_obj.target;
		const lGemId_num = lQuestCompletedAmimation_qca.gemId;

		let lGemIndicator_gi = this._getGemSidebarIndicatorById(lGemId_num);
		lGemIndicator_gi.i_startDeactivateAnimation(false);

		const lIndex_int = this._fQuestCompletedAmimation_spr_arr.indexOf(lQuestCompletedAmimation_qca);
		if (~lIndex_int)
		{
			this._fQuestCompletedAmimation_spr_arr.splice(lIndex_int, 1);
		}
		lQuestCompletedAmimation_qca.off(QuestCompletedAmimation.EVENT_ON_GEM_ANIMATION_START, this.emit, this);
		lQuestCompletedAmimation_qca.off(QuestCompletedAmimation.EVENT_ON_ANIMATION_FINISH, this._onQuestCompletedAnimationFinish, this);
		lQuestCompletedAmimation_qca.destroy();

		this.emit(TreasuresSidebarView.QUEST_COMPLETE_ANIMATION_FINISH, { gemId: lGemId_num });
	}

	__clear()
	{
		this._fQuestCompletedAmimation_spr_arr && this._fQuestCompletedAmimation_spr_arr.forEach(l_qca => 
		{
			l_qca.off(QuestCompletedAmimation.EVENT_ON_GEM_ANIMATION_START, this.emit, this);
			l_qca.off(QuestCompletedAmimation.EVENT_ON_ANIMATION_FINISH, this._onQuestCompletedAnimationFinish, this);
			l_qca.destroy();
		});

		this._fGemsIndicators_gi_arr && this._fGemsIndicators_gi_arr.forEach(l_gi => l_gi.clear())

		this._fQuestCompletedAmimation_spr_arr = [];
	}

	destroy()
	{
		if (this._fGemsIndicators_gi_arr && this._fGemsIndicators_gi_arr.length)
		{
			for (let i = 0; i < this._fGemsIndicators_gi_arr.length; i++)
			{
				this._fGemsIndicators_gi_arr[i].off(GemSidebarIndicator.EVENT_ON_PRE_QUEST_COMPETE_ANIMATION_END, this.__onPreQuestCompleteAnimationFinish, this);
				this._fGemsIndicators_gi_arr[i].destroy();
				this._fGemsIndicators_gi_arr[i] = null;
			}
		}

		super.destroy();

		this._fCurrentGemId_num = null;

		this._fGemsIndicators_gi_arr = null;
	}
}

class TreasuresSidebarBTGView extends TreasuresSidebarView
{
	_addHighlightedFrame()
	{
		// nothing to do
	}

	__update()
	{
		let lPrices_obj = this.uiInfo.i_getPrices();
		for (let i in lPrices_obj)
		{
			const lGemSidebarIndicator_gi = this._getGemSidebarIndicatorById(i);
			lGemSidebarIndicator_gi.i_setPrice(lPrices_obj[i]);
		}
	}

	__startQuestCompleteAnimation(aGemId_int)
	{
		this.__onPreQuestCompleteAnimationFinish({gemId: aGemId_int});
		this._startGemAwardAnimationById(aGemId_int)
	}

	__generateQuestCompletedAnimation(aGemId_int)
	{
		return new BTGQuestCompletedAmimation(aGemId_int)
	}

	__clear()
	{
		this._fGemsIndicators_gi_arr && this._fGemsIndicators_gi_arr.forEach(l_gi => l_gi.i_startDeactivateAnimation());
		super.__clear();
	}
}

export {TreasuresSidebarView, TreasuresSidebarBTGView};