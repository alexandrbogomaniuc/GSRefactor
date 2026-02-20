import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import NumberValueFormat from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';

class BossGemsPanelElement extends Sprite
{
	set gemsCount(aValue_num)
	{
		this._gemsCountField.text = "x" + aValue_num;

		this._fCurrentGemsCount_num = aValue_num;
	}

	get gemsCount()
	{
		return this._fCurrentGemsCount_num;
	}

	set gemsPayout(aValue_num)
	{
		let lCurrencySymbol_str = APP.playerController.info.currencySymbol;
		let lCurrencySymbolText_str = (lCurrencySymbol_str && lCurrencySymbol_str.length > 0) ? "\u200E" + lCurrencySymbol_str + "\u200E" : "";
		this._gemsPayout.text = lCurrencySymbolText_str + NumberValueFormat.formatMoney(aValue_num);
	}

	get gemId()
	{
		return this._fGemId_num;
	}

	startChangeValueAnimation()
	{
		this._startChangeValueAnimation();
	}

	constructor(aId_num, aGemAsset_str)
	{
		super();

		this._fGemId_num = aId_num;
		this._fGemsCountIndicator_tf = null;
		this._fGemsPayoutIndicator_tf = null;
		this._fFlaersAnims_arr = [];
		this._fGemsCountIndicatorContainer_spr = null;
		this._fCurrentGemsCount_num = null;

		this._init(aGemAsset_str);
	}

	_init(aGemAsset_str)
	{
		this._addGem(aGemAsset_str);
	}

	_addGem(aGemAsset_str)
	{
		let lCurrencySymbol_str = APP.playerController.info.currencySymbol;

		let lGemPositionX_num = -15;
		let lGemDesktopDcale_num = 1;
		let lGemMobileScale_num = 0.8;
		if (lCurrencySymbol_str && lCurrencySymbol_str.length >= 3)
		{
			lGemPositionX_num = -22;
			lGemDesktopDcale_num = 0.7;
			lGemMobileScale_num = 0.7;
		}

		let l_spr = this.addChild(APP.library.getSpriteFromAtlas(aGemAsset_str));
		l_spr.position.set(lGemPositionX_num, 0);
		l_spr.scale.set(lGemDesktopDcale_num);

		if (APP.isMobile)
		{
			l_spr.scale.set(lGemMobileScale_num);
			l_spr.position.set(-19, 0);
		}
	}

	get _gemsCountField()
	{
		if (!this._fGemsCountIndicator_tf)
		{
			let lStyle_obj = {
				fontFamily: "fnt_nm_barlow_semibold",
				fontSize: 12,
				fill: 0xfb8e0c,
				dropShadow: true,
				dropShadowColor: 0x000000,
				dropShadowAngle: Math.PI/2,
				dropShadowDistance: 1,
				dropShadowAlpha: 0.5
			};

			this._fGemsCountIndicatorContainer_spr = this.addChild(new Sprite());
			this._fGemsCountIndicatorContainer_spr.position.set(22.5, 7);

			this._fGemsCountIndicator_tf = this._fGemsCountIndicatorContainer_spr.addChild(new TextField(lStyle_obj));
			this._fGemsCountIndicator_tf.maxWidth = 20;
			this._fGemsCountIndicator_tf.position.set(this._fGemsCountIndicator_tf.getBounds().width * this._fGemsCountIndicator_tf.scale.x, 0);
			this._fGemsCountIndicator_tf.anchor.set(1, 0.5);
		}

		return this._fGemsCountIndicator_tf;
	}

	_startChangeValueAnimation()
	{
		this._startFlaerAnimation();
		this._startGemsCountIndicatorAnimation()
	}

