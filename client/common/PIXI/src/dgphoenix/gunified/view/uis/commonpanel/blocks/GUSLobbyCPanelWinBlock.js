import Sprite from '../../../../../unified/view/base/display/Sprite';
import { APP } from '../../../../../unified/controller/main/globals';
import TextField from '../../../../../unified/view/base/display/TextField';
import I18 from '../../../../../unified/controller/translations/I18';
import Counter from '../../../../../unified/view/custom/Counter';

class GUSLobbyCPanelWinBlock extends Sprite
{
	hideSeptum()
	{
		this._fSeptum_grphc.visible = false;
	}

	showSeptum()
	{
		this._fSeptum_grphc.visible = true;
	}

	updateWinIfRequired(aValue_num)
	{
		this._updateWinIfRequired(aValue_num);
	}

	resetCounting()
	{
		this._resetCounting();
	}

	constructor(aIndicatorsUpdateTime_num)
	{
		super();

		this._fIndicatorsUpdateTime_num = aIndicatorsUpdateTime_num;
		this._fWinValue_num = null;

		this._fWinView_tf = null;
		this._fWinCounter_c = null;

		this._fSeptum_grphc = null;

		this._init();
	}

	_init()
	{
		let lWinCaptionViewParams_obj = this.__winCaptionViewParams;
		let lWinLabelAsset_ta = I18.generateNewCTranslatableAsset(this.__winCaptionTAssetName);
		this.addChild(lWinLabelAsset_ta);
		lWinLabelAsset_ta.position.set(lWinCaptionViewParams_obj.position.x, lWinCaptionViewParams_obj.position.y);

		let lIconViewParams_obj = this.__iconViewParams;
		let lWinBase_sprt = APP.library.getSprite(this.__iconAssetName);
		this.addChild(lWinBase_sprt);
		lWinBase_sprt.position.set(lIconViewParams_obj.position.x, lIconViewParams_obj.position.y);
		lWinBase_sprt.scale.set(lIconViewParams_obj.scale.x, lIconViewParams_obj.scale.y);

		let lValueTextViewParams_obj = this.__valueTextViewParams;
		this._fWinView_tf = this.addChild(new TextField(this.__winTextFormat));
		this._fWinView_tf.anchor.set(lValueTextViewParams_obj.anchor.x, lValueTextViewParams_obj.anchor.y);
		this._fWinView_tf.position.set(lValueTextViewParams_obj.position.x, lValueTextViewParams_obj.position.y);
		this._fWinView_tf.maxWidth = lValueTextViewParams_obj.maxTextWidth;

		this._setWin(0);
		this._fWinCounter_c = new Counter({callback: this._setWin.bind(this)}, {callback: this._getWin.bind(this)});

		this._addSeptum();
	}

	get __winCaptionTAssetName()
	{
		return "TACommonPanelWinLabel";
	}

	get __winCaptionViewParams()
	{
		return { position: {x: -16, y: -17} }
	}

	get __iconAssetName()
	{
		// must be overridden
		return undefined;
	}

	get __iconViewParams()
	{
		return { position: {x: -42, y: (APP.isMobile ? -7 : 0)}, scale: {x: (APP.isMobile ? 0.76 : 1), y: (APP.isMobile ? 0.76 : 1)} }
	}

	get __valueTextViewParams()
	{
		return { 
					anchor: {x: 0, y: 0.5}, 
					position: {x: (APP.isMobile ? -48 : -26), y: (APP.isMobile ? 7 : 4)}, 
					scale: {x: (APP.isMobile ? 0.76 : 1), y: (APP.isMobile ? 0.76 : 1)},
					maxTextWidth: (APP.isMobile ? 76 : 62)
				}
	}

	get __septumViewParams()
	{
		return { position: {x: -63, y: 0}, width: 1, height: (APP.isMobile ? 22 : 18), color: 0x4e4e4e }
	}

	_addSeptum()
	{
		let lSeptumViewParams_obj = this.__septumViewParams;
		this._fSeptum_grphc = this.addChild(new PIXI.Graphics());
		this._fSeptum_grphc.beginFill(lSeptumViewParams_obj.color).drawRect(-lSeptumViewParams_obj.width, -lSeptumViewParams_obj.height/2, lSeptumViewParams_obj.width, lSeptumViewParams_obj.height).endFill();
		this._fSeptum_grphc.position.set(lSeptumViewParams_obj.position.x, lSeptumViewParams_obj.position.y);
	}

	_setWin(aValue_num)
	{
		this._fWinValue_num = aValue_num;
		this._fWinView_tf.text = this._formatMoneyValue(aValue_num);
	}

	_getWin()
	{
		return this._fWinValue_num;
	}

	_updateWinIfRequired(aValue_num)
	{
		aValue_num = aValue_num || 0;

		let lDuration_num = this._fIndicatorsUpdateTime_num;
		if (this._fWinCounter_c.inProgress)
		{
			if (lDuration_num > 0 || aValue_num == 0)
			{
				this._fWinCounter_c.stopCounting();
			}
			else
			{
				this._fWinCounter_c.updateCounting(aValue_num);
			}
		}

		if (lDuration_num > 0 || aValue_num == 0)
		{
			this._fWinCounter_c.startCounting(aValue_num, lDuration_num);
		}
		else if (this._fWinValue_num !== aValue_num)
		{
			this._setWin(aValue_num);
		}
	}

	_resetCounting()
	{
		this._fWinCounter_c && this._fWinCounter_c.finishCounting();
	}

	_formatMoneyValue(aValue_num)
	{
		if (aValue_num !== undefined)
		{
			return APP.currencyInfo.i_formatNumber(aValue_num, true);
		}

		return "";
	}

	get __winTextFormat()
	{
		return {
			fontFamily: "fnt_nm_cmn_barlow",
			fontSize: APP.isMobile ? 13 : 11,
			align: "left",
			letterSpacing: 0.5,
			padding: 5,
			fill: 0xffffff
		};
	}

	destroy()
	{
		this._fWinCounter_c && this._fWinCounter_c.destructor();

		super.destroy();

		this._fIndicatorsUpdateTime_num = null;
		this._fWinValue_num = null;

		this._fWinView_tf = null;
		this._fWinCounter_c = null;

		this._fSeptum_grphc = null;
	}
}

export default GUSLobbyCPanelWinBlock