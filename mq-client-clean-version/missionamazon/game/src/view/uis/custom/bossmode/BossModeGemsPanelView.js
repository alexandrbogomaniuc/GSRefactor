import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { ENEMIES } from '../../../../../../shared/src/CommonConstants';
import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from './../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import BossGemsPanelElement from './BossGemsPanelElement';
import NumberValueFormat from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import BossModeGemsTotalWinIndicator from './BossModeGemsTotalWinIndicator';
import Counter from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/Counter';
import AtlasConfig from '../../../../config/AtlasConfig';
import AtlasSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';

const GEMS = [
	{
		id: 0,
		asset: "boss_mode/gems_panel/diamond",
		position: {x: 0, y: 0},
		treasureId: 11,
	},
	{
		id: 1,
		asset: "boss_mode/gems_panel/ruby",
		position: {x: 0, y: 0},
		treasureId: 13,
	},
	{
		id: 2,
		asset: "boss_mode/gems_panel/emerald",
		position: {x: 0, y: 0},
		treasureId: 12,
	},
	{
		id: 3,
		asset: "boss_mode/gems_panel/sapphire",
		position: {x: 0, y: 0},
		treasureId: 14,
	}
];

const PANEL_ELEMENTS_RANGE = 44;
const PANEL_ELEMENTS_START_POSITION_Y = -84;
const TOTAL_WIN_COUNTING_DELAY = 1500;

let _criticalParticlesTextures = null;

function _initParticlesTextures()
{
	if (_criticalParticlesTextures) return;

	_criticalParticlesTextures = AtlasSprite.getFrames(APP.library.getAsset("critical_hit/critical_particles"), AtlasConfig.CriticalParticles, "");
}

class BossModeGemsPanelView extends SimpleUIView
{
	updatePayouts()
	{
		this._updatePayouts();
	}

	updateGemsCounters(aSkipAnimation_b)
	{
		this._updateGemsCounters(aSkipAnimation_b);
	}

	updateTotalWin(aSkipAnimation_b)
	{
		this._updateTotalWin(aSkipAnimation_b);
	}

	getGemElementByGemId(Id)
	{
		if (!this._fPanelElaments_bgpe_arr) return null;

		let lGemId = this._getBossGemId(Id);
		for (let gemElement of this._fPanelElaments_bgpe_arr)
		{
			if (gemElement.gemId == lGemId)
			{
				return gemElement;
			}
		}

		return null;
	}

	constructor()
	{
		super();

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			_initParticlesTextures();
		}