	_startFlaerAnimation()
	{
		let lFlaer_spr = this.addChild(APP.library.getSpriteFromAtlas("common/orange_flare"));
		lFlaer_spr.blendMode = PIXI.BLEND_MODES.ADD;
		lFlaer_spr.position.set(17, 7);
		lFlaer_spr.scale.set(0);
		let lFlareSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 0.6},			{prop: 'scale.y', to: 0.34}],		duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 0.52},			{prop: 'scale.y', to: 0.44}],		duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 0},				{prop: 'scale.y', to: 0}],			duration: 4*FRAME_RATE, onfinish: () => {
				let lId_num = this._fFlaersAnims_arr.indexOf(lFlaer_spr);
				if (~lId_num)
				{
					this._fFlaersAnims_arr.splice(lId_num, 1);
				}
				lFlaer_spr && lFlaer_spr.destroy();
				lFlaer_spr = null;
			}}
		];

		this._fFlaersAnims_arr.push(lFlaer_spr);

		Sequence.start(lFlaer_spr, lFlareSeq_arr);
	}

	_startGemsCountIndicatorAnimation()
	{
		this._gemsCountField.scale.set(1.5);
		Sequence.destroy(Sequence.findByTarget(this._gemsCountField));

		let lMobileScale_num = APP.isMobile ? 2 : 1;

		let lSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 0.9 / lMobileScale_num},			{prop: 'scale.y', to: 0.9 / lMobileScale_num}],			duration: 4*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1 / lMobileScale_num},				{prop: 'scale.y', to: 1 / lMobileScale_num}],			duration: 3*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1.08 / lMobileScale_num},			{prop: 'scale.y', to: 1.08 / lMobileScale_num}],		duration: 3*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1 / lMobileScale_num},				{prop: 'scale.y', to: 1 / lMobileScale_num}],			duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 0.96 / lMobileScale_num},			{prop: 'scale.y', to: 0.96 / lMobileScale_num}],		duration: 3*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1 / lMobileScale_num},				{prop: 'scale.y', to: 1 / lMobileScale_num}],			duration: 2*FRAME_RATE},
		];

		Sequence.start(this._gemsCountField, lSeq_arr);
	}

	get _gemsPayout()
	{
		if (!this._fGemsPayoutIndicator_tf)
		{
			let lStyle_obj = {
				fontFamily: "fnt_nm_barlow_semibold",
				fontSize: 12,
				fill: 0xffffff,
				align: "center",
				dropShadow: true,
				dropShadowColor: 0x000000,
				dropShadowAngle: Math.PI/2,
				dropShadowDistance: 1,
				dropShadowAlpha: 0.5
			};

			let lCurrencySymbol_str = APP.playerController.info.currencySymbol;
			let lMaxWidth_num = APP.isMobile ? 29: 20;
			if(lCurrencySymbol_str && lCurrencySymbol_str.length >= 3)
			{
				lMaxWidth_num = 38;
			}

			this._fGemsPayoutIndicator_tf = this.addChild(new TextField(lStyle_obj));
			this._fGemsPayoutIndicator_tf.position.set(26, -7);
			this._fGemsPayoutIndicator_tf.maxWidth = lMaxWidth_num;
			this._fGemsPayoutIndicator_tf.anchor.set(1, 0.5);
		}
		return this._fGemsPayoutIndicator_tf;
	}

	destroy()
	{
		super.destroy();

		Sequence.destroy(Sequence.findByTarget(this._fGemsCountIndicator_tf));
		this._fGemsCountIndicator_tf = null;
		this._fGemsPayoutIndicator_tf = null;

		if (this._fFlaersAnims_arr)
		{
			for (let lAnim_sprt of this._fFlaersAnims_arr)
			{
				Sequence.destroy(Sequence.findByTarget(lAnim_sprt));
				lAnim_sprt && lAnim_sprt.destroy();
				lAnim_sprt = null;
			}

			this._fFlaersAnims_arr = [];
		}
		this._fFlaersAnims_arr = null;
		this._fGemsCountIndicatorContainer_spr = null;
		this._fGemId_num = null;
		this._fCurrentGemsCount_num = null;
	}
}

export default BossGemsPanelElement;