		this._fPanelContainer_sprt = null;
		this._fTotalValue_bmgtwi = null;
		this._fPanelElaments_bgpe_arr = [];
		this._fTotalWinCounter_c = null;
		this._fFlaer_spr = null;
		this._fStartTotalWinCountingFinishAnimation_t = null;
	}

	_getBossGemId(aId_num)
	{
		let lGemId_int = 0;
		for (let i = 0; i < GEMS.length; i++)
		{
			if(GEMS[i].id == aId_num)
			{
				lGemId_int = GEMS[i].id;
			}
		}

		return lGemId_int;
	}

	__init()
	{
		super.__init();

		this._initPanel();
	}

	_initPanel()
	{
		this._fPanelContainer_sprt = this.addChild(new Sprite());

		this._addBack();
		this._addPanelElemants();
		this._addTotalValue();
	}

	_addBack()
	{
		this._fPanelContainer_sprt.addChild(APP.library.getSpriteFromAtlas("boss_mode/gems_panel/back"));
	}
	
	_addPanelElemants()
	{
		for (let i = 0; i < GEMS.length; i++)
		{
			let lElement_bgpe = this._fPanelContainer_sprt.addChild(new BossGemsPanelElement(GEMS[i].id, GEMS[i].asset));
			lElement_bgpe.position.y = PANEL_ELEMENTS_START_POSITION_Y + i * PANEL_ELEMENTS_RANGE;
			this._fPanelElaments_bgpe_arr.push(lElement_bgpe);
		}

		this._updateGemsCounters();
	}

	_updateGemsCounters(aSkipAnimation_b = false)
	{
		if(this._fPanelElaments_bgpe_arr)
		{
			for (let i = 0; i < this._fPanelElaments_bgpe_arr.length; i++)
			{
				let lCount_int = this.uiInfo.currentGemsCount[i];
				if(this._fPanelElaments_bgpe_arr[i].gemsCount != lCount_int && lCount_int != 0 && !aSkipAnimation_b)
				{
					this._fPanelElaments_bgpe_arr[i].startChangeValueAnimation();
				}
				this._fPanelElaments_bgpe_arr[i].gemsCount = lCount_int;
			}
		}
	}

	_updateTotalWin(aSkipAnimation_b)
	{
		let lCurentTotalWin_num = this._getCurentTotalWin();
		if(!aSkipAnimation_b)
		{	
			this._fTotalWinCounter_c.startCounting(
				lCurentTotalWin_num,
				TOTAL_WIN_COUNTING_DELAY
			);

			this._fStartTotalWinCountingFinishAnimation_t = new Timer(this._startTotalWinCountingFinishAnimation.bind(this), TOTAL_WIN_COUNTING_DELAY / 2);
		}
		else
		{
			this.totalWinIndicatorView.indicatorValue = lCurentTotalWin_num;
		}
	}

	_getCurentTotalWin()
	{
		let lTotalWin_num = 0;
		let lInfo_bmgpi = this.uiInfo;
		let lCurrentGems_int_arr = lInfo_bmgpi.currentGemsCount;

		for(let i = 0; i < lCurrentGems_int_arr.length; i++)
		{
			lTotalWin_num += lCurrentGems_int_arr[i] * lInfo_bmgpi.getPayoutByGemId(i);
		}

		return lTotalWin_num;
	}

	_addTotalValue()
	{
		let lTotalWinCaption_cta = this._fPanelContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TABossModeTotalValueCaption"));
		lTotalWinCaption_cta.position.set(0, 91);

		this.totalWinIndicatorView.indicatorValue = 0;

		this._fTotalWinCounter_c = new Counter({ target: this.totalWinIndicatorView, method: "indicatorValue" });
	}

	_startTotalWinCountingFinishAnimation()
	{
		this._fStartTotalWinCountingFinishAnimation_t && this._fStartTotalWinCountingFinishAnimation_t.destructor();
		this._fStartTotalWinCountingFinishAnimation_t = null;

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._startParticle({x: 0, y: 103}, Utils.gradToRad(108));
			this._startParticle({x: 0, y: 103}, Utils.gradToRad(30));
			this._startParticle({x: 0, y: 103}, Utils.gradToRad(200));
		}

		Sequence.destroy(Sequence.findByTarget(this._fFlaer_spr));
		this._fFlaer_spr && this._fFlaer_spr.destroy();
		this._fFlaer_spr = null;

		this._fFlaer_spr = this.totalWinIndicatorView.addChild(APP.library.getSpriteFromAtlas("common/orange_flare"));
		this._fFlaer_spr.blendMode = PIXI.BLEND_MODES.ADD;
		this._fFlaer_spr.scale.set(0);
		let lFlareSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 3.3},			{prop: 'scale.y', to: 1.2}],		duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 2.1},			{prop: 'scale.y', to: 1.5}],		duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 0},				{prop: 'scale.y', to: 0}],			duration: 4*FRAME_RATE, onfinish: () => {
				this._fFlaer_spr && this._fFlaer_spr.destroy();
				this._fFlaer_spr = null;
			}}
		];

		Sequence.start(this._fFlaer_spr, lFlareSeq_arr);

		this._starTotalWinIndicatorAnimation();
	}

	_startParticle(aPos_obj, aRot_num)
	{
		let lParticle_sprt = this.addChild(new Sprite());
		lParticle_sprt.textures = _criticalParticlesTextures;
		lParticle_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lParticle_sprt.rotation = aRot_num;
		lParticle_sprt.scale.set(1.4);
		lParticle_sprt.position.set(aPos_obj.x, aPos_obj.y);
		lParticle_sprt.on('animationend', () => {
			lParticle_sprt && lParticle_sprt.destroy();
			lParticle_sprt = null;
		});
		lParticle_sprt.play();
		lParticle_sprt.fadeTo(1, 10*FRAME_RATE, null, () => {
			lParticle_sprt.fadeTo(0, 10*FRAME_RATE);
		});
	}

	_starTotalWinIndicatorAnimation()
	{
		Sequence.destroy(Sequence.findByTarget(this._fTotalValue_bmgtwi));
		this._fTotalValue_bmgtwi.scale.set(1.5);

		let lSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 0.9},			{prop: 'scale.y', to: 0.9}],		duration: 4*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1},				{prop: 'scale.y', to: 1}],			duration: 3*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1.08},			{prop: 'scale.y', to: 1.08}],		duration: 3*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1},				{prop: 'scale.y', to: 1}],			duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 0.96},			{prop: 'scale.y', to: 0.96}],		duration: 3*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1},				{prop: 'scale.y', to: 1}],			duration: 2*FRAME_RATE},
		];

		Sequence.start(this._fTotalValue_bmgtwi, lSeq_arr);
	}

	get totalWinIndicatorView()
	{
		return this._fTotalValue_bmgtwi || (this._fTotalValue_bmgtwi = this._initTotalWinIndicatorView());
	}

	_initTotalWinIndicatorView()
	{
		let l_bmgtwi = this.addChild(new BossModeGemsTotalWinIndicator());
		l_bmgtwi.position.set(0, 103);
		l_bmgtwi.centringIndicator();
		return l_bmgtwi;
	}

	_updatePayouts()
	{
		if(this._fPanelElaments_bgpe_arr)
		{
			for (let i = 0; i < this._fPanelElaments_bgpe_arr.length; i++)
			{
				let lPayout_num = this.uiInfo.getPayoutByGemId(this._fPanelElaments_bgpe_arr[i].gemId);
				this._fPanelElaments_bgpe_arr[i].gemsPayout = lPayout_num;
			}
		}
	}

	destroy()
	{
		super.destroy();

		this._fStartTotalWinCountingFinishAnimation_t && this._fStartTotalWinCountingFinishAnimation_t.destructor();
		this._fStartTotalWinCountingFinishAnimation_t = null;

		this._fPanelContainer_sprt = null;
		Sequence.destroy(Sequence.findByTarget(this._fTotalValue_bmgtwi));
		this._fTotalValue_bmgtwi && this._fTotalValue_bmgtwi.destroy();
		this._fTotalValue_bmgtwi = null;

		if (this._fPanelElaments_bgpe_arr)
		{
			while (this._fPanelElaments_bgpe_arr.length)
			{
				let lTargetTip_pt = this._fPanelElaments_bgpe_arr.shift();
				lTargetTip_pt.destroy();
			}
		}
		this._fPanelElaments_bgpe_arr = null;

		this._fTotalWinCounter_c && this._fTotalWinCounter_c.stopCounting();
		this._fTotalWinCounter_c = null;

		Sequence.destroy(Sequence.findByTarget(this._fFlaer_spr));
		this._fFlaer_spr && this._fFlaer_spr.destroy();
		this._fFlaer_spr = null;
	}
}

export default BossModeGemsPanelView